package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtType
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.local.entity.UserStatus
import com.example.data.local.entity.WithdrawalEntity
import com.example.data.repository.BizLedgerRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupFileInfo(
    val id: String,
    val fileName: String,
    val csvFileName: String,
    val jsonFileName: String,
    val timestamp: Long,
    val formattedDate: String,
    val totalRecords: Int,
    val fileSizeFormatted: String,
    val csvFile: File,
    val jsonFile: File
)

data class BackupRestoreResult(
    val success: Boolean,
    val message: String,
    val expensesRestored: Int = 0,
    val withdrawalsRestored: Int = 0,
    val debtsRestored: Int = 0,
    val budgetsRestored: Int = 0,
    val usersRestored: Int = 0
)

object DataBackupManager {

    private const val BACKUP_DIR_NAME = "bizledger_backups"

    private fun getBackupDir(context: Context): File {
        val dir = File(context.filesDir, BACKUP_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Creates a complete on-device backup consisting of:
     * 1. A multi-section Excel-compatible CSV file (opens in Microsoft Excel, Google Sheets, etc.)
     * 2. A complete JSON snapshot for 100% loss-less database restore
     */
    fun createBackup(
        context: Context,
        expenses: List<ExpenseEntity>,
        withdrawals: List<WithdrawalEntity>,
        debts: List<DebtEntity>,
        budgets: List<BudgetEntity>,
        users: List<UserEntity>
    ): BackupFileInfo {
        val dir = getBackupDir(context)
        val timestamp = System.currentTimeMillis()
        val sdfFile = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val sdfDisplay = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val dateStamp = sdfFile.format(Date(timestamp))
        val displayDate = sdfDisplay.format(Date(timestamp))

        val baseName = "BizLedger_Backup_$dateStamp"
        val csvFile = File(dir, "$baseName.csv")
        val jsonFile = File(dir, "$baseName.json")

        // 1. Build and save Excel CSV Master File
        val csvContent = buildExcelCsvBackup(
            displayDate = displayDate,
            expenses = expenses,
            withdrawals = withdrawals,
            debts = debts,
            budgets = budgets,
            users = users
        )
        csvFile.writeText(csvContent, Charsets.UTF_8)

        // 2. Build and save JSON Snapshot File
        val jsonContent = buildJsonBackup(
            timestamp = timestamp,
            displayDate = displayDate,
            expenses = expenses,
            withdrawals = withdrawals,
            debts = debts,
            budgets = budgets,
            users = users
        )
        jsonFile.writeText(jsonContent, Charsets.UTF_8)

        val totalRecords = expenses.size + withdrawals.size + debts.size + budgets.size + users.size
        val totalBytes = csvFile.length() + jsonFile.length()
        val sizeFormatted = formatFileSize(totalBytes)

        return BackupFileInfo(
            id = dateStamp,
            fileName = baseName,
            csvFileName = csvFile.name,
            jsonFileName = jsonFile.name,
            timestamp = timestamp,
            formattedDate = displayDate,
            totalRecords = totalRecords,
            fileSizeFormatted = sizeFormatted,
            csvFile = csvFile,
            jsonFile = jsonFile
        )
    }

    /**
     * Lists all saved backups on device storage, sorted newest first
     */
    fun listBackups(context: Context): List<BackupFileInfo> {
        val dir = getBackupDir(context)
        val files = dir.listFiles { _, name -> name.endsWith(".json") } ?: return emptyList()
        val sdfDisplay = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())

        return files.mapNotNull { jsonFile ->
            try {
                val baseName = jsonFile.nameWithoutExtension
                val csvFile = File(dir, "$baseName.csv")
                val jsonStr = jsonFile.readText(Charsets.UTF_8)
                val root = JSONObject(jsonStr)
                val timestamp = root.optLong("timestamp", jsonFile.lastModified())
                val displayDate = root.optString("displayDate", sdfDisplay.format(Date(timestamp)))
                val totalRecords = root.optInt("totalRecords", 0)
                val totalBytes = (if (csvFile.exists()) csvFile.length() else 0L) + jsonFile.length()

                BackupFileInfo(
                    id = baseName,
                    fileName = baseName,
                    csvFileName = if (csvFile.exists()) csvFile.name else "",
                    jsonFileName = jsonFile.name,
                    timestamp = timestamp,
                    formattedDate = displayDate,
                    totalRecords = totalRecords,
                    fileSizeFormatted = formatFileSize(totalBytes),
                    csvFile = csvFile,
                    jsonFile = jsonFile
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.timestamp }
    }

    /**
     * Deletes a backup from device
     */
    fun deleteBackup(context: Context, backup: BackupFileInfo): Boolean {
        var success = true
        if (backup.csvFile.exists()) {
            success = success && backup.csvFile.delete()
        }
        if (backup.jsonFile.exists()) {
            success = success && backup.jsonFile.delete()
        }
        return success
    }

    /**
     * Restores database data from a local JSON backup file
     */
    suspend fun restoreBackup(
        jsonFile: File,
        repository: BizLedgerRepository,
        clearExistingFirst: Boolean = false
    ): BackupRestoreResult {
        return try {
            val content = jsonFile.readText(Charsets.UTF_8)
            restoreFromJsonString(content, repository, clearExistingFirst)
        } catch (e: Exception) {
            BackupRestoreResult(
                success = false,
                message = "Failed to restore backup: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Restores database from a raw JSON string
     */
    suspend fun restoreFromJsonString(
        jsonString: String,
        repository: BizLedgerRepository,
        clearExistingFirst: Boolean = false
    ): BackupRestoreResult {
        return try {
            val root = JSONObject(jsonString)

            if (clearExistingFirst) {
                repository.clearAllLedgerData()
            }

            var expCount = 0
            var wdlCount = 0
            var dbtCount = 0
            var bdgCount = 0
            var usrCount = 0

            // 1. Restore Budgets
            val budgetsArr = root.optJSONArray("budgets")
            if (budgetsArr != null) {
                for (i in 0 until budgetsArr.length()) {
                    val obj = budgetsArr.getJSONObject(i)
                    repository.insertBudget(
                        BudgetEntity(
                            category = obj.getString("category"),
                            monthlyLimit = obj.getDouble("monthlyLimit")
                        )
                    )
                    bdgCount++
                }
            }

            // 2. Restore Expenses
            val expensesArr = root.optJSONArray("expenses")
            if (expensesArr != null) {
                for (i in 0 until expensesArr.length()) {
                    val obj = expensesArr.getJSONObject(i)
                    repository.insertExpense(
                        ExpenseEntity(
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            dateMillis = obj.getLong("dateMillis"),
                            paymentMethod = obj.optString("paymentMethod", "Cash"),
                            notes = obj.optString("notes", ""),
                            receiptTag = obj.optString("receiptTag", "")
                        )
                    )
                    expCount++
                }
            }

            // 3. Restore Withdrawals
            val withdrawalsArr = root.optJSONArray("withdrawals")
            if (withdrawalsArr != null) {
                for (i in 0 until withdrawalsArr.length()) {
                    val obj = withdrawalsArr.getJSONObject(i)
                    repository.insertWithdrawal(
                        WithdrawalEntity(
                            ownerName = obj.getString("ownerName"),
                            amount = obj.getDouble("amount"),
                            dateMillis = obj.getLong("dateMillis"),
                            paymentMethod = obj.optString("paymentMethod", "Cash"),
                            reason = obj.optString("reason", "")
                        )
                    )
                    wdlCount++
                }
            }

            // 4. Restore Debts
            val debtsArr = root.optJSONArray("debts")
            if (debtsArr != null) {
                for (i in 0 until debtsArr.length()) {
                    val obj = debtsArr.getJSONObject(i)
                    val debtTypeStr = obj.optString("type", "PAYABLE")
                    val debtType = if (debtTypeStr.contains("RECEIVABLE", ignoreCase = true)) DebtType.RECEIVABLE else DebtType.PAYABLE
                    val dueDateVal = if (obj.has("dueDateMillis") && !obj.isNull("dueDateMillis")) obj.getLong("dueDateMillis") else null

                    val debtId = repository.insertDebt(
                        DebtEntity(
                            partyName = obj.getString("partyName"),
                            type = debtType,
                            totalAmount = obj.getDouble("totalAmount"),
                            paidAmount = obj.optDouble("paidAmount", 0.0),
                            dueDateMillis = dueDateVal,
                            issueDateMillis = obj.optLong("issueDateMillis", System.currentTimeMillis()),
                            contactInfo = obj.optString("contactInfo", ""),
                            notes = obj.optString("notes", ""),
                            isSettled = obj.optBoolean("isSettled", false)
                        )
                    )
                    dbtCount++
                }
            }

            // 5. Restore Users (if present and not existing)
            val usersArr = root.optJSONArray("users")
            if (usersArr != null) {
                for (i in 0 until usersArr.length()) {
                    val obj = usersArr.getJSONObject(i)
                    val username = obj.getString("username")
                    val existing = repository.getUserByUsername(username)
                    if (existing == null) {
                        val roleStr = obj.optString("role", "USER")
                        val role = if (roleStr.equals("ADMIN", ignoreCase = true)) UserRole.ADMIN else UserRole.USER
                        val statusStr = obj.optString("status", "APPROVED")
                        val status = when (statusStr.uppercase()) {
                            "PENDING" -> UserStatus.PENDING
                            "REJECTED" -> UserStatus.REJECTED
                            else -> UserStatus.APPROVED
                        }

                        repository.registerUser(
                            UserEntity(
                                username = username,
                                fullName = obj.optString("fullName", username),
                                email = obj.optString("email", "$username@bizledger.local"),
                                password = obj.optString("password", "123456"),
                                role = role,
                                status = status,
                                createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                                approvedAtMillis = if (status == UserStatus.APPROVED) System.currentTimeMillis() else null,
                                approvedBy = obj.optString("approvedBy", "Backup Restore"),
                                notes = obj.optString("notes", "Restored from backup")
                            )
                        )
                        usrCount++
                    }
                }
            }

            BackupRestoreResult(
                success = true,
                message = "Backup restored successfully! Total restored: $expCount expenses, $wdlCount withdrawals, $dbtCount debts, $bdgCount budgets, $usrCount users.",
                expensesRestored = expCount,
                withdrawalsRestored = wdlCount,
                debtsRestored = dbtCount,
                budgetsRestored = bdgCount,
                usersRestored = usrCount
            )
        } catch (e: Exception) {
            BackupRestoreResult(
                success = false,
                message = "Error during restore: ${e.localizedMessage}"
            )
        }
    }

    /**
     * Share backup file to device (Google Drive, WhatsApp, Email, Local Files)
     */
    fun shareBackup(context: Context, file: File, title: String = "BizLedger Master Data Backup") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (file.name.endsWith(".csv")) "text/comma-separated-values" else "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "BizLedger administrative backup file: ${file.name}\nGenerated on ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share / Save Data Backup"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Open CSV backup directly in Excel / Spreadsheet viewer
     */
    fun openInExcel(context: Context, csvFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/comma-separated-values")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open in Microsoft Excel / Sheets"))
        } catch (e: Exception) {
            // Fallback to generic text view or share
            shareBackup(context, csvFile, "Open Excel Data")
        }
    }

    // ==================== BACKUP FORMATTERS ====================

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun buildExcelCsvBackup(
        displayDate: String,
        expenses: List<ExpenseEntity>,
        withdrawals: List<WithdrawalEntity>,
        debts: List<DebtEntity>,
        budgets: List<BudgetEntity>,
        users: List<UserEntity>
    ): String {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val totalExpenses = expenses.sumOf { it.amount }
        val totalWithdrawals = withdrawals.sumOf { it.amount }
        val totalReceivables = debts.filter { it.type == DebtType.RECEIVABLE }.sumOf { it.remainingAmount }
        val totalPayables = debts.filter { it.type == DebtType.PAYABLE }.sumOf { it.remainingAmount }

        return buildString {
            appendLine(escapeCsv("================================================================================"))
            appendLine(escapeCsv("BIZLEDGER MASTER ADMINISTRATIVE DATA BACKUP & EXCEL ARCHIVE"))
            appendLine(escapeCsv("Generated on: $displayDate"))
            appendLine(escapeCsv("Currency: PKR (Pakistani Rupee)"))
            appendLine(escapeCsv("Total Outflow: PKR ${"%.2f".format(totalExpenses + totalWithdrawals)} | Receivables: PKR ${"%.2f".format(totalReceivables)} | Payables: PKR ${"%.2f".format(totalPayables)}"))
            appendLine(escapeCsv("================================================================================"))
            appendLine()

            // 1. Expenses Table
            appendLine(escapeCsv("=== 1. BUSINESS EXPENSES REGISTER ==="))
            appendLine("${escapeCsv("ID")},${escapeCsv("Date & Time")},${escapeCsv("Title / Payee")},${escapeCsv("Category")},${escapeCsv("Amount (PKR)")},${escapeCsv("Payment Method")},${escapeCsv("Receipt / Invoice Ref")},${escapeCsv("Notes")}")
            if (expenses.isEmpty()) {
                appendLine("${escapeCsv("None")},${escapeCsv("-")},${escapeCsv("No expenses recorded")},${escapeCsv("-")},0.00,${escapeCsv("-")},${escapeCsv("-")},${escapeCsv("-")}")
            } else {
                expenses.sortedBy { it.dateMillis }.forEachIndexed { idx, exp ->
                    val dateStr = sdfDate.format(Date(exp.dateMillis))
                    appendLine("EXP-${idx + 1},${escapeCsv(dateStr)},${escapeCsv(exp.title)},${escapeCsv(exp.category)},${"%.2f".format(exp.amount)},${escapeCsv(exp.paymentMethod)},${escapeCsv(exp.receiptTag ?: "")},${escapeCsv(exp.notes)}")
                }
            }
            appendLine("${escapeCsv("TOTAL EXPENSES")},,,,${"%.2f".format(totalExpenses)},,,")
            appendLine()

            // 2. Owner Withdrawals Table
            appendLine(escapeCsv("=== 2. OWNER WITHDRAWALS & CAPITAL DRAWS ==="))
            appendLine("${escapeCsv("ID")},${escapeCsv("Date & Time")},${escapeCsv("Owner / Partner Name")},${escapeCsv("Amount (PKR)")},${escapeCsv("Payment Method")},${escapeCsv("Purpose / Reason")}")
            if (withdrawals.isEmpty()) {
                appendLine("${escapeCsv("None")},${escapeCsv("-")},${escapeCsv("No withdrawals recorded")},0.00,${escapeCsv("-")},${escapeCsv("-")}")
            } else {
                withdrawals.sortedBy { it.dateMillis }.forEachIndexed { idx, wdl ->
                    val dateStr = sdfDate.format(Date(wdl.dateMillis))
                    appendLine("WDL-${idx + 1},${escapeCsv(dateStr)},${escapeCsv(wdl.ownerName)},${"%.2f".format(wdl.amount)},${escapeCsv(wdl.paymentMethod)},${escapeCsv(wdl.reason)}")
                }
            }
            appendLine("${escapeCsv("TOTAL WITHDRAWALS")},,,${"%.2f".format(totalWithdrawals)},,")
            appendLine()

            // 3. Debts & Receivables Table
            appendLine(escapeCsv("=== 3. ACCOUNTS RECEIVABLE & PAYABLE REGISTRY ==="))
            appendLine("${escapeCsv("ID")},${escapeCsv("Party / Client / Vendor")},${escapeCsv("Type")},${escapeCsv("Total Amount (PKR)")},${escapeCsv("Paid Amount (PKR)")},${escapeCsv("Balance (PKR)")},${escapeCsv("Issue Date")},${escapeCsv("Due Date")},${escapeCsv("Status")},${escapeCsv("Contact")},${escapeCsv("Notes")}")
            if (debts.isEmpty()) {
                appendLine("${escapeCsv("None")},${escapeCsv("No debt records")},${escapeCsv("-")},0.00,0.00,0.00,${escapeCsv("-")},${escapeCsv("-")},${escapeCsv("-")},${escapeCsv("-")},${escapeCsv("-")}")
            } else {
                debts.forEachIndexed { idx, dbt ->
                    val typeStr = if (dbt.type == DebtType.RECEIVABLE) "RECEIVABLE (Owed to Us)" else "PAYABLE (We Owe)"
                    val issueStr = sdfDay.format(Date(dbt.issueDateMillis))
                    val dueStr = dbt.dueDateMillis?.let { sdfDay.format(Date(it)) } ?: "None"
                    val statusStr = if (dbt.isSettled) "SETTLED" else if (dbt.isOverdue) "OVERDUE" else "PENDING"
                    appendLine("DBT-${idx + 1},${escapeCsv(dbt.partyName)},${escapeCsv(typeStr)},${"%.2f".format(dbt.totalAmount)},${"%.2f".format(dbt.paidAmount)},${"%.2f".format(dbt.remainingAmount)},${escapeCsv(issueStr)},${escapeCsv(dueStr)},${escapeCsv(statusStr)},${escapeCsv(dbt.contactInfo)},${escapeCsv(dbt.notes)}")
                }
            }
            appendLine()

            // 4. Budgets Table
            appendLine(escapeCsv("=== 4. MONTHLY BUDGET ALLOCATIONS ==="))
            appendLine("${escapeCsv("Category")},${escapeCsv("Monthly Cap (PKR)")}")
            if (budgets.isEmpty()) {
                appendLine("${escapeCsv("General")},0.00")
            } else {
                budgets.forEach { b ->
                    appendLine("${escapeCsv(b.category)},${"%.2f".format(b.monthlyLimit)}")
                }
            }
            appendLine()

            // 5. Users Table
            appendLine(escapeCsv("=== 5. REGISTERED ACCOUNTS & ROLES ==="))
            appendLine("${escapeCsv("Username")},${escapeCsv("Full Name")},${escapeCsv("Email")},${escapeCsv("Role")},${escapeCsv("Status")},${escapeCsv("Created Date")}")
            users.forEach { u ->
                val dateStr = sdfDate.format(Date(u.createdAtMillis))
                appendLine("${escapeCsv(u.username)},${escapeCsv(u.fullName)},${escapeCsv(u.email)},${escapeCsv(u.role.name)},${escapeCsv(u.status.name)},${escapeCsv(dateStr)}")
            }
            appendLine()
            appendLine(escapeCsv("=== END OF BIZLEDGER BACKUP ==="))
        }
    }

    private fun buildJsonBackup(
        timestamp: Long,
        displayDate: String,
        expenses: List<ExpenseEntity>,
        withdrawals: List<WithdrawalEntity>,
        debts: List<DebtEntity>,
        budgets: List<BudgetEntity>,
        users: List<UserEntity>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "BizLedger")
        root.put("timestamp", timestamp)
        root.put("displayDate", displayDate)
        root.put("currency", "PKR")
        root.put("totalRecords", expenses.size + withdrawals.size + debts.size + budgets.size + users.size)

        // Expenses
        val expArray = JSONArray()
        expenses.forEach { e ->
            val obj = JSONObject()
            obj.put("title", e.title)
            obj.put("amount", e.amount)
            obj.put("category", e.category)
            obj.put("dateMillis", e.dateMillis)
            obj.put("paymentMethod", e.paymentMethod)
            obj.put("notes", e.notes)
            obj.put("receiptTag", e.receiptTag ?: "")
            expArray.put(obj)
        }
        root.put("expenses", expArray)

        // Withdrawals
        val wdlArray = JSONArray()
        withdrawals.forEach { w ->
            val obj = JSONObject()
            obj.put("ownerName", w.ownerName)
            obj.put("amount", w.amount)
            obj.put("dateMillis", w.dateMillis)
            obj.put("paymentMethod", w.paymentMethod)
            obj.put("reason", w.reason)
            wdlArray.put(obj)
        }
        root.put("withdrawals", wdlArray)

        // Debts
        val dbtArray = JSONArray()
        debts.forEach { d ->
            val obj = JSONObject()
            obj.put("partyName", d.partyName)
            obj.put("type", d.type.name)
            obj.put("totalAmount", d.totalAmount)
            obj.put("paidAmount", d.paidAmount)
            if (d.dueDateMillis != null) obj.put("dueDateMillis", d.dueDateMillis)
            obj.put("issueDateMillis", d.issueDateMillis)
            obj.put("contactInfo", d.contactInfo)
            obj.put("notes", d.notes)
            obj.put("isSettled", d.isSettled)
            dbtArray.put(obj)
        }
        root.put("debts", dbtArray)

        // Budgets
        val bdgArray = JSONArray()
        budgets.forEach { b ->
            val obj = JSONObject()
            obj.put("category", b.category)
            obj.put("monthlyLimit", b.monthlyLimit)
            bdgArray.put(obj)
        }
        root.put("budgets", bdgArray)

        // Users
        val usrArray = JSONArray()
        users.forEach { u ->
            val obj = JSONObject()
            obj.put("username", u.username)
            obj.put("fullName", u.fullName)
            obj.put("email", u.email)
            obj.put("password", u.password)
            obj.put("role", u.role.name)
            obj.put("status", u.status.name)
            obj.put("createdAtMillis", u.createdAtMillis)
            obj.put("approvedBy", u.approvedBy ?: "")
            obj.put("notes", u.notes)
            usrArray.put(obj)
        }
        root.put("users", usrArray)

        return root.toString(2)
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

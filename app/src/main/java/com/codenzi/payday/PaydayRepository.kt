package com.codenzi.payday

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "PaydayPrefs")

// *** YENİ: Döngü sonu özet verilerini tutmak için data class ***
data class PreviousCycleSummary(
    val totalExpenses: Double,
    val totalSavings: Double
)

@Suppress("DEPRECATION")
class PaydayRepository(private val context: Context) {

    private val prefs = context.dataStore
    private val gson = Gson()
    private val transactionDao = AppDatabase.getDatabase(context.applicationContext).transactionDao()
    private val googleDriveManager = GoogleDriveManager(context)

    companion object {
        val KEY_PAYDAY_VALUE = intPreferencesKey("payday")
        val KEY_WEEKEND_ADJUSTMENT = booleanPreferencesKey("weekend_adjustment")
        val KEY_SALARY = longPreferencesKey("salary")
        val KEY_PAY_PERIOD = stringPreferencesKey("pay_period")
        val KEY_BI_WEEKLY_REF_DATE = stringPreferencesKey("bi_weekly_ref_date")
        val KEY_SAVINGS_GOALS = stringPreferencesKey("savings_goals")
        val KEY_MONTHLY_SAVINGS = longPreferencesKey("monthly_savings")
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements")
        val KEY_FIRST_LAUNCH_DATE = stringPreferencesKey("first_launch_date")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_CONSECUTIVE_POSITIVE_CYCLES = intPreferencesKey("consecutive_positive_cycles")
        val KEY_SHOW_SIGN_IN_PROMPT = booleanPreferencesKey("show_sign_in_prompt")
        val KEY_SHOW_LOGIN_ON_START = booleanPreferencesKey("show_login_on_start")
        val KEY_AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val KEY_LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
        val KEY_AUTO_SAVING_ENABLED = booleanPreferencesKey("auto_saving_enabled")
        val KEY_LAST_PROCESSED_PAYDAY = stringPreferencesKey("last_processed_payday")
        val KEY_RESTORE_VALIDATION_NEEDED = booleanPreferencesKey("restore_validation_needed")
        val KEY_CARRY_OVER_AMOUNT = longPreferencesKey("carry_over_amount")
        val KEY_INTRO_SHOWN = booleanPreferencesKey("intro_shown")
        // YENİ EKLENEN ANAHTAR: Harcama sayacını tutmak için
        val KEY_TRANSACTION_AD_COUNTER = intPreferencesKey("transaction_ad_counter")
    }

    // YENİ FONKSİYONLAR: Reklam sayacını yönetmek için
    fun getTransactionAdCounter(): Flow<Int> = prefs.data.map { it[KEY_TRANSACTION_AD_COUNTER] ?: 0 }

    suspend fun incrementTransactionAdCounter() {
        prefs.edit {
            val currentCount = it[KEY_TRANSACTION_AD_COUNTER] ?: 0
            it[KEY_TRANSACTION_AD_COUNTER] = currentCount + 1
        }
    }

    // Diğer tüm kodlar aynı kalacak...
    suspend fun deleteAllUserData(): Boolean = withContext(Dispatchers.IO) {
        val driveSuccess = googleDriveManager.deleteBackupFile()
        if (driveSuccess) {
            clearLocalData()
        }
        return@withContext driveSuccess
    }

    suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        prefs.edit { preferences ->
            preferences.clear()
        }
    }

    fun getPayPeriod(): Flow<PayPeriod> = prefs.data.map { PayPeriod.valueOf(it[KEY_PAY_PERIOD] ?: PayPeriod.MONTHLY.name) }
    fun getPaydayValue(): Flow<Int> = prefs.data.map { it[KEY_PAYDAY_VALUE] ?: -1 }
    fun getBiWeeklyRefDateString(): Flow<String?> = prefs.data.map { it[KEY_BI_WEEKLY_REF_DATE] }
    fun getSalaryAmount(): Flow<Long> = prefs.data.map { it[KEY_SALARY] ?: 0L }
    fun isWeekendAdjustmentEnabled(): Flow<Boolean> = prefs.data.map { it[KEY_WEEKEND_ADJUSTMENT] ?: false }
    fun getMonthlySavingsAmount(): Flow<Long> = prefs.data.map { it[KEY_MONTHLY_SAVINGS] ?: 0L }
    fun isOnboardingComplete(): Flow<Boolean> = prefs.data.map { it[KEY_ONBOARDING_COMPLETE] ?: false }
    fun getFirstLaunchDate(): Flow<String?> = prefs.data.map { it[KEY_FIRST_LAUNCH_DATE] }
    fun getTheme(): Flow<String> = prefs.data.map { it[KEY_THEME] ?: "System" }
    fun getConsecutivePositiveCycles(): Flow<Int?> = prefs.data.map { it[KEY_CONSECUTIVE_POSITIVE_CYCLES] }
    fun getUnlockedAchievementIds(): Flow<Set<String>> = prefs.data.map { it[KEY_UNLOCKED_ACHIEVEMENTS] ?: emptySet() }
    fun shouldShowSignInPrompt(): Flow<Boolean> = prefs.data.map { it[KEY_SHOW_SIGN_IN_PROMPT] ?: true }
    fun shouldShowLoginOnStart(): Flow<Boolean> = prefs.data.map { it[KEY_SHOW_LOGIN_ON_START] ?: true }
    fun isAutoBackupEnabled(): Flow<Boolean> = prefs.data.map { it[KEY_AUTO_BACKUP_ENABLED] ?: false }
    fun getLastBackupTimestamp(): Flow<Long> = prefs.data.map { it[KEY_LAST_BACKUP_TIMESTAMP] ?: 0L }
    fun isAutoSavingEnabled(): Flow<Boolean> = prefs.data.map { it[KEY_AUTO_SAVING_ENABLED] ?: false }
    fun getLastProcessedPayday(): Flow<String?> = prefs.data.map { it[KEY_LAST_PROCESSED_PAYDAY] }
    fun isRestoreValidationNeeded(): Flow<Boolean> = prefs.data.map { it[KEY_RESTORE_VALIDATION_NEEDED] ?: false }
    fun getCarryOverAmount(): Flow<Long> = prefs.data.map { it[KEY_CARRY_OVER_AMOUNT] ?: 0L }
    fun hasIntroBeenShown(): Flow<Boolean> = prefs.data.map { it[KEY_INTRO_SHOWN] ?: false }

    suspend fun savePayPeriod(payPeriod: PayPeriod) = prefs.edit { it[KEY_PAY_PERIOD] = payPeriod.name }
    suspend fun saveTheme(theme: String) = prefs.edit { it[KEY_THEME] = theme }
    suspend fun savePayday(day: Int) = prefs.edit { it[KEY_PAYDAY_VALUE] = day; it.remove(KEY_BI_WEEKLY_REF_DATE) }
    suspend fun saveSalary(salary: Long) = prefs.edit { it[KEY_SALARY] = salary }
    suspend fun saveGoals(goals: List<SavingsGoal>) = prefs.edit { it[KEY_SAVINGS_GOALS] = gson.toJson(goals) }
    suspend fun setOnboardingComplete(isComplete: Boolean) = prefs.edit { it[KEY_ONBOARDING_COMPLETE] = isComplete }
    suspend fun saveBiWeeklyReferenceDate(date: LocalDate) = prefs.edit { it[KEY_BI_WEEKLY_REF_DATE] = date.format(DateTimeFormatter.ISO_LOCAL_DATE) }
    suspend fun saveMonthlySavings(amount: Long) = prefs.edit { it[KEY_MONTHLY_SAVINGS] = amount }
    suspend fun setFirstLaunchDate(date: LocalDate) = prefs.edit { it[KEY_FIRST_LAUNCH_DATE] = date.format(DateTimeFormatter.ISO_LOCAL_DATE) }
    suspend fun saveConsecutivePositiveCycles(count: Int) = prefs.edit { it[KEY_CONSECUTIVE_POSITIVE_CYCLES] = count }
    suspend fun setSignInPrompt(shouldShow: Boolean) = prefs.edit { it[KEY_SHOW_SIGN_IN_PROMPT] = shouldShow }
    suspend fun setShowLoginOnStart(shouldShow: Boolean) { prefs.edit { it[KEY_SHOW_LOGIN_ON_START] = shouldShow } }
    suspend fun setAutoBackupEnabled(isEnabled: Boolean) { prefs.edit { it[KEY_AUTO_BACKUP_ENABLED] = isEnabled } }
    suspend fun saveLastProcessedPayday(date: LocalDate) = prefs.edit { it[KEY_LAST_PROCESSED_PAYDAY] = date.format(DateTimeFormatter.ISO_LOCAL_DATE) }
    suspend fun saveAutoSavingEnabled(isEnabled: Boolean) = prefs.edit { it[KEY_AUTO_SAVING_ENABLED] = isEnabled }
    suspend fun clearRestoreValidationFlag() = prefs.edit { it.remove(KEY_RESTORE_VALIDATION_NEEDED) }
    suspend fun saveCarryOverAmount(amount: Long) = prefs.edit { it[KEY_CARRY_OVER_AMOUNT] = amount }
    suspend fun setIntroShown(hasBeenShown: Boolean) = prefs.edit { it[KEY_INTRO_SHOWN] = hasBeenShown }

    suspend fun saveLastBackupTimestamp(timestamp: Long) {
        prefs.edit { it[KEY_LAST_BACKUP_TIMESTAMP] = timestamp }
    }

    suspend fun unlockAchievement(achievementId: String) {
        prefs.edit { preferences ->
            val unlocked = preferences[KEY_UNLOCKED_ACHIEVEMENTS]?.toMutableSet() ?: mutableSetOf()
            if (unlocked.add(achievementId)) {
                preferences[KEY_UNLOCKED_ACHIEVEMENTS] = unlocked
            }
        }
    }

    suspend fun performSmartBackup() {
        if (isAutoBackupEnabled().first() && GoogleSignIn.getLastSignedInAccount(context) != null) {
            val lastBackupTimestamp = getLastBackupTimestamp().first()
            val fifteenMinutesInMillis = TimeUnit.MINUTES.toMillis(15)

            if ((System.currentTimeMillis() - lastBackupTimestamp) > fifteenMinutesInMillis) {
                try {
                    val backupData = getAllDataForBackup()
                    val backupJson = gson.toJson(backupData)
                    googleDriveManager.uploadFileContent(backupJson)
                    saveLastBackupTimestamp(System.currentTimeMillis())
                } catch (e: Exception) {
                    // Hata durumunda bir sonraki denemeyi engellememek için sessiz kal.
                }
            }
        }
    }

    fun getGoals(): Flow<MutableList<SavingsGoal>> {
        return prefs.data.map { preferences ->
            val jsonGoals = preferences[KEY_SAVINGS_GOALS]
            if (jsonGoals != null) {
                try {
                    val type = object : TypeToken<MutableList<SavingsGoal>>() {}.type
                    gson.fromJson(jsonGoals, type)
                } catch (e: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }
        }
    }

    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): Flow<List<Transaction>> = transactionDao.getTransactionsBetweenDates(startDate, endDate)

    suspend fun getPreviousCycleSummary(startDate: Date, endDate: Date): PreviousCycleSummary = withContext(Dispatchers.IO) {
        val savingsCategoryId = ExpenseCategory.getSavingsCategoryId()
        val expenses = transactionDao.getTotalExpensesBetweenDatesSuspend(startDate, endDate, savingsCategoryId) ?: 0.0
        val savings = transactionDao.getTotalSavingsBetweenDatesSuspend(startDate, endDate, savingsCategoryId) ?: 0.0
        PreviousCycleSummary(totalExpenses = expenses, totalSavings = savings)
    }

    fun getSpendingByCategoryBetweenDates(startDate: Date, endDate: Date): Flow<List<CategorySpending>> = transactionDao.getSpendingByCategoryBetweenDates(startDate, endDate, ExpenseCategory.getSavingsCategoryId())
    fun getRecurringTransactionTemplates(): Flow<List<Transaction>> = transactionDao.getRecurringTransactionTemplates()
    fun getTransactionById(id: Int): Flow<Transaction?> = transactionDao.getTransactionById(id)
    fun getDailySpendingForChart(startDate: Date, endDate: Date): Flow<List<DailySpending>> = transactionDao.getDailySpendingForChart(startDate, endDate)
    fun getMonthlySpendingForCategory(categoryId: Int): Flow<List<MonthlyCategorySpending>> = transactionDao.getMonthlySpendingForCategory(categoryId)
    fun getAllTransactionsForAchievements(): Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow()
    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insert(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.delete(transaction)

    suspend fun getAllDataForBackup(): BackupData = withContext(Dispatchers.IO) {
        val allTransactions = transactionDao.getAllTransactions()
        val goals = getGoals().first()
        val settingsMap = mutableMapOf<String, String?>()
        val prefsSnapshot = prefs.data.first()
        prefsSnapshot.asMap().forEach { (key, value) ->
            settingsMap[key.name] = value.toString()
        }
        return@withContext BackupData(
            transactions = allTransactions,
            savingsGoals = goals,
            settings = settingsMap
        )
    }

    suspend fun restoreDataFromBackup(backupData: BackupData) = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        backupData.transactions.forEach { transactionDao.insert(it) }

        var isRestorePotentiallyCorrupt = false

        prefs.edit { preferences ->
            val currentAutoBackupSetting = preferences[KEY_AUTO_BACKUP_ENABLED]
            preferences.clear()

            backupData.settings.forEach { (key, value) ->
                if (key != KEY_SAVINGS_GOALS.name && key != KEY_AUTO_BACKUP_ENABLED.name) {
                    when (key) {
                        KEY_PAYDAY_VALUE.name, KEY_CONSECUTIVE_POSITIVE_CYCLES.name, KEY_TRANSACTION_AD_COUNTER.name -> preferences[intPreferencesKey(key)] = value?.toIntOrNull() ?: 0
                        KEY_WEEKEND_ADJUSTMENT.name, KEY_ONBOARDING_COMPLETE.name, KEY_SHOW_LOGIN_ON_START.name, KEY_SHOW_SIGN_IN_PROMPT.name, KEY_AUTO_SAVING_ENABLED.name, KEY_INTRO_SHOWN.name -> preferences[booleanPreferencesKey(key)] = value?.toBoolean() ?: false
                        KEY_SALARY.name, KEY_MONTHLY_SAVINGS.name, KEY_CARRY_OVER_AMOUNT.name, KEY_LAST_BACKUP_TIMESTAMP.name -> preferences[longPreferencesKey(key)] = value?.toLongOrNull() ?: 0L
                        KEY_PAY_PERIOD.name, KEY_BI_WEEKLY_REF_DATE.name, KEY_FIRST_LAUNCH_DATE.name, KEY_LAST_PROCESSED_PAYDAY.name, KEY_THEME.name -> {
                            if (value != null) preferences[stringPreferencesKey(key)] = value
                        }
                        KEY_UNLOCKED_ACHIEVEMENTS.name -> {
                            if (value != null) {
                                val unlockedSet = value.removeSurrounding("[", "]")
                                    .split(", ")
                                    .filter { it.isNotEmpty() }
                                    .toSet()
                                preferences[stringSetPreferencesKey(key)] = unlockedSet
                            }
                        }
                    }
                }
            }
            if (backupData.savingsGoals.isNotEmpty()) {
                val goalsJson = gson.toJson(backupData.savingsGoals)
                preferences[KEY_SAVINGS_GOALS] = goalsJson
            }
            if (currentAutoBackupSetting != null) {
                preferences[KEY_AUTO_BACKUP_ENABLED] = currentAutoBackupSetting
            }

            val restoredSalary = preferences[KEY_SALARY] ?: 0L
            val restoredPayPeriod = preferences[KEY_PAY_PERIOD]

            if (restoredSalary <= 0 || restoredPayPeriod == null) {
                isRestorePotentiallyCorrupt = true
            }
        }

        if (isRestorePotentiallyCorrupt) {
            prefs.edit { it[KEY_RESTORE_VALIDATION_NEEDED] = true }
        }
    }
}
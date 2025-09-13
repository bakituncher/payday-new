package com.codenzi.payday

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

@Database(
    entities = [Transaction::class, Account::class, Category::class, Budget::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN categoryId INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN isRecurringTemplate INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create new tables
                db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `balance` REAL NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, `color` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `categoryId` INTEGER NOT NULL, `amount` REAL NOT NULL, `startDate` INTEGER NOT NULL, `endDate` INTEGER NOT NULL, `period` TEXT NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")

                // 2. Create a default account
                db.execSQL("INSERT INTO `accounts` (id, name, type, balance) VALUES (1, 'Genel Nakit', 'Nakit', 0.0)")

                // 3. Create a new transactions table with the foreign keys
                db.execSQL("""
                    CREATE TABLE transactions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount REAL NOT NULL,
                        date INTEGER NOT NULL,
                        accountId INTEGER NOT NULL,
                        categoryId INTEGER,
                        isRecurringTemplate INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE CASCADE,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL
                    )
                """)

                // 4. Copy data from old transactions table to new one
                db.execSQL("""
                    INSERT INTO transactions_new (id, name, amount, date, accountId, categoryId, isRecurringTemplate)
                    SELECT id, name, amount, date, 1, categoryId, isRecurringTemplate FROM transactions
                """)

                // 5. Drop the old table
                db.execSQL("DROP TABLE transactions")

                // 6. Rename the new table to the old table's name
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

                // 7. Populate default categories
                db.execSQL("INSERT INTO `categories` (name, icon, color) VALUES ('Diğer', 'ic_other', '#808080')")
                db.execSQL("INSERT INTO `categories` (name, icon, color) VALUES ('Fatura', 'ic_bill', '#FF6347')")
                db.execSQL("INSERT INTO `categories` (name, icon, color) VALUES ('Yiyecek', 'ic_food', '#32CD32')")
                db.execSQL("INSERT INTO `categories` (name, icon, color) VALUES ('Ulaşım', 'ic_transport', '#1E90FF')")
                db.execSQL("INSERT INTO `categories` (name, icon, color) VALUES ('Eğlence', 'ic_entertainment', '#FFD700')")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "payday_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
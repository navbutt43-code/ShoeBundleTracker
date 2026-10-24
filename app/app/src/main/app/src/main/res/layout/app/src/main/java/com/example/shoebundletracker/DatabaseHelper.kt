package com.example.shoebundletracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class BundleRecord(
    val id: Long = 0,
    val date: String,
    val made: Int,
    val incoming: Int,
    val outgoing: Int
) {
    val salary: Int get() = made * 200
}

data class ReportSummary(
    val totalMade: Int,
    val totalSalary: Int,
    val totalIncoming: Int,
    val totalOutgoing: Int
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "shoes_bundle.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "bundle_records"
        const val COL_ID = "id"
        const val COL_DATE = "record_date"
        const val COL_MADE = "bundles_made"
        const val COL_INCOMING = "bundles_incoming"
        const val COL_OUTGOING = "bundles_outgoing"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DATE TEXT UNIQUE,
                $COL_MADE INTEGER,
                $COL_INCOMING INTEGER,
                $COL_OUTGOING INTEGER
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertOrUpdateRecord(record: BundleRecord): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_DATE, record.date)
            put(COL_MADE, record.made)
            put(COL_INCOMING, record.incoming)
            put(COL_OUTGOING, record.outgoing)
        }
        return db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllRecords(): List<BundleRecord> {
        val list = mutableListOf<BundleRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COL_DATE DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val item = BundleRecord(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                    made = cursor.getInt(cursor.getColumnIndexOrThrow(COL_MADE)),
                    incoming = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INCOMING)),
                    outgoing = cursor.getInt(cursor.getColumnIndexOrThrow(COL_OUTGOING))
                )
                list.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getReportBetweenDates(startDate: String, endDate: String): ReportSummary {
        val db = readableDatabase
        val query = """
            SELECT SUM($COL_MADE) AS sum_made,
                   SUM($COL_INCOMING) AS sum_inc,
                   SUM($COL_OUTGOING) AS sum_out
            FROM $TABLE_NAME
            WHERE $COL_DATE BETWEEN ? AND ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        var totalMade = 0
        var totalInc = 0
        var totalOut = 0

        if (cursor.moveToFirst()) {
            totalMade = cursor.getInt(cursor.getColumnIndexOrThrow("sum_made"))
            totalInc = cursor.getInt(cursor.getColumnIndexOrThrow("sum_inc"))
            totalOut = cursor.getInt(cursor.getColumnIndexOrThrow("sum_out"))
        }
        cursor.close()

        return ReportSummary(
            totalMade = totalMade,
            totalSalary = totalMade * 200,
            totalIncoming = totalInc,
            totalOutgoing = totalOut
        )
    }
}

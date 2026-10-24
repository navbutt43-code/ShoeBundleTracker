package com.example.shoebundletracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        val etEntryDate = findViewById<EditText>(R.id.etEntryDate)
        val etBundlesMade = findViewById<EditText>(R.id.etBundlesMade)
        val etIncoming = findViewById<EditText>(R.id.etIncoming)
        val etOutgoing = findViewById<EditText>(R.id.etOutgoing)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val etStartDate = findViewById<EditText>(R.id.etStartDate)
        val etEndDate = findViewById<EditText>(R.id.etEndDate)
        val btnGenerateReport = findViewById<Button>(R.id.btnGenerateReport)
        val tvReportResult = findViewById<TextView>(R.id.tvReportResult)
        val tvRecordsList = findViewById<TextView>(R.id.tvRecordsList)

        etEntryDate.setText(dateFormat.format(calendar.time))

        etEntryDate.setOnClickListener { showDatePicker(etEntryDate) }
        etStartDate.setOnClickListener { showDatePicker(etStartDate) }
        etEndDate.setOnClickListener { showDatePicker(etEndDate) }

        btnSave.setOnClickListener {
            val dateStr = etEntryDate.text.toString().trim()
            val madeStr = etBundlesMade.text.toString().trim()
            val incomingStr = etIncoming.text.toString().trim()
            val outgoingStr = etOutgoing.text.toString().trim()

            if (dateStr.isEmpty() || madeStr.isEmpty()) {
                Toast.makeText(this, "Please enter Date and Bundles Made", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val made = madeStr.toIntOrNull() ?: 0
            val incoming = incomingStr.toIntOrNull() ?: 0
            val outgoing = outgoingStr.toIntOrNull() ?: 0

            val record = BundleRecord(
                date = dateStr,
                made = made,
                incoming = incoming,
                outgoing = outgoing
            )

            dbHelper.insertOrUpdateRecord(record)
            Toast.makeText(this, "Saved! Salary earned: Rs. ${record.salary}", Toast.LENGTH_LONG).show()

            etBundlesMade.text.clear()
            etIncoming.text.clear()
            etOutgoing.text.clear()

            loadRecordsLog(tvRecordsList)
        }

        btnGenerateReport.setOnClickListener {
            val start = etStartDate.text.toString().trim()
            val end = etEndDate.text.toString().trim()

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(this, "Please select both Start Date and End Date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val report = dbHelper.getReportBetweenDates(start, end)
            tvReportResult.visibility = View.VISIBLE
            tvReportResult.text = """
                📊 Report Summary ($start to $end)
                ------------------------------------------------
                Total Bundles Made: ${report.totalMade}
                💰 Total Salary Earned: Rs. ${report.totalSalary}
                📥 Total Incoming: ${report.totalIncoming}
                📤 Total Outgoing: ${report.totalOutgoing}
            """.trimIndent()
        }

        loadRecordsLog(tvRecordsList)
    }

    private fun showDatePicker(targetEditText: EditText) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, y, m, d ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(y, m, d)
            targetEditText.setText(dateFormat.format(selectedCalendar.time))
        }, year, month, day).show()
    }

    private fun loadRecordsLog(textView: TextView) {
        val records = dbHelper.getAllRecords()
        if (records.isEmpty()) {
            textView.text = "No records added yet."
            return
        }

        val sb = StringBuilder()
        for (r in records) {
            sb.append("📅 Date: ${r.date}\n")
            sb.append("• Made: ${r.made} bundles | Salary: Rs. ${r.salary}\n")
            sb.append("• Incoming: ${r.incoming} | Outgoing: ${r.outgoing}\n")
            sb.append("--------------------------------------------------\n")
        }
        textView.text = sb.toString()
    }
}

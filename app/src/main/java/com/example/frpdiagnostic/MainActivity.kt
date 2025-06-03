package com.example.frpdiagnostic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvFrpStatus: TextView
    private lateinit var tvGoogleAccounts: TextView
    private lateinit var tvPartitionInfo: TextView
    private lateinit var tvBootloaderStatus: TextView
    private lateinit var tvAdbResult: TextView
    private lateinit var etAdbCommand: EditText

    private lateinit var btnCheckFrp: Button
    private lateinit var btnRemoveFrp: Button
    private lateinit var btnCheckAccounts: Button
    private lateinit var btnCheckPartitions: Button
    private lateinit var btnCheckBootloader: Button
    private lateinit var btnExecuteAdb: Button

    private val frpChecker = FrpChecker()
    private val accountReader = AccountReader()
    private val partitionInfo = PartitionInfo()
    private val bootloaderChecker = BootloaderChecker()
    private val adbExecutor = AdbExecutor()

    private val PERMISSIONS_REQUEST_CODE = 100
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
        checkPermissions()
    }

    private fun initViews() {
        tvFrpStatus = findViewById(R.id.tvFrpStatus)
        tvGoogleAccounts = findViewById(R.id.tvGoogleAccounts)
        tvPartitionInfo = findViewById(R.id.tvPartitionInfo)
        tvBootloaderStatus = findViewById(R.id.tvBootloaderStatus)
        tvAdbResult = findViewById(R.id.tvAdbResult)
        etAdbCommand = findViewById(R.id.etAdbCommand)

        btnCheckFrp = findViewById(R.id.btnCheckFrp)
        btnRemoveFrp = findViewById(R.id.btnRemoveFrp)
        btnCheckAccounts = findViewById(R.id.btnCheckAccounts)
        btnCheckPartitions = findViewById(R.id.btnCheckPartitions)
        btnCheckBootloader = findViewById(R.id.btnCheckBootloader)
        btnExecuteAdb = findViewById(R.id.btnExecuteAdb)
    }

    private fun setupClickListeners() {
        btnCheckFrp.setOnClickListener {
            val frpStatus = frpChecker.checkFrpStatus(this)
            tvFrpStatus.text = frpStatus
        }

        btnRemoveFrp.setOnClickListener {
            val result = frpChecker.removeFrp(this)
            Toast.makeText(this, result, Toast.LENGTH_LONG).show()
            // Refresh FRP status after removal attempt
            val frpStatus = frpChecker.checkFrpStatus(this)
            tvFrpStatus.text = frpStatus
        }

        btnCheckAccounts.setOnClickListener {
            if (hasPermissions()) {
                val accounts = accountReader.getGoogleAccounts(this)
                tvGoogleAccounts.text = accounts
            } else {
                requestPermissions()
            }
        }

        btnCheckPartitions.setOnClickListener {
            val partitionsData = partitionInfo.getPartitionInfo()
            tvPartitionInfo.text = partitionsData
        }

        btnCheckBootloader.setOnClickListener {
            val bootloaderStatus = bootloaderChecker.checkBootloaderStatus()
            tvBootloaderStatus.text = bootloaderStatus
        }

        btnExecuteAdb.setOnClickListener {
            val command = etAdbCommand.text.toString()
            if (command.isNotEmpty()) {
                val result = adbExecutor.executeCommand(command)
                tvAdbResult.text = result
            } else {
                Toast.makeText(this, "Please enter a command", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        if (!hasPermissions()) {
            requestPermissions()
        }
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
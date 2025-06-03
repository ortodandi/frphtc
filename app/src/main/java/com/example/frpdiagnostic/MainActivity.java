package com.example.frpdiagnostic;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 100;
    private static final String TAG = "FRPDiagnostic";
    
    private TextView tvGoogleAccounts;
    private TextView tvFrpStatus;
    private TextView tvPartitions;
    private TextView tvBootloader;
    private TextView tvCommandOutput;
    private EditText etCommand;
    private Button btnExecute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        tvGoogleAccounts = findViewById(R.id.tv_google_accounts);
        tvFrpStatus = findViewById(R.id.tv_frp_status);
        tvPartitions = findViewById(R.id.tv_partitions);
        tvBootloader = findViewById(R.id.tv_bootloader);
        tvCommandOutput = findViewById(R.id.tv_command_output);
        etCommand = findViewById(R.id.et_command);
        btnExecute = findViewById(R.id.btn_execute);
        
        // Set up button click listeners
        findViewById(R.id.btn_check_accounts).setOnClickListener(v -> checkGoogleAccounts());
        findViewById(R.id.btn_check_frp).setOnClickListener(v -> checkFrpStatus());
        findViewById(R.id.btn_check_partitions).setOnClickListener(v -> checkPartitions());
        findViewById(R.id.btn_check_bootloader).setOnClickListener(v -> checkBootloader());
        btnExecute.setOnClickListener(v -> executeCommand());
        
        // Request necessary permissions
        requestPermissions();
    }
    
    private void requestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                    permissionsNeeded.toArray(new String[0]), 
                    REQUEST_PERMISSIONS);
        }
    }
    
    private void checkGoogleAccounts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) 
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission denied. Cannot access accounts.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder accountInfo = new StringBuilder();
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();
        
        accountInfo.append("Total accounts: ").append(accounts.length).append("\n\n");
        
        for (Account account : accounts) {
            if (account.type.equals("com.google")) {
                accountInfo.append("Google Account: ").append(account.name).append("\n");
            } else {
                accountInfo.append("Account: ").append(account.name)
                        .append(" (").append(account.type).append(")\n");
            }
        }
        
        tvGoogleAccounts.setText(accountInfo.toString());
    }
    
    private void checkFrpStatus() {
        StringBuilder frpInfo = new StringBuilder();
        
        // Check if device has FRP enabled
        boolean hasFrp = false;
        
        try {
            // Check for FRP properties
            String frpProp = executeShellCommand("getprop ro.frp.pst");
            if (frpProp != null && !frpProp.trim().isEmpty()) {
                hasFrp = true;
                frpInfo.append("FRP Partition: ").append(frpProp.trim()).append("\n");
            }
            
            // Check for persist.sys.usb.config property
            String usbConfig = executeShellCommand("getprop persist.sys.usb.config");
            frpInfo.append("USB Config: ").append(usbConfig.trim()).append("\n");
            
            // Check for factory reset protection flag
            String frpFlag = executeShellCommand("getprop ro.boot.verifiedbootstate");
            frpInfo.append("Verified Boot State: ").append(frpFlag.trim()).append("\n");
            
            // Check for FRP status in settings database
            String frpSetting = executeShellCommand("settings get secure secure_frp_mode");
            frpInfo.append("Secure FRP Mode: ").append(frpSetting.trim()).append("\n");
            
            frpInfo.append("\nFRP Status: ").append(hasFrp ? "ENABLED" : "DISABLED");
            
        } catch (Exception e) {
            frpInfo.append("Error checking FRP status: ").append(e.getMessage());
            Log.e(TAG, "Error checking FRP status", e);
        }
        
        tvFrpStatus.setText(frpInfo.toString());
    }
    
    private void checkPartitions() {
        StringBuilder partitionInfo = new StringBuilder();
        
        try {
            // List critical partitions
            String[] partitionsToCheck = {"misc", "fsg", "cid", "frp", "persist"};
            
            for (String partition : partitionsToCheck) {
                String partitionPath = "/dev/block/bootdevice/by-name/" + partition;
                String result = executeShellCommand("ls -la " + partitionPath);
                partitionInfo.append(partition).append(": ");
                
                if (result.contains("No such file")) {
                    partitionInfo.append("Not found\n");
                } else {
                    partitionInfo.append("Found\n");
                    partitionInfo.append(result).append("\n");
                }
            }
            
            // Get partition table
            partitionInfo.append("\nPartition Table:\n");
            partitionInfo.append(executeShellCommand("cat /proc/partitions"));
            
        } catch (Exception e) {
            partitionInfo.append("Error checking partitions: ").append(e.getMessage());
            Log.e(TAG, "Error checking partitions", e);
        }
        
        tvPartitions.setText(partitionInfo.toString());
    }
    
    private void checkBootloader() {
        StringBuilder bootloaderInfo = new StringBuilder();
        
        try {
            // Check bootloader status
            String bootloaderState = executeShellCommand("getprop ro.boot.verifiedbootstate");
            bootloaderInfo.append("Verified Boot State: ").append(bootloaderState.trim()).append("\n");
            
            String secureBootState = executeShellCommand("getprop ro.boot.secureboot");
            bootloaderInfo.append("Secure Boot: ").append(secureBootState.trim()).append("\n");
            
            String unlockAbility = executeShellCommand("getprop ro.oem_unlock_supported");
            bootloaderInfo.append("OEM Unlock Supported: ").append(unlockAbility.trim()).append("\n");
            
            // Check for HTC specific bootloader properties
            String htcUnlockToken = executeShellCommand("getprop ro.htc.unlock_token");
            if (htcUnlockToken != null && !htcUnlockToken.trim().isEmpty()) {
                bootloaderInfo.append("HTC Unlock Token: ").append(htcUnlockToken.trim()).append("\n");
            }
            
            // Determine bootloader status
            boolean isLocked = true;
            if (bootloaderState.contains("orange") || bootloaderState.contains("unlocked")) {
                isLocked = false;
            }
            
            bootloaderInfo.append("\nBootloader Status: ").append(isLocked ? "LOCKED" : "UNLOCKED");
            
        } catch (Exception e) {
            bootloaderInfo.append("Error checking bootloader: ").append(e.getMessage());
            Log.e(TAG, "Error checking bootloader", e);
        }
        
        tvBootloader.setText(bootloaderInfo.toString());
    }
    
    private void executeCommand() {
        String command = etCommand.getText().toString().trim();
        
        if (command.isEmpty()) {
            Toast.makeText(this, "Please enter a command", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String result = executeShellCommand(command);
            tvCommandOutput.setText(result);
        } catch (Exception e) {
            tvCommandOutput.setText("Error executing command: " + e.getMessage());
            Log.e(TAG, "Error executing command", e);
        }
    }
    
    private String executeShellCommand(String command) {
        StringBuilder output = new StringBuilder();
        
        try {
            Process process;
            
            // Try to execute with root if available
            try {
                process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            } catch (Exception e) {
                // Fall back to non-root execution
                process = Runtime.getRuntime().exec(command);
            }
            
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            while ((line = errorReader.readLine()) != null) {
                output.append("Error: ").append(line).append("\n");
            }
            
            process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            output.append("Exception: ").append(e.getMessage());
            Log.e(TAG, "Error executing shell command", e);
        }
        
        return output.toString();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Some permissions were denied. Functionality may be limited.", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}

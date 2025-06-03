package com.example.frpdiagnostic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Build;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    
    private TextView tvStatus;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvStatus = findViewById(R.id.tvStatus);
        Button btnCheckFRP = findViewById(R.id.btnCheckFRP);
        Button btnRootFRP = findViewById(R.id.btnRootFRP);
        
        btnCheckFRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFRPStatus();
            }
        });
        
        btnRootFRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RootFRPActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void checkFRPStatus() {
        tvStatus.setText("FRP Status: Checking...");
        
        StringBuilder status = new StringBuilder();
        
        // Verificar cuentas de Google
        status.append("Google Accounts:\n");
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccounts();
        boolean hasGoogleAccount = false;
        
        for (Account account : accounts) {
            if (account.type.equals("com.google")) {
                status.append("- ").append(account.name).append("\n");
                hasGoogleAccount = true;
            }
        }
        
        if (!hasGoogleAccount) {
            status.append("No Google accounts found\n");
        }
        
        // Verificar estado de FRP
        boolean frpActive = checkFRPActive();
        status.append("\nFRP Status: ").append(frpActive ? "Active" : "Inactive").append("\n");
        
        // Verificar estado del bootloader
        String bootloaderStatus = checkBootloaderStatus();
        status.append("Bootloader: ").append(bootloaderStatus).append("\n");
        
        // Información de particiones
        status.append("\nPartition Info:\n");
        String partitionInfo = getPartitionInfo();
        status.append(partitionInfo);
        
        tvStatus.setText(status.toString());
    }
    
    private boolean checkFRPActive() {
        try {
            // Verificar si existe el archivo de persistencia de FRP
            File frpFile = new File("/persist/frp/persistent_data_block.bin");
            if (frpFile.exists()) {
                return true;
            }
            
            // Verificar mediante propiedades del sistema
            String frpStatus = executeCommand("getprop ro.frp.pst");
            return frpStatus != null && !frpStatus.isEmpty() && !frpStatus.equals("unknown");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private String checkBootloaderStatus() {
        try {
            String status = executeCommand("getprop ro.boot.verifiedbootstate");
            if (status.contains("orange")) {
                return "Unlocked";
            } else if (status.contains("green")) {
                return "Locked";
            }
            
            // Método alternativo
            status = executeCommand("getprop ro.boot.secure_hardware");
            if (status.equals("0")) {
                return "Unlocked";
            } else if (status.equals("1")) {
                return "Locked";
            }
            
            return "Unknown";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error checking bootloader";
        }
    }
    
    private String getPartitionInfo() {
        StringBuilder info = new StringBuilder();
        try {
            // Listar particiones importantes
            String[] partitions = {"misc", "fsg", "cid", "persist"};
            for (String partition : partitions) {
                String output = executeCommand("ls -la /dev/block/bootdevice/by-name/" + partition);
                info.append(partition).append(": ").append(output).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            info.append("Error reading partition info");
        }
        return info.toString();
    }
    
    private String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            process.waitFor();
            return output.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing command: " + e.getMessage();
        }
    }
}
package com.example.frpdiagnostic

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Class to check and manipulate Factory Reset Protection (FRP) status
 * Specifically designed for HTC One M9 running Android 7.0 Nougat
 */
class FrpChecker {

    /**
     * Check if FRP is active on the device
     * @param context Application context
     * @return String describing FRP status
     */
    fun checkFrpStatus(context: Context): String {
        val stringBuilder = StringBuilder()
        
        try {
            // Method 1: Check using Settings.Global
            val frpSetting = Settings.Global.getInt(context.contentResolver, "frp_credential_enabled", 0)
            stringBuilder.append("FRP Setting: ${if (frpSetting == 1) "Enabled" else "Disabled"}\n")
            
            // Method 2: Check for Google accounts (FRP is typically active if Google accounts exist)
            val accountReader = AccountReader()
            val hasGoogleAccounts = accountReader.hasGoogleAccounts(context)
            stringBuilder.append("Google Accounts: ${if (hasGoogleAccounts) "Present" else "Not present"}\n")
            
            // Method 3: Check specific files in the system that might indicate FRP status
            if (isRooted()) {
                // Check for FRP partition or file
                val frpPartition = checkFrpPartition()
                stringBuilder.append("FRP Partition: $frpPartition\n")
                
                // Check for persist.sys.usb.config property (may indicate FRP status)
                val usbConfig = executeRootCommand("getprop persist.sys.usb.config")
                stringBuilder.append("USB Config: $usbConfig\n")
            } else {
                stringBuilder.append("Root access required for detailed FRP partition check\n")
            }
            
            // Final determination based on collected data
            val isFrpActive = frpSetting == 1 || hasGoogleAccounts
            stringBuilder.append("\nFRP Status: ${if (isFrpActive) "ACTIVE" else "INACTIVE"}")
            
        } catch (e: Exception) {
            stringBuilder.append("Error checking FRP status: ${e.message}")
        }
        
        return stringBuilder.toString()
    }
    
    /**
     * Attempt to remove FRP from the device (requires root)
     * @param context Application context
     * @return Result of the operation
     */
    fun removeFrp(context: Context): String {
        if (!isRooted()) {
            return "Root access required to remove FRP"
        }
        
        val stringBuilder = StringBuilder()
        
        try {
            // Method 1: Disable FRP setting
            val result1 = executeRootCommand("settings put global frp_credential_enabled 0")
            stringBuilder.append("Disabled FRP setting: $result1\n")
            
            // Method 2: Clear FRP partition (HTC specific)
            // For HTC One M9, the FRP data might be stored in the misc partition
            val result2 = executeRootCommand("dd if=/dev/zero of=/dev/block/bootdevice/by-name/misc bs=256 count=1")
            stringBuilder.append("Cleared misc partition: $result2\n")
            
            // Method 3: Remove persist properties related to FRP
            val result3 = executeRootCommand("setprop persist.sys.usb.config mtp,adb")
            stringBuilder.append("Reset USB config: $result3\n")
            
            // Method 4: Remove Google accounts (optional, more invasive)
            // This is commented out as it's a more invasive approach
            // val result4 = executeRootCommand("rm -rf /data/system/users/0/accounts*")
            // stringBuilder.append("Removed account data: $result4\n")
            
            stringBuilder.append("\nFRP removal operations completed. Please reboot the device.")
            
        } catch (e: Exception) {
            stringBuilder.append("Error removing FRP: ${e.message}")
        }
        
        return stringBuilder.toString()
    }
    
    /**
     * Check FRP partition status
     * @return Description of FRP partition status
     */
    private fun checkFrpPartition(): String {
        // For HTC One M9, FRP data is typically stored in the misc partition
        val miscPartition = File("/dev/block/bootdevice/by-name/misc")
        
        if (!miscPartition.exists()) {
            return "Misc partition not found"
        }
        
        // Check if the partition contains FRP data
        // This is a simplified check - in reality, you'd need to analyze the partition content
        val result = executeRootCommand("hexdump -n 256 -C /dev/block/bootdevice/by-name/misc")
        
        return if (result.contains("00000000")) {
            "Misc partition appears to be empty (FRP likely inactive)"
        } else {
            "Misc partition contains data (FRP may be active)"
        }
    }
    
    /**
     * Check if the device is rooted
     * @return true if the device has root access
     */
    fun isRooted(): Boolean {
        // Check for su binary
        val suProcess = Runtime.getRuntime().exec("which su")
        val reader = BufferedReader(InputStreamReader(suProcess.inputStream))
        val output = reader.readLine()
        reader.close()
        
        return output != null && output.isNotEmpty()
    }
    
    /**
     * Execute a command with root privileges
     * @param command Command to execute
     * @return Output of the command
     */
    private fun executeRootCommand(command: String): String {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val error = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            while (error.readLine().also { line = it } != null) {
                output.append("Error: ").append(line).append("\n")
            }
            
            process.waitFor()
            return output.toString().trim()
            
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }
}
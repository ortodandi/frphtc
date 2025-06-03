package com.example.frpdiagnostic

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Class to check the bootloader status of the device
 * Specifically designed for HTC One M9 running Android 7.0 Nougat
 */
class BootloaderChecker {

    /**
     * Check if the bootloader is locked or unlocked
     * @return String describing bootloader status
     */
    fun checkBootloaderStatus(): String {
        val frpChecker = FrpChecker()
        
        if (!frpChecker.isRooted()) {
            return "Root access required to check bootloader status"
        }
        
        val stringBuilder = StringBuilder()
        
        try {
            // Method 1: Check ro.bootloader property
            val bootloaderProp = executeRootCommand("getprop ro.bootloader")
            stringBuilder.append("Bootloader version: $bootloaderProp\n")
            
            // Method 2: Check secure boot status
            val secureBootStatus = executeRootCommand("getprop ro.boot.secure_hardware")
            stringBuilder.append("Secure boot: $secureBootStatus\n")
            
            // Method 3: Check if device is tampered
            val tamperedStatus = executeRootCommand("getprop ro.boot.warranty_bit")
            stringBuilder.append("Tampered status: $tamperedStatus\n")
            
            // Method 4: Check unlock status (HTC specific)
            val unlockStatus = executeRootCommand("getprop ro.boot.unlocked")
            stringBuilder.append("Unlock status: $unlockStatus\n")
            
            // Method 5: Check for S-OFF/S-ON status (HTC specific)
            val sOffStatus = checkSOffStatus()
            stringBuilder.append("S-OFF status: $sOffStatus\n")
            
            // Determine overall bootloader status
            val isUnlocked = unlockStatus == "1" || tamperedStatus == "1" || sOffStatus.contains("S-OFF")
            
            stringBuilder.append("\nBootloader status: ${if (isUnlocked) "UNLOCKED" else "LOCKED"}")
            
        } catch (e: Exception) {
            stringBuilder.append("Error checking bootloader status: ${e.message}")
        }
        
        return stringBuilder.toString()
    }
    
    /**
     * Check S-OFF/S-ON status (HTC specific security feature)
     * @return String describing S-OFF/S-ON status
     */
    private fun checkSOffStatus(): String {
        try {
            // Try to read the HTC specific flag
            // This might be in different locations depending on the exact model
            val result = executeRootCommand("cat /proc/cmdline | grep -i s-off")
            
            if (result.contains("s-off", ignoreCase = true)) {
                return "S-OFF (Security disabled)"
            } else {
                // Try alternative method
                val htcVariableResult = executeRootCommand("cat /sys/class/htc_sysinfo/htc_magic")
                
                if (htcVariableResult.contains("s-off", ignoreCase = true)) {
                    return "S-OFF (Security disabled)"
                }
            }
            
            return "S-ON (Security enabled)"
            
        } catch (e: Exception) {
            return "Unable to determine S-OFF status: ${e.message}"
        }
    }
    
    /**
     * Get detailed bootloader information
     * @return String containing detailed bootloader information
     */
    fun getDetailedBootloaderInfo(): String {
        val frpChecker = FrpChecker()
        
        if (!frpChecker.isRooted()) {
            return "Root access required for detailed bootloader information"
        }
        
        val stringBuilder = StringBuilder()
        
        try {
            // Get all bootloader related properties
            stringBuilder.append("Bootloader Properties:\n")
            val bootProps = executeRootCommand("getprop | grep -E 'bootloader|secure|unlock|tamper|warranty'")
            stringBuilder.append(bootProps).append("\n\n")
            
            // Check for custom recovery
            stringBuilder.append("Recovery Information:\n")
            val recoveryInfo = executeRootCommand("getprop | grep recovery")
            stringBuilder.append(recoveryInfo).append("\n\n")
            
            // Check for OEM unlock option
            stringBuilder.append("OEM Unlock Status:\n")
            val oemUnlock = executeRootCommand("settings get global oem_unlock_enabled")
            stringBuilder.append("OEM unlock enabled: $oemUnlock\n\n")
            
            // Check for verified boot status
            stringBuilder.append("Verified Boot Status:\n")
            val verifiedBoot = executeRootCommand("getprop ro.boot.verifiedbootstate")
            stringBuilder.append("Verified boot state: $verifiedBoot\n\n")
            
        } catch (e: Exception) {
            stringBuilder.append("Error getting detailed bootloader info: ${e.message}")
        }
        
        return stringBuilder.toString()
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
package com.example.frpdiagnostic

import android.accounts.AccountManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import java.io.BufferedReader

/**
 * Class to read Google accounts associated with the device
 */
class AccountReader {

    /**
     * Get all Google accounts on the device
     * @param context Application context
     * @return String containing all Google accounts or error message
     */
    fun getGoogleAccounts(context: Context): String {
        // Check for necessary permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) 
            != PackageManager.PERMISSION_GRANTED) {
            return "Permission GET_ACCOUNTS not granted"
        }
        
        try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            
            if (accounts.isEmpty()) {
                return "No Google accounts found on this device"
            }
            
            val stringBuilder = StringBuilder()
            stringBuilder.append("Google Accounts (${accounts.size}):\n")
            
            accounts.forEachIndexed { index, account ->
                stringBuilder.append("${index + 1}. ${account.name}\n")
            }
            
            return stringBuilder.toString()
            
        } catch (e: Exception) {
            return "Error reading accounts: ${e.message}"
        }
    }
    
    /**
     * Check if the device has any Google accounts
     * @param context Application context
     * @return true if Google accounts exist, false otherwise
     */
    fun hasGoogleAccounts(context: Context): Boolean {
        // Check for necessary permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) 
            != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        
        try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            return accounts.isNotEmpty()
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Get detailed account information (requires root)
     * @return String containing detailed account information
     */
    fun getDetailedAccountInfo(): String {
        val frpChecker = FrpChecker()
        
        if (!frpChecker.isRooted()) {
            return "Root access required for detailed account information"
        }
        
        try {
            // Execute commands to get account information from system files
            val command1 = "ls -la /data/system/users/0/accounts*"
            val command2 = "cat /data/system/users/0/accounts.db"
            
            val process1 = Runtime.getRuntime().exec(arrayOf("su", "-c", command1))
            val reader1 = BufferedReader(java.io.InputStreamReader(process1.inputStream))
            
            val output = StringBuilder()
            output.append("Account files:\n")
            
            var line: String?
            while (reader1.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            // Note: Reading accounts.db directly might not work due to SQLite format
            // This is just a demonstration of what could be done with root access
            
            return output.toString()
            
        } catch (e: Exception) {
            return "Error getting detailed account info: ${e.message}"
        }
    }
}
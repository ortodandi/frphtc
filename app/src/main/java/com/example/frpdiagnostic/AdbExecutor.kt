package com.example.frpdiagnostic

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Class to execute ADB commands on the device
 * Requires root access
 */
class AdbExecutor {

    /**
     * Execute an ADB command
     * @param command ADB command to execute
     * @return Output of the command
     */
    fun executeCommand(command: String): String {
        val frpChecker = FrpChecker()
        
        if (!frpChecker.isRooted()) {
            return "Root access required to execute ADB commands"
        }
        
        // Sanitize the command to prevent command injection
        val sanitizedCommand = sanitizeCommand(command)
        
        if (sanitizedCommand.isEmpty()) {
            return "Invalid command"
        }
        
        return try {
            // Execute the command with root privileges
            val fullCommand = if (sanitizedCommand.startsWith("adb ")) {
                sanitizedCommand
            } else {
                "adb $sanitizedCommand"
            }
            
            executeRootCommand(fullCommand)
        } catch (e: Exception) {
            "Error executing command: ${e.message}"
        }
    }
    
    /**
     * Execute a shell command with ADB
     * @param command Shell command to execute
     * @return Output of the command
     */
    fun executeShellCommand(command: String): String {
        return executeCommand("shell $command")
    }
    
    /**
     * Get device information using ADB
     * @return String containing device information
     */
    fun getDeviceInfo(): String {
        val stringBuilder = StringBuilder()
        
        try {
            // Get device information
            stringBuilder.append("Device Information:\n")
            stringBuilder.append(executeCommand("devices -l")).append("\n\n")
            
            // Get device properties
            stringBuilder.append("Device Properties:\n")
            stringBuilder.append(executeShellCommand("getprop")).append("\n\n")
            
            // Get package list
            stringBuilder.append("Installed Packages:\n")
            stringBuilder.append(executeShellCommand("pm list packages -f | grep google")).append("\n\n")
            
        } catch (e: Exception) {
            stringBuilder.append("Error getting device info: ${e.message}")
        }
        
        return stringBuilder.toString()
    }
    
    /**
     * Sanitize command to prevent command injection
     * @param command Command to sanitize
     * @return Sanitized command
     */
    private fun sanitizeCommand(command: String): String {
        // Remove potentially dangerous characters and commands
        val dangerousPatterns = arrayOf(
            ";", "&&", "||", "`", "$", "\\", "\"", "'", ">", "<", "|", "rm", "mkfs", "dd", "format"
        )
        
        var sanitized = command
        
        for (pattern in dangerousPatterns) {
            if (sanitized.contains(pattern)) {
                // If it's a dangerous command that could damage the device, reject it
                if (pattern in arrayOf("rm", "mkfs", "dd", "format")) {
                    if (isDestructiveCommand(sanitized, pattern)) {
                        return "Potentially destructive command rejected"
                    }
                } else {
                    // For shell metacharacters, just remove them
                    sanitized = sanitized.replace(pattern, "")
                }
            }
        }
        
        return sanitized.trim()
    }
    
    /**
     * Check if a command is potentially destructive
     * @param command Command to check
     * @param pattern Dangerous pattern found in the command
     * @return true if the command is potentially destructive
     */
    private fun isDestructiveCommand(command: String, pattern: String): Boolean {
        // Check if the command is using the dangerous pattern in a way that could be destructive
        val lowercaseCommand = command.toLowerCase()
        
        return when (pattern) {
            "rm" -> lowercaseCommand.contains("rm -rf") || lowercaseCommand.contains("rm -r")
            "dd" -> lowercaseCommand.contains("of=") // Writing to a device with dd
            "mkfs" -> true // Any mkfs command is potentially destructive
            "format" -> true // Any format command is potentially destructive
            else -> false
        }
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
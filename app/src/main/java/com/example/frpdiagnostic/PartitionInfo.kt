package com.example.frpdiagnostic

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Class to get information about critical partitions on the device
 * Specifically designed for HTC One M9 running Android 7.0 Nougat
 */
class PartitionInfo {

    /**
     * Get information about critical partitions
     * @return String containing partition information
     */
    fun getPartitionInfo(): String {
        val frpChecker = FrpChecker()
        
        if (!frpChecker.isRooted()) {
            return "Root access required to access partition information"
        }
        
        val stringBuilder = StringBuilder()
        
        try {
            // List all block devices
            stringBuilder.append("Block Devices:\n")
            val blockDevices = executeRootCommand("ls -la /dev/block/bootdevice/by-name/")
            stringBuilder.append(blockDevices).append("\n\n")
            
            // Check specific critical partitions for FRP
            val criticalPartitions = arrayOf("misc", "fsg", "cid", "persist", "frp")
            
            stringBuilder.append("Critical Partitions:\n")
            
            for (partition in criticalPartitions) {
                val partitionPath = "/dev/block/bootdevice/by-name/$partition"
                val partitionFile = File(partitionPath)
                
                if (partitionFile.exists()) {
                    // Get partition size
                    val size = executeRootCommand("blockdev --getsize64 $partitionPath")
                    
                    // Get first few bytes to check content (safely)
                    val content = executeRootCommand("hexdump -n 32 -C $partitionPath")
                    
                    stringBuilder.append("$partition: Exists, Size: $size bytes\n")
                    stringBuilder.append("Content sample: \n$content\n\n")
                } else {
                    stringBuilder.append("$partition: Not found\n\n")
                }
            }
            
            // Get mount points
            stringBuilder.append("Mount Points:\n")
            val mountPoints = executeRootCommand("mount | grep -E 'system|data|cache'")
            stringBuilder.append(mountPoints).append("\n\n")
            
            // Get partition table
            stringBuilder.append("Partition Table:\n")
            val partitionTable = executeRootCommand("cat /proc/partitions")
            stringBuilder.append(partitionTable)
            
        } catch (e: Exception) {
            stringBuilder.append("Error getting partition info: ${e.message}")
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
    
    /**
     * Get information about a specific partition
     * @param partitionName Name of the partition
     * @return String containing detailed information about the partition
     */
    fun getSpecificPartitionInfo(partitionName: String): String {
        val frpChecker = FrpChecker()
        
        if (!frpChecker.isRooted()) {
            return "Root access required to access partition information"
        }
        
        val partitionPath = "/dev/block/bootdevice/by-name/$partitionName"
        val partitionFile = File(partitionPath)
        
        if (!partitionFile.exists()) {
            return "Partition $partitionName not found"
        }
        
        val stringBuilder = StringBuilder()
        
        try {
            // Get partition size
            val size = executeRootCommand("blockdev --getsize64 $partitionPath")
            stringBuilder.append("Size: $size bytes\n\n")
            
            // Get content sample
            val content = executeRootCommand("hexdump -n 256 -C $partitionPath")
            stringBuilder.append("Content sample: \n$content\n\n")
            
            // Get filesystem type if mounted
            val fsType = executeRootCommand("mount | grep $partitionPath")
            if (fsType.isNotEmpty()) {
                stringBuilder.append("Filesystem: $fsType\n\n")
            } else {
                stringBuilder.append("Not mounted\n\n")
            }
            
        } catch (e: Exception) {
            stringBuilder.append("Error getting partition info: ${e.message}")
        }
        
        return stringBuilder.toString()
    }
}
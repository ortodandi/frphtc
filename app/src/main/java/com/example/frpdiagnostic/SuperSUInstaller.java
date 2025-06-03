package com.example.frpdiagnostic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Clase para instalar SuperSU en el dispositivo
 */
public class SuperSUInstaller {
    private static final String TAG = "SuperSUInstaller";
    private Context context;
    private InstallCallback callback;

    // Interfaz para manejar callbacks de la instalación
    public interface InstallCallback {
        void onSuccess(String message);
        void onFailed(String error);
        void onProgress(String progress);
    }

    // Constructor
    public SuperSUInstaller(Context context, InstallCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    // Método para instalar SuperSU
    public void install() {
        new InstallTask().execute();
    }

    // AsyncTask para instalar SuperSU en segundo plano
    private class InstallTask extends AsyncTask<Void, String, Boolean> {
        @Override
        protected void onPreExecute() {
            callback.onProgress("Iniciando instalación de SuperSU...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                publishProgress("Extrayendo archivos de SuperSU...");
                
                // Extraer archivos necesarios
                File dataDir = context.getFilesDir();
                File supersuDir = new File(dataDir, "supersu");
                if (!supersuDir.exists()) {
                    supersuDir.mkdir();
                }
                
                // Extraer binario su
                File suBinary = new File(supersuDir, "su");
                try (InputStream is = context.getAssets().open("su");
                     OutputStream os = new FileOutputStream(suBinary)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                }
                
                // Extraer SuperSU.apk
                File supersuApk = new File(supersuDir, "SuperSU.apk");
                try (InputStream is = context.getAssets().open("SuperSU.apk");
                     OutputStream os = new FileOutputStream(supersuApk)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                }
                
                // Hacer ejecutables
                suBinary.setExecutable(true);
                
                // Copiar a /system/xbin/
                publishProgress("Copiando binario su a /system/xbin/...");
                Process process = Runtime.getRuntime().exec("mount -o rw,remount /system");
                process.waitFor();
                
                process = Runtime.getRuntime().exec("cp " + suBinary.getAbsolutePath() + " /system/xbin/su");
                process.waitFor();
                
                process = Runtime.getRuntime().exec("chmod 4755 /system/xbin/su");
                process.waitFor();
                
                // Instalar SuperSU.apk
                publishProgress("Instalando aplicación SuperSU...");
                process = Runtime.getRuntime().exec("pm install -r " + supersuApk.getAbsolutePath());
                process.waitFor();
                
                // Verificar instalación
                publishProgress("Verificando instalación...");
                process = Runtime.getRuntime().exec("ls -la /system/xbin/su");
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    publishProgress("SuperSU instalado correctamente");
                    return true;
                } else {
                    publishProgress("Error al verificar instalación de SuperSU");
                    return false;
                }
                
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Error en instalación: " + e.getMessage());
                publishProgress("Error: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values.length > 0) {
                callback.onProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onSuccess("SuperSU instalado correctamente. El dispositivo tiene ahora acceso root permanente.");
            } else {
                callback.onFailed("Falló la instalación de SuperSU.");
            }
        }
    }
}
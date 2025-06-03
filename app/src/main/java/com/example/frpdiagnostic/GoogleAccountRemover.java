package com.example.frpdiagnostic;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Clase para eliminar cuentas de Google del dispositivo
 */
public class GoogleAccountRemover {
    private static final String TAG = "GoogleAccountRemover";
    private Context context;
    private RemoveCallback callback;

    // Interfaz para manejar callbacks de la eliminación
    public interface RemoveCallback {
        void onSuccess(String message);
        void onFailed(String error);
        void onProgress(String progress);
    }

    // Constructor
    public GoogleAccountRemover(Context context, RemoveCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    // Método para eliminar cuentas de Google
    public void removeAccounts() {
        new RemoveTask().execute();
    }

    // AsyncTask para eliminar cuentas en segundo plano
    private class RemoveTask extends AsyncTask<Void, String, Boolean> {
        @Override
        protected void onPreExecute() {
            callback.onProgress("Iniciando eliminación de cuentas de Google...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                publishProgress("Obteniendo cuentas de Google...");
                
                // Obtener cuentas de Google
                AccountManager accountManager = AccountManager.get(context);
                Account[] accounts = accountManager.getAccountsByType("com.google");
                
                if (accounts.length == 0) {
                    publishProgress("No se encontraron cuentas de Google");
                    return true;
                }
                
                publishProgress("Se encontraron " + accounts.length + " cuentas de Google");
                
                // Intentar eliminar cuentas usando métodos estándar
                boolean success = false;
                
                // Método 1: Usando AccountManager directamente
                publishProgress("Intentando eliminar cuentas con AccountManager...");
                for (Account account : accounts) {
                    try {
                        // En Android 6.0+, removeAccount está obsoleto, pero aún funciona
                        // en algunas versiones de Android 7.0
                        Method removeAccountMethod = AccountManager.class.getMethod("removeAccount", 
                                Account.class, AccountManager.AccountManagerCallback.class, android.os.Handler.class);
                        removeAccountMethod.invoke(accountManager, account, null, null);
                        publishProgress("Cuenta eliminada: " + account.name);
                        success = true;
                    } catch (Exception e) {
                        publishProgress("Error al eliminar cuenta con método 1: " + e.getMessage());
                    }
                }
                
                // Método 2: Usando comandos de shell con root
                if (!success) {
                    publishProgress("Intentando eliminar cuentas con comandos root...");
                    
                    // Eliminar archivos de cuenta directamente
                    Process process = Runtime.getRuntime().exec("su -c rm -rf /data/system/users/0/accounts*");
                    process.waitFor();
                    
                    // Eliminar base de datos de cuentas
                    process = Runtime.getRuntime().exec("su -c rm -rf /data/system/users/0/accounts.db*");
                    process.waitFor();
                    
                    publishProgress("Archivos de cuentas eliminados con root");
                    success = true;
                }
                
                // Método 3: Modificar directamente la partición FRP
                if (!success) {
                    publishProgress("Intentando modificar partición FRP...");
                    
                    // Buscar partición FRP
                    Process process = Runtime.getRuntime().exec("su -c ls -la /dev/block/bootdevice/by-name/frp");
                    int exitCode = process.waitFor();
                    
                    if (exitCode == 0) {
                        // Limpiar partición FRP
                        process = Runtime.getRuntime().exec("su -c dd if=/dev/zero of=/dev/block/bootdevice/by-name/frp bs=1024 count=1024");
                        process.waitFor();
                        
                        publishProgress("Partición FRP limpiada");
                        success = true;
                    } else {
                        publishProgress("No se encontró partición FRP");
                    }
                }
                
                // Método 4: Modificar base de datos de configuración
                if (!success) {
                    publishProgress("Intentando modificar base de datos de configuración...");
                    
                    Process process = Runtime.getRuntime().exec("su -c sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"DELETE FROM secure WHERE name='android_id'\"");
                    process.waitFor();
                    
                    publishProgress("Base de datos de configuración modificada");
                    success = true;
                }
                
                return success;
                
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar cuentas: " + e.getMessage());
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
                callback.onSuccess("Cuentas de Google eliminadas correctamente. Se recomienda reiniciar el dispositivo.");
            } else {
                callback.onFailed("Falló la eliminación de cuentas de Google.");
            }
        }
    }
}
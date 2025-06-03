package com.example.frpdiagnostic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Receptor para ejecutar operaciones al inicio del sistema
 * Útil para mantener el bypass de FRP activo después de reinicios
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot completed received");
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {
            
            // Verificar si FRP está activo después del reinicio
            RootFRPBypass rootFRPBypass = new RootFRPBypass(context, new RootFRPBypass.OperationCallback() {
                @Override
                public void onSuccess(String operation, String message) {
                    Log.d(TAG, "Operación exitosa: " + operation + " - " + message);
                }

                @Override
                public void onFailed(String operation, String error) {
                    Log.e(TAG, "Operación fallida: " + operation + " - " + error);
                }

                @Override
                public void onProgress(String operation, String progress) {
                    Log.d(TAG, "Progreso: " + operation + " - " + progress);
                }
            });
            
            // Verificar si FRP está activo
            boolean frpActive = rootFRPBypass.isFRPActive();
            
            if (frpActive) {
                Log.d(TAG, "FRP activo después del reinicio, intentando bypass automático");
                
                // Intentar bypass automático si tenemos root
                if (rootFRPBypass.isRooted()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            rootFRPBypass.bypassFRPWithRoot();
                        }
                    }).start();
                }
                
                // Iniciar la actividad principal para que el usuario pueda tomar acción
                Intent mainIntent = new Intent(context, RootFRPActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mainIntent);
            }
        }
    }
}
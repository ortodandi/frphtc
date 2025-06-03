package com.example.frpdiagnostic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RootFRPActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.GET_ACCOUNTS
    };

    private Button btnCheckRoot;
    private Button btnCheckFRP;
    private Button btnGetRoot;
    private Button btnBypassFRP;
    private Button btnFullBypass;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private ScrollView scrollView;

    private RootFRPBypass rootFRPBypass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_frp);

        // Inicializar vistas
        btnCheckRoot = findViewById(R.id.btnCheckRoot);
        btnCheckFRP = findViewById(R.id.btnCheckFRP);
        btnGetRoot = findViewById(R.id.btnGetRoot);
        btnBypassFRP = findViewById(R.id.btnBypassFRP);
        btnFullBypass = findViewById(R.id.btnFullBypass);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        scrollView = findViewById(R.id.scrollView);

        // Verificar permisos
        if (!hasRequiredPermissions()) {
            requestPermissions();
        }

        // Inicializar RootFRPBypass
        rootFRPBypass = new RootFRPBypass(this, new RootFRPBypass.OperationCallback() {
            @Override
            public void onSuccess(final String operation, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appendStatus("✅ " + operation.toUpperCase() + " EXITOSO: " + message);
                        progressBar.setVisibility(View.GONE);
                        enableButtons(true);
                    }
                });
            }

            @Override
            public void onFailed(final String operation, final String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appendStatus("❌ " + operation.toUpperCase() + " FALLIDO: " + error);
                        progressBar.setVisibility(View.GONE);
                        enableButtons(true);
                    }
                });
            }

            @Override
            public void onProgress(final String operation, final String progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        appendStatus("🔄 " + operation.toUpperCase() + ": " + progress);
                        scrollToBottom();
                    }
                });
            }
        });

        // Configurar listeners de botones
        btnCheckRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRoot();
            }
        });

        btnCheckFRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFRP();
            }
        });

        btnGetRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRoot();
            }
        });

        btnBypassFRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bypassFRP();
            }
        });

        btnFullBypass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullBypass();
            }
        });
    }

    private void checkRoot() {
        appendStatus("Verificando estado de root...");
        boolean isRooted = rootFRPBypass.isRooted();
        if (isRooted) {
            appendStatus("✅ El dispositivo tiene root");
        } else {
            appendStatus("❌ El dispositivo NO tiene root");
        }
    }

    private void checkFRP() {
        appendStatus("Verificando estado de FRP...");
        boolean isFRPActive = rootFRPBypass.isFRPActive();
        if (isFRPActive) {
            appendStatus("⚠️ FRP está ACTIVO");
        } else {
            appendStatus("✅ FRP está DESACTIVADO");
        }
    }

    private void getRoot() {
        enableButtons(false);
        progressBar.setVisibility(View.VISIBLE);
        appendStatus("Iniciando proceso para obtener root...");
        
        // Esto se ejecutaría en un hilo separado en una implementación real
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = rootFRPBypass.obtainRoot();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            appendStatus("✅ Root obtenido exitosamente");
                        } else {
                            appendStatus("❌ No se pudo obtener root");
                        }
                        progressBar.setVisibility(View.GONE);
                        enableButtons(true);
                    }
                });
            }
        }).start();
    }

    private void bypassFRP() {
        enableButtons(false);
        progressBar.setVisibility(View.VISIBLE);
        appendStatus("Iniciando proceso de bypass FRP...");
        
        // Esto se ejecutaría en un hilo separado en una implementación real
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success;
                if (rootFRPBypass.isRooted()) {
                    appendStatus("Usando método con root...");
                    success = rootFRPBypass.bypassFRPWithRoot();
                } else {
                    appendStatus("Usando método sin root...");
                    success = rootFRPBypass.bypassFRPWithoutRoot();
                }
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            appendStatus("✅ Bypass de FRP exitoso");
                        } else {
                            appendStatus("❌ No se pudo hacer bypass de FRP");
                        }
                        progressBar.setVisibility(View.GONE);
                        enableButtons(true);
                    }
                });
            }
        }).start();
    }

    private void fullBypass() {
        enableButtons(false);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText(""); // Limpiar log
        appendStatus("Iniciando proceso completo de Root + FRP Bypass...");
        rootFRPBypass.executeFullBypass();
    }

    private void appendStatus(String message) {
        tvStatus.append(message + "\n\n");
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void enableButtons(boolean enable) {
        btnCheckRoot.setEnabled(enable);
        btnCheckFRP.setEnabled(enable);
        btnGetRoot.setEnabled(enable);
        btnBypassFRP.setEnabled(enable);
        btnFullBypass.setEnabled(enable);
    }

    private boolean hasRequiredPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "Se requieren todos los permisos para el funcionamiento completo", Toast.LENGTH_LONG).show();
            }
        }
    }
}
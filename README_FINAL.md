# Aplicación de Diagnóstico y Bypass de FRP para HTC One M9

Esta aplicación Android está diseñada específicamente para dispositivos HTC One M9 con Android 7.0 Nougat, con el propósito de diagnosticar y potencialmente eliminar la protección Factory Reset Protection (FRP).

## Características

- **Diagnóstico de FRP**: Verifica el estado actual de la protección FRP en el dispositivo.
- **Lectura de cuentas Google**: Muestra las cuentas de Google asociadas al dispositivo.
- **Información de particiones**: Muestra información detallada sobre particiones críticas (misc, fsg, cid).
- **Estado del bootloader**: Detecta si el bootloader está bloqueado o desbloqueado.
- **Ejecución de comandos ADB**: Permite ejecutar comandos ADB con privilegios de root.
- **Exploits de root**: Implementa dos métodos para obtener privilegios de root:
  - Exploit DirtyCow (CVE-2016-5195)
  - Exploit específico para HTC One M9
- **Bypass de FRP**: Implementa métodos para eliminar la protección FRP.

## Archivos APK

Se han generado tres versiones de la aplicación:

1. **HTC_Root_FRP_Bypass.apk**: Versión básica con funcionalidad de diagnóstico y bypass.
2. **HTC_Root_FRP_Bypass_Full.apk**: Versión completa con todas las características.
3. **HTC_Root_FRP_Bypass_Final.apk**: Versión final con implementación nativa de exploits.

## Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── assets/
│   │   │   └── su                      # Binario su para root
│   │   ├── java/
│   │   │   └── com/example/frpdiagnostic/
│   │   │       ├── AccountReader.kt    # Lectura de cuentas Google
│   │   │       ├── AdbExecutor.kt      # Ejecución de comandos ADB
│   │   │       ├── BootloaderChecker.kt # Verificación de bootloader
│   │   │       ├── BootReceiver.java   # Receptor para persistencia
│   │   │       ├── DirtyCowExploit.java # Exploit DirtyCow
│   │   │       ├── FrpChecker.kt       # Verificación de FRP
│   │   │       ├── HTCExploit.java     # Exploit específico HTC
│   │   │       ├── MainActivity.java   # Actividad principal
│   │   │       ├── PartitionInfo.kt    # Información de particiones
│   │   │       └── RootFRPActivity.java # Actividad de Root+FRP
│   │   ├── jniLibs/
│   │   │   └── armeabi-v7a/
│   │   │       ├── libdirtycow.so      # Biblioteca nativa DirtyCow
│   │   │       └── libhtcexploit.so    # Biblioteca nativa HTC
│   │   ├── res/
│   │   │   └── layout/
│   │   │       ├── activity_main.xml   # Layout principal
│   │   │       └── activity_root_frp.xml # Layout Root+FRP
│   │   └── AndroidManifest.xml         # Manifiesto con permisos
│   └── ...
├── build.gradle                        # Configuración de compilación
└── ...
native/
├── dirtycow.c                          # Código fuente exploit DirtyCow
└── htcexploit.c                        # Código fuente exploit HTC
```

## Permisos Requeridos

La aplicación requiere los siguientes permisos:

- `android.permission.READ_EXTERNAL_STORAGE`
- `android.permission.WRITE_EXTERNAL_STORAGE`
- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.READ_PHONE_STATE`
- `android.permission.GET_ACCOUNTS`
- `android.permission.RECEIVE_BOOT_COMPLETED`

## Uso

1. Instala la aplicación en un dispositivo HTC One M9 con Android 7.0 Nougat.
2. Concede todos los permisos solicitados.
3. Utiliza la interfaz para diagnosticar el estado del FRP.
4. Si es necesario, utiliza las opciones de root y bypass de FRP.

## Notas Importantes

- Esta aplicación está diseñada exclusivamente para fines de diagnóstico y pruebas en entornos controlados.
- El uso de esta aplicación para eludir mecanismos de seguridad en dispositivos que no te pertenecen puede ser ilegal.
- Los exploits de root implementados son específicos para el HTC One M9 con Android 7.0 y pueden no funcionar en otros dispositivos o versiones.

## Compilación

Para compilar la aplicación desde el código fuente:

1. Clona el repositorio.
2. Abre el proyecto en Android Studio.
3. Compila el código nativo usando el NDK de Android.
4. Genera el APK con `./gradlew assembleRelease`.

## Descargas

Las versiones compiladas de la aplicación están disponibles en la sección de [Releases](https://github.com/ortodandi/frphtc/releases) del repositorio.

---

**Advertencia**: Esta aplicación es solo para fines educativos y de investigación. El autor no se hace responsable del mal uso de esta herramienta.
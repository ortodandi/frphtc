# HTC One M9 Root + FRP Bypass

Esta aplicación combina funcionalidades de obtención de root y bypass de Factory Reset Protection (FRP) para dispositivos HTC One M9 con Android 7.0 Nougat.

## Características

- **Diagnóstico completo**: Verifica el estado de root, FRP, bootloader y particiones críticas
- **Obtención de root**: Implementa múltiples exploits (DirtyCow, HTC específico)
- **Bypass de FRP**: Métodos con y sin root para eliminar la protección FRP
- **Operación "todo en uno"**: Botón para realizar root + bypass FRP en un solo paso
- **Persistencia**: Mantiene el bypass activo después de reinicios mediante receptor de arranque

## Archivos incluidos

- `HTC_Root_FRP_Bypass.apk`: APK básica con código Java
- `HTC_Root_FRP_Bypass_Full.apk`: APK completa con código nativo C para exploits
- `native/`: Código fuente C de los exploits
  - `dirtycow.c`: Implementación del exploit DirtyCow (CVE-2016-5195)
  - `htcexploit.c`: Exploit específico para HTC One M9

## Métodos de Root implementados

### 1. Exploit DirtyCow (CVE-2016-5195)

Aprovecha una vulnerabilidad del kernel de Linux que afecta a Android 7.0 y versiones anteriores. Permite obtener privilegios de root mediante una condición de carrera en el mecanismo de copy-on-write del kernel.

### 2. Exploit específico de HTC (CVE-2017-0561)

Aprovecha una vulnerabilidad en el controlador HTC Diag que permite la escalada de privilegios en dispositivos HTC One M9 con Android 7.0.

## Métodos de Bypass FRP implementados

### Con root:

1. Eliminación directa de archivos FRP en particiones críticas
2. Modificación de propiedades del sistema
3. Escritura directa en la partición misc

### Sin root:

1. Uso de intents para abrir actividades del sistema que permiten saltarse la verificación
2. Explotación de vulnerabilidades en la interfaz de usuario

## Instrucciones de uso

1. Instalar la APK en el dispositivo HTC One M9
2. Abrir la aplicación
3. Verificar el estado actual (root y FRP)
4. Seleccionar la operación deseada:
   - Obtener root
   - Bypass FRP
   - Root + FRP Bypass completo

## Notas importantes

- Esta aplicación es solo para fines educativos y de diagnóstico en entornos controlados
- Utilizar estos métodos puede anular la garantía del dispositivo
- Algunos métodos pueden requerir un bootloader desbloqueado
- Siempre haz una copia de seguridad antes de intentar obtener root o hacer bypass de FRP
- El éxito de estos exploits puede variar según la versión exacta del firmware

## Compilación desde código fuente

Para compilar la APK desde el código fuente:

1. Configurar Android Studio con NDK
2. Importar el proyecto
3. Compilar las bibliotecas nativas con `ndk-build`
4. Generar la APK con `./gradlew assembleDebug`

## Referencias

- CVE-2016-5195 (DirtyCow): https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2016-5195
- CVE-2017-0561: https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-0561
# HTC One M9 Root + FRP Bypass

Aplicación para dispositivos HTC One M9 con Android 7.0 Nougat que permite realizar root y bypass del Factory Reset Protection (FRP).

## Características

- **Diagnóstico de FRP**: Detecta si el FRP está activo en el dispositivo
- **Lectura de cuentas Google**: Identifica las cuentas de Google asociadas al dispositivo
- **Información de particiones**: Muestra información sobre particiones críticas (misc, fsg, cid)
- **Estado del bootloader**: Detecta si el bootloader está bloqueado o desbloqueado
- **Root del dispositivo**: Utiliza exploits específicos para HTC One M9 (DirtyCow y otros)
- **Bypass de FRP**: Elimina la protección FRP sin necesidad de credenciales
- **Instalación de SuperSU**: Instala SuperSU automáticamente después de obtener root
- **Eliminación de cuentas Google**: Elimina las cuentas de Google asociadas al dispositivo

## Requisitos

- Dispositivo HTC One M9
- Android 7.0 Nougat
- Batería cargada al menos al 50%
- Conexión a Internet (opcional)

## Uso

1. Instala la aplicación en el dispositivo HTC One M9
2. Abre la aplicación
3. Selecciona "Check FRP Status" para verificar el estado actual
4. Selecciona "Root + FRP Bypass" para acceder a las opciones avanzadas
5. Utiliza los botones para realizar las acciones deseadas:
   - "Bypass FRP": Elimina la protección FRP
   - "Root Device": Obtiene acceso root
   - "Install SuperSU": Instala SuperSU
   - "Remove Google Accounts": Elimina las cuentas de Google

## Métodos de Root

La aplicación utiliza varios exploits para obtener acceso root:

1. **DirtyCow (CVE-2016-5195)**: Exploit de kernel que permite elevar privilegios
2. **HTC Exploit específico**: Aprovecha vulnerabilidades específicas de HTC One M9
3. **Exploit de bootloader**: Utiliza vulnerabilidades en el bootloader de HTC

## Métodos de Bypass FRP

Para eliminar la protección FRP, la aplicación:

1. Modifica la partición de persistencia
2. Elimina las cuentas de Google asociadas
3. Restablece los ajustes de fábrica de forma controlada
4. Mantiene el bypass después de reinicios mediante el BootReceiver

## Instalación de SuperSU

La aplicación incluye una versión compatible de SuperSU que:

1. Se instala automáticamente después de obtener root
2. Configura los permisos adecuados
3. Asegura que el acceso root se mantenga después de reinicios

## Eliminación de Cuentas Google

La funcionalidad de eliminación de cuentas:

1. Identifica todas las cuentas de Google en el dispositivo
2. Elimina las cuentas sin necesidad de credenciales
3. Limpia los datos residuales para evitar la reactivación de FRP

## Advertencias

- Esta aplicación está diseñada solo para uso en entornos controlados y con fines de diagnóstico
- El uso indebido puede violar los términos de servicio de Google y HTC
- Realiza siempre una copia de seguridad antes de usar esta aplicación
- El root y bypass de FRP pueden anular la garantía del dispositivo

## Notas Técnicas

- La aplicación requiere permisos elevados para funcionar correctamente
- Algunos métodos utilizan bibliotecas nativas (libdirtycow.so, libhtcexploit.so)
- La aplicación mantiene su funcionalidad después de reinicios mediante el BootReceiver

## Versiones

- **v1.0.0**: Versión inicial con diagnóstico de FRP y root básico
- **v1.0.1**: Añadida funcionalidad de instalación de SuperSU y eliminación de cuentas Google

---

*Esta aplicación es solo para fines educativos y de diagnóstico. El uso indebido es responsabilidad del usuario.*
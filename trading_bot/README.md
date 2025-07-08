# Trading Bot

Este directorio contiene un ejemplo básico de un bot de trading para criptomonedas.

**Advertencia**: Operar con criptomonedas y especialmente con futuros y apalancamiento
implica riesgos elevados. Existe la posibilidad de perder la totalidad del capital
invertido. Utilice este software únicamente con fines educativos y bajo su propia
responsabilidad.

## Instalación

1. Cree un entorno virtual de Python.
2. Instale las dependencias requeridas (`ccxt` y `pandas` son necesarias para
   la ejecución):

```bash
pip install -r requirements.txt
```

## Uso

Edite `bot.py` con sus claves de API y ejecute:

```bash
python -m trading_bot.bot
```

El ejemplo implementa:
- Análisis técnico sencillo con EMA y RSI.
- Gestión de riesgo básica mediante porcentaje de capital.
- Ejecución asincrónica de operaciones.

Amplíe las funciones según sus necesidades antes de operar en cuentas reales.

import asyncio
from importlib import import_module
import pandas as pd
from .strategy import Strategy
from .risk_management import RiskManager

try:
    ccxt = import_module('ccxt.async_support')
except ModuleNotFoundError as e:
    raise ModuleNotFoundError(
        "ccxt is required to run the trading bot. Please install it via 'pip install ccxt'."
    ) from e


class TradingBot:
    """Ejemplo básico de bot de trading asincrónico."""

    def __init__(self, exchange_id='binance', api_key=None, secret=None,
                 symbol='BTC/USDT', timeframe='1h'):
        if not hasattr(ccxt, exchange_id):
            raise ValueError(f"Exchange '{exchange_id}' not supported by ccxt")

        self.exchange = getattr(ccxt, exchange_id)({
            'apiKey': api_key,
            'secret': secret,
            'enableRateLimit': True
        })
        self.symbol = symbol
        self.timeframe = timeframe
        self.strategy = Strategy()
        self.risk_manager = RiskManager()

    async def fetch_ohlcv(self):
        ohlcv = await self.exchange.fetch_ohlcv(self.symbol, timeframe=self.timeframe, limit=500)
        df = pd.DataFrame(ohlcv, columns=['timestamp', 'open', 'high', 'low', 'close', 'volume'])
        df['timestamp'] = pd.to_datetime(df['timestamp'], unit='ms')
        return df

    async def trade(self):
        df = await self.fetch_ohlcv()
        signal = self.strategy.generate_signal(df)
        position_size = self.risk_manager.calculate_position_size(df['close'].iloc[-1])
        print(f'Signal: {signal}, size: {position_size}')
        # Aquí debería enviarse la orden al exchange según la señal

    async def run(self):
        while True:
            try:
                await self.trade()
            except Exception as e:
                print(f'Error: {e}')
            await asyncio.sleep(60)

    async def close(self):
        await self.exchange.close()


if __name__ == '__main__':
    bot = TradingBot()
    asyncio.run(bot.run())

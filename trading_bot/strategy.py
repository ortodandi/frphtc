import pandas as pd


def _ema(series: pd.Series, span: int) -> pd.Series:
    """Simple exponential moving average without external deps."""
    return series.ewm(span=span, adjust=False).mean()


def _rsi(series: pd.Series, period: int) -> pd.Series:
    """Relative Strength Index implementation."""
    delta = series.diff()
    gain = delta.clip(lower=0)
    loss = -delta.clip(upper=0)
    avg_gain = gain.rolling(window=period).mean()
    avg_loss = loss.rolling(window=period).mean()
    rs = avg_gain / avg_loss.replace(0, pd.NA)
    rsi = 100 - (100 / (1 + rs))
    return rsi.fillna(50)


class Strategy:
    """Implementación sencilla utilizando EMA y RSI."""

    def __init__(self, ema_short=9, ema_long=21, rsi_period=14):
        self.ema_short = ema_short
        self.ema_long = ema_long
        self.rsi_period = rsi_period

    def generate_signal(self, df: pd.DataFrame) -> str:
        data = df.copy()
        data['ema_short'] = _ema(data['close'], span=self.ema_short)
        data['ema_long'] = _ema(data['close'], span=self.ema_long)
        data['rsi'] = _rsi(data['close'], period=self.rsi_period)

        if data['ema_short'].iloc[-1] > data['ema_long'].iloc[-1] and data['rsi'].iloc[-1] < 70:
            return 'buy'
        if data['ema_short'].iloc[-1] < data['ema_long'].iloc[-1] and data['rsi'].iloc[-1] > 30:
            return 'sell'
        return 'hold'

import pytest
from trading_bot.risk_management import RiskManager

def test_position_size():
    rm = RiskManager(balance=1000, risk_per_trade=0.02, stop_loss_pct=0.02)
    size = rm.calculate_position_size(100)
    assert size == 10


def test_invalid_price():
    rm = RiskManager()
    with pytest.raises(ValueError):
        rm.calculate_position_size(0)

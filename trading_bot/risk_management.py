class RiskManager:
    """Funciones básicas de gestión de riesgo."""

    def __init__(self, balance=1000, risk_per_trade=0.01, stop_loss_pct=0.02):
        self.balance = balance
        self.risk_per_trade = risk_per_trade
        self.stop_loss_pct = stop_loss_pct

    def calculate_position_size(self, price: float) -> float:
        if price <= 0:
            raise ValueError("Price must be positive")

        risk_amount = self.balance * self.risk_per_trade
        stop_loss_amount = price * self.stop_loss_pct
        if stop_loss_amount <= 0:
            return 0.0
        return risk_amount / stop_loss_amount

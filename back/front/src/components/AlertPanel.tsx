import { Alert } from "@/data/mockData";
import { useState } from "react";

interface AlertPanelProps {
  alerts: Alert[];
}

const typeLabel: Record<Alert["type"], string> = {
  "expense-increase": "Alerta de Despesa",
  "payment-delay": "Atraso de Pagamento",
  "category-change": "Mudança de Categoria",
};

const AlertPanel = ({ alerts: initialAlerts }: AlertPanelProps) => {
  const [alerts, setAlerts] = useState(initialAlerts);
  const activeAlerts = alerts.filter((a) => !a.dismissed);

  const dismiss = (id: string) => {
    setAlerts(alerts.map((a) => (a.id === id ? { ...a, dismissed: true } : a)));
  };

  if (activeAlerts.length === 0) {
    return (
      <div className="border border-border rounded-lg p-6 bg-card">
        <p className="text-sm text-muted-foreground font-heading">Nenhum alerta ativo.</p>
      </div>
    );
  }

  return (
    <div className="border border-border rounded-lg overflow-hidden bg-card">
      <div className="px-6 py-3 border-b border-border">
        <h3 className="font-heading font-medium text-foreground text-sm">Alertas ({activeAlerts.length})</h3>
      </div>
      <div className="divide-y divide-border max-h-[320px] overflow-auto">
        {activeAlerts.map((alert) => (
          <div key={alert.id} className="px-6 py-4 flex items-start justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-heading font-medium text-status-at-risk uppercase tracking-wider">{typeLabel[alert.type]}</span>
                <span className="text-xs text-muted-foreground">{alert.date}</span>
              </div>
              <p className="text-sm text-foreground font-body">{alert.clientName}</p>
              <p className="text-sm text-muted-foreground">{alert.message}</p>
            </div>
            <button
              onClick={() => dismiss(alert.id)}
              className="text-xs font-heading text-muted-foreground hover:text-foreground shrink-0 mt-1"
            >
              Dispensar
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AlertPanel;

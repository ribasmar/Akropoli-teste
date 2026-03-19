import { Client, mockMonthlyData } from "@/data/mockData";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, LineChart, Line } from "recharts";

interface ReportViewProps {
  client: Client;
}

const ReportView = ({ client }: ReportViewProps) => {
  const margin = ((client.revenue - client.expenses) / client.revenue * 100).toFixed(1);

  return (
    <div className="space-y-8">
      <div className="border-b border-border pb-6">
        <h2 className="font-heading font-semibold text-2xl text-foreground">{client.name}</h2>
        <p className="text-sm text-muted-foreground mt-1">Relatório Financeiro — Gerado em {new Date().toLocaleDateString("pt-BR")}</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-card border border-border rounded-lg p-5">
          <p className="text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">Receita</p>
          <p className="text-2xl font-heading font-semibold text-foreground mt-1">R${(client.revenue / 1000000).toFixed(2)}M</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-5">
          <p className="text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">Despesas</p>
          <p className="text-2xl font-heading font-semibold text-foreground mt-1">R${(client.expenses / 1000000).toFixed(2)}M</p>
        </div>
        <div className="bg-card border border-border rounded-lg p-5">
          <p className="text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">Margem</p>
          <p className="text-2xl font-heading font-semibold text-foreground mt-1">{margin}%</p>
        </div>
      </div>

      <div>
        <h3 className="font-heading font-medium text-foreground mb-4">Receita vs. Despesas (6 Meses)</h3>
        <div className="h-72 bg-card border border-border rounded-lg p-4">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={mockMonthlyData}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(210, 14%, 89%)" />
              <XAxis dataKey="month" tick={{ fontFamily: "Inter", fontSize: 12, fill: "hsl(210, 7%, 70%)" }} />
              <YAxis tick={{ fontFamily: "Inter", fontSize: 12, fill: "hsl(210, 7%, 70%)" }} tickFormatter={(v) => `R$${v / 1000}k`} />
              <Tooltip contentStyle={{ fontFamily: "Inter", fontSize: 12, borderRadius: 6, border: "1px solid hsl(210, 14%, 83%)" }} formatter={(value: number) => [`R$${(value / 1000).toFixed(0)}k`]} />
              <Bar dataKey="revenue" fill="hsl(211, 100%, 13%)" radius={[3, 3, 0, 0]} name="Receita" />
              <Bar dataKey="expenses" fill="hsl(210, 14%, 83%)" radius={[3, 3, 0, 0]} name="Despesas" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div>
        <h3 className="font-heading font-medium text-foreground mb-4">Tendência de Receita</h3>
        <div className="h-56 bg-card border border-border rounded-lg p-4">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={mockMonthlyData}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(210, 14%, 89%)" />
              <XAxis dataKey="month" tick={{ fontFamily: "Inter", fontSize: 12, fill: "hsl(210, 7%, 70%)" }} />
              <YAxis tick={{ fontFamily: "Inter", fontSize: 12, fill: "hsl(210, 7%, 70%)" }} tickFormatter={(v) => `R$${v / 1000}k`} />
              <Line type="monotone" dataKey="revenue" stroke="hsl(134, 61%, 41%)" strokeWidth={2} dot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div>
        <h3 className="font-heading font-medium text-foreground mb-2">Notas Financeiras</h3>
        <p className="text-foreground leading-relaxed">{client.financialNotes}</p>
      </div>
    </div>
  );
};

export default ReportView;

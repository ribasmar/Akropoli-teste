import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { clientsApi, pluggyApi } from "@/services/api";
import {
  PieChart, Pie, Cell, Legend, Tooltip, ResponsiveContainer,
} from "recharts";
import { RefreshCw, Link2, ArrowLeft, Wifi, WifiOff, ExternalLink, User } from "lucide-react";
import "../css/ClientDetail.css";

const BRL = (v: number | null | undefined) =>
    v != null ? new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(v) : "—";

const PCT = (v: number | null | undefined) => v != null ? `${v.toFixed(1)}%` : "—";

const CONNECTION_LABELS: Record<string, string> = {
  DISCONNECTED: "Não conectado",
  CONNECTING: "Conectando...",
  UPDATING: "Atualizando...",
  UPDATED: "Conectado",
  CONNECTION_ERROR: "Erro de conexão",
};

// Paleta Emerald para os Gráficos
const PIE_COLORS = ["#064e3b", "#065f46", "#059669", "#10b981", "#6ee7b7"];

const ClientDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();

  const { data: client, isLoading, error } = useQuery({
    queryKey: ["client", id],
    queryFn: () => clientsApi.getById(id!),
    enabled: !!id,
  });

  const { data: accounts } = useQuery({
    queryKey: ["accounts", id],
    queryFn: () => pluggyApi.getAccounts(id!),
    enabled: !!id && client?.connectionStatus === "UPDATED",
  });

  const { data: txData } = useQuery({
    queryKey: ["transactions", id],
    queryFn: () => pluggyApi.getTransactions(id!, {
      from: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString().split("T")[0],
      to: new Date().toISOString().split("T")[0],
      page: 1,
    }),
    enabled: !!id && client?.connectionStatus === "UPDATED",
  });

  const syncMutation = useMutation({
    mutationFn: () => pluggyApi.sync(id!),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["client", id] });
      qc.invalidateQueries({ queryKey: ["accounts", id] });
    },
  });

  if (isLoading) return <div className="loading-state">Carregando perfil...</div>;
  if (error || !client) return <div className="error-state">Cliente não encontrado.</div>;

  const isConnected = client.connectionStatus === "UPDATED";
  const a = client.analytics;

  const categoryData = txData?.results
      ? Object.entries(
          txData.results
              .filter((t) => t.amount < 0)
              .reduce<Record<string, number>>((acc, t) => {
                const cat = t.category ?? "Outros";
                acc[cat] = (acc[cat] ?? 0) + Math.abs(t.amount);
                return acc;
              }, {})
      )
          .sort((a, b) => b[1] - a[1])
          .slice(0, 5)
          .map(([name, value]) => ({ name, value }))
      : [];

  return (
      <div className="detail-container">
        {/* Top Bar */}
        <nav className="detail-nav">
          <button onClick={() => navigate("/clients")} className="btn-back">
            <ArrowLeft size={16} /> Voltar para lista
          </button>
        </nav>

        {/* Profile Header */}
        <header className="profile-header">
          <div className="profile-info">
            <div className="profile-avatar">
              <User size={32} />
            </div>
            <div>
              <h1 className="profile-name">{client.name}</h1>
              <div className="profile-meta">
                <span>{client.email}</span>
                <span className="dot" />
                <span>CPF: {client.cpf}</span>
              </div>
            </div>
          </div>

          <div className="profile-actions">
            {!isConnected ? (
                <button onClick={() => navigate(`/clients/${client.id}/connect`)} className="btn-primary-emerald">
                  <Link2 size={18} /> Conectar Pluggy
                </button>
            ) : (
                <button
                    onClick={() => syncMutation.mutate()}
                    className="btn-outline-emerald"
                    disabled={syncMutation.isPending}
                >
                  <RefreshCw size={16} className={syncMutation.isPending ? "animate-spin" : ""} />
                  {syncMutation.isPending ? "Sincronizando..." : "Sincronizar Dados"}
                </button>
            )}
          </div>
        </header>

        {/* Connection Status Banner */}
        <div className={`status-banner ${isConnected ? 'status-banner--active' : 'status-banner--warn'}`}>
          {isConnected ? <Wifi size={16} /> : <WifiOff size={16} />}
          <span>Status da Conexão: <strong>{CONNECTION_LABELS[client.connectionStatus]}</strong></span>
          {client.lastSync && <span className="sync-time">Última atualização: {new Date(client.lastSync).toLocaleString()}</span>}
        </div>

        {/* Analytics Grid */}
        {a && (
            <section className="section-block">
              <h2 className="section-title">Resumo Financeiro</h2>
              <div className="analytics-grid">
                {[
                  { label: "Saldo Total", value: BRL(a.currentBalance), highlight: true },
                  { label: "Score de Saúde", value: PCT(a.financialHealthScore), health: true },
                  { label: "Renda Média", value: BRL(a.avgMonthlyIncome) },
                  { label: "Taxa de Poupança", value: PCT(a.savingsRate) },
                ].map((item) => (
                    <div key={item.label} className="analytics-card">
                      <span className="card-label">{item.label}</span>
                      <span className={`card-value ${item.highlight ? 'text-emerald' : ''} ${item.health ? 'text-health' : ''}`}>
                  {item.value}
                </span>
                    </div>
                ))}
              </div>
            </section>
        )}

        <div className="detail-content-grid">
          {/* accounts list */}
          <section className="section-block">
            <h2 className="section-title">Contas Conectadas</h2>
            <div className="accounts-stack">
              {accounts?.results.map(acc => (
                  <div key={acc.id} className="account-item">
                    <div className="account-details">
                      <span className="account-type">{acc.subtype}</span>
                      <span className="account-name">{acc.name}</span>
                    </div>
                    <span className="account-balance">{BRL(acc.balance)}</span>
                  </div>
              ))}
            </div>
          </section>

          {/* Categories Chart */}
          {categoryData.length > 0 && (
              <section className="section-block chart-container">
                <h2 className="section-title">Distribuição de Gastos</h2>
                <div className="chart-wrapper">
                  <ResponsiveContainer width="100%" height={240}>
                    <PieChart>
                      <Pie
                          data={categoryData}
                          innerRadius={60}
                          outerRadius={80}
                          paddingAngle={5}
                          dataKey="value"
                      >
                        {categoryData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                      </Pie>
                      <Tooltip />
                      <Legend iconType="circle" />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </section>
          )}
        </div>
      </div>
  );
};

export default ClientDetail;
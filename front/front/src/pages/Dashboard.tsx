import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { clientsApi } from "@/services/api";
import { useAuth } from "@/contexts/AuthContext";
import { Users, TrendingUp, AlertTriangle, Minus } from "lucide-react";
import "../css/Dashboard.css";

const BRL = (v: number) =>
    new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL",
        minimumFractionDigits: 2
    }).format(v);

const Dashboard = () => {
    const navigate = useNavigate();
    const { banker } = useAuth();

    const { data: clients, isLoading } = useQuery({
        queryKey: ["clients"],
        queryFn: clientsApi.list,
    });

    const total = clients?.length ?? 0;
    const connected = clients?.filter((c) => c.connectionStatus === "UPDATED").length ?? 0;
    const errors = clients?.filter((c) => ["CONNECTION_ERROR", "LOGIN_ERROR"].includes(c.connectionStatus)).length ?? 0;
    const disconnected = clients?.filter((c) =>
        ["PENDING", "AWAITING_CONSENT", "CONSENT_EXPIRED", "CONSENT_REVOKED"].includes(c.connectionStatus)
    ).length ?? 0;

    const totalBalance = clients?.reduce((sum, c) => sum + (c.analytics?.currentBalance ?? 0), 0) ?? 0;

    const kpis = [
        { title: "Total de Clientes", value: total, icon: <Users size={20} />, class: "total" },
        { title: "Conectados", value: connected, icon: <TrendingUp size={20} />, class: "connected" },
        { title: "Com Erros", value: errors, icon: <AlertTriangle size={20} />, class: "error" },
        { title: "Sem Consentimento", value: disconnected, icon: <Minus size={20} />, class: "off" },
    ];

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <div>
                    <h1 className="dashboard-title">Painel de Controle</h1>
                    {banker && <p className="dashboard-subtitle">Bem-vindo, {banker.name}</p>}
                </div>
                <div className="dashboard-date">
                    {new Date().toLocaleDateString('pt-BR', { day: '2-digit', month: 'long' })}
                </div>
            </header>

            <div className="kpi-grid">
                {kpis.map((kpi) => (
                    <div key={kpi.title} className={`kpi-card kpi-card--${kpi.class}`}>
                        <div className="kpi-card__header">
                            <span className="kpi-card__icon">{kpi.icon}</span>
                            <span className="kpi-card__label">{kpi.title}</span>
                        </div>
                        {isLoading ? (
                            <div className="skeleton h-10 w-20" />
                        ) : (
                            <div className="kpi-card__value">{kpi.value}</div>
                        )}
                    </div>
                ))}
            </div>

            <div className="metrics-row">
                <div className="metric-box box--primary">
                    <span className="metric-box__label">Patrimônio sob Gestão</span>
                    <h2 className="metric-box__value">{BRL(totalBalance)}</h2>
                    <div className="metric-box__footer">Total consolidado Open Finance</div>
                </div>
            </div>

            <section className="dashboard-section">
                <div className="recent-clients-header">
                    <h2 className="section-title">Clientes Recentes</h2>
                    <button className="btn-link-all" onClick={() => navigate("/clients")}>
                        Ver todos os clientes
                    </button>
                </div>

                <div className="recent-clients-card">
                    {isLoading ? (
                        [1, 2, 3].map(i => <div key={i} className="client-item-skeleton skeleton" />)
                    ) : (
                        clients?.slice(0, 5).map((client) => (
                            <div
                                key={client.id}
                                className="client-item-row"
                                onClick={() => navigate(`/clients/${client.id}`)}
                            >
                                <div className="client-info-stack">
                                    <span className="client-name">{client.name}</span>
                                    <span className="client-email">{client.email}</span>
                                    <span className="client-balance-value">
                    {BRL(client.analytics?.currentBalance ?? 0)}
                  </span>
                                    <span className={`client-status-tag ${client.connectionStatus === 'UPDATED' ? 'active' : 'inactive'}`}>
                    ● {client.connectionStatus === 'UPDATED' ? 'CONECTADO' : 'AGUARDANDO'}
                  </span>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </section>
        </div>
    );
};

export default Dashboard;
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { clientsApi, Client } from "@/services/api";
import { Plus, Search } from "lucide-react";
import "../css/Clients.css";

const statusLabel: Record<Client["connectionStatus"], string> = {
  PENDING: "Pendente",
  AWAITING_CONSENT: "Aguardando consentimento",
  UPDATING: "Atualizando...",
  UPDATED: "Conectado",
  LOGIN_ERROR: "Erro de autenticação",
  CONNECTION_ERROR: "Erro de conexão",
  CONSENT_EXPIRED: "Consentimento expirado",
  CONSENT_REVOKED: "Consentimento revogado",
};

const Clients = () => {
  const navigate = useNavigate();

  const { data: clients, isLoading, error } = useQuery({
    queryKey: ["clients"],
    queryFn: clientsApi.list,
  });

  return (
      <div className="clients-container">
        <header className="clients-header">
          <div>
            <h1 className="clients-title">Base de Clientes</h1>
            <p className="clients-subtitle">Gerencie consentimentos e patrimônio via Open Finance</p>
          </div>

          <div className="clients-actions">
            <button className="btn-icon-secondary">
              <Search size={18} />
            </button>
            <button
                onClick={() => navigate("/clients/new")}
                className="btn-primary-green"
            >
              <Plus size={18} />
              Novo Cliente
            </button>
          </div>
        </header>

        {error && (
            <div className="error-banner">
              <p>Erro ao carregar dados. Verifique a conexão com o servidor.</p>
            </div>
        )}

        <div className="clients-card">
          {isLoading ? (
              <div className="skeleton-list">
                {[...Array(5)].map((_, i) => (
                    <div key={i} className="skeleton-item" />
                ))}
              </div>
          ) : clients && clients.length === 0 ? (
              <div className="empty-state">
                <p>Nenhum cliente encontrado na sua carteira.</p>
                <button onClick={() => navigate("/clients/new")} className="btn-link-green">
                  Adicionar primeiro cliente
                </button>
              </div>
          ) : (
              <div className="table-responsive">
                <table className="custom-table">
                  <thead>
                  <tr>
                    <th>Nome do Cliente</th>
                    <th className="hidden-sm">E-mail</th>
                    <th className="hidden-md text-right">Saldo Consolidado</th>
                    <th>Status Akropoli</th>
                    <th className="hidden-lg">Última Sync</th>
                  </tr>
                  </thead>
                  <tbody>
                  {clients?.map((client) => (
                      <tr key={client.id} onClick={() => navigate(`/clients/${client.id}`)}>
                        <td>
                          <div className="client-cell">
                            <div className="client-avatar">
                              {client.name.substring(0, 2).toUpperCase()}
                            </div>
                            <div>
                              <p className="client-name">{client.name}</p>
                              <p className="client-email-mobile">{client.email}</p>
                            </div>
                          </div>
                        </td>
                        <td className="hidden-sm text-muted">{client.email}</td>
                        <td className="hidden-md text-right font-medium">
                          {client.analytics?.currentBalance != null
                              ? new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(client.analytics.currentBalance)
                              : "—"}
                        </td>
                        <td>
                      <span className={`status-pill pill--${client.connectionStatus.toLowerCase()}`}>
                        {statusLabel[client.connectionStatus]}
                      </span>
                        </td>
                        <td className="hidden-lg text-muted text-xs">
                          {client.lastSync ? new Date(client.lastSync).toLocaleDateString("pt-BR") : "—"}
                        </td>
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>
          )}
        </div>
      </div>
  );
};

export default Clients;

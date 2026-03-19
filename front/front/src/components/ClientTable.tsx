import { Client, ClientStatus } from "@/data/mockData";
import { useNavigate } from "react-router-dom";

interface ClientTableProps {
  clients: Client[];
}

const statusLabel: Record<ClientStatus, string> = {
  growing: "Em Crescimento",
  stable: "Estável",
  "at-risk": "Em Risco",
};

const statusClass: Record<ClientStatus, string> = {
  growing: "text-status-growing",
  stable: "text-status-stable",
  "at-risk": "text-status-at-risk",
};

const ClientTable = ({ clients }: ClientTableProps) => {
  const navigate = useNavigate();

  return (
    <div className="border border-border rounded-lg overflow-hidden">
      <div className="overflow-auto max-h-[480px]">
        <table className="w-full text-left">
          <thead className="bg-card sticky top-0 z-10">
            <tr className="border-b border-border">
              <th className="px-6 py-3 text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">Nome</th>
              <th className="px-6 py-3 text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">Categoria</th>
              <th className="px-6 py-3 text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">Status</th>
              <th className="px-6 py-3 text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">Última Atualização</th>
              <th className="px-6 py-3 text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider"></th>
            </tr>
          </thead>
          <tbody>
            {clients.map((client) => (
              <tr key={client.id} className="border-b border-border last:border-0 hover:bg-card/80 transition-colors">
                <td className="px-6 py-4 font-body font-semibold text-foreground">{client.name}</td>
                <td className="px-6 py-4 text-sm text-muted-foreground">{client.category}</td>
                <td className={`px-6 py-4 text-sm font-heading font-medium ${statusClass[client.status]}`}>
                  {statusLabel[client.status]}
                </td>
                <td className="px-6 py-4 text-sm text-muted-foreground">{client.lastUpdate}</td>
                <td className="px-6 py-4">
                  <button
                    onClick={() => navigate(`/clients/${client.id}`)}
                    className="text-sm font-heading font-medium text-primary hover:underline"
                  >
                    Ver Detalhes
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ClientTable;

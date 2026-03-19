import { useParams } from "react-router-dom";
import { mockClients } from "@/data/mockData";
import ReportView from "@/components/ReportView";

const Reports = () => {
  const { clientId } = useParams();

  if (clientId) {
    const client = mockClients.find((c) => c.id === clientId);
    if (!client) return <div className="p-6 lg:p-8"><p className="text-muted-foreground">Cliente não encontrado.</p></div>;
    return (
      <div className="p-6 lg:p-8">
        <ReportView client={client} />
      </div>
    );
  }

  return (
    <div className="p-6 lg:p-8 space-y-6">
      <h1 className="font-heading font-semibold text-2xl text-foreground">Relatórios</h1>
      <p className="text-muted-foreground text-sm">Selecione um cliente para gerar seu relatório financeiro.</p>
      <div className="border border-border rounded-lg overflow-hidden max-w-2xl">
        {mockClients.map((client) => (
          <a
            key={client.id}
            href={`/reports/${client.id}`}
            className="block px-6 py-4 border-b border-border last:border-0 hover:bg-card transition-colors"
          >
            <p className="font-heading font-medium text-foreground">{client.name}</p>
            <p className="text-sm text-muted-foreground">{client.category} · {client.status}</p>
          </a>
        ))}
      </div>
    </div>
  );
};

export default Reports;

import { useNavigate, useParams } from "react-router-dom";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { clientsApi } from "@/services/api";
import { ArrowLeft, CheckCircle, Link2, RefreshCw } from "lucide-react";

const ConnectAkropoli = () => {
  const { clientId } = useParams<{ clientId: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();

  const { data: client, isFetching, refetch } = useQuery({
    queryKey: ["client", clientId],
    queryFn: () => clientsApi.getById(clientId!),
    enabled: !!clientId,
  });

  const isConnected = client?.connectionStatus === "UPDATED";

  const refreshStatus = async () => {
    await refetch();
    await qc.invalidateQueries({ queryKey: ["clients"] });
  };

  return (
      <div className="p-6 lg:p-8 max-w-xl mx-auto">
        <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground mb-6 font-heading"
        >
          <ArrowLeft size={15} /> Voltar
        </button>

        <div className="bg-card border border-border rounded-lg overflow-hidden">
          <div className="p-8 text-center border-b border-border">
            <div className="mx-auto bg-primary/10 p-4 rounded-full w-fit mb-4">
              <Link2 size={28} className="text-primary" />
            </div>
            <h1 className="font-heading font-semibold text-xl text-foreground">
              Consentimento Akropoli
            </h1>
            <p className="text-sm text-muted-foreground mt-2">
              O consentimento Open Finance é concluído fora do painel. Quando a Akropoli confirmar o vínculo via webhook, o status do cliente será atualizado automaticamente.
            </p>
          </div>

          <div className="p-8 space-y-6">
            <div className="bg-background border border-border rounded-md p-4 space-y-1">
              <p className="text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">
                Cliente
              </p>
              <p className="font-heading font-medium text-foreground">
                {client?.name ?? clientId}
              </p>
              {client?.email && (
                  <p className="text-sm text-muted-foreground">{client.email}</p>
              )}
              <p className="text-sm text-muted-foreground">
                Status atual: <strong>{client?.connectionStatus ?? "PENDING"}</strong>
              </p>
            </div>

            <div className="space-y-2">
              <p className="text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">
                Próximos passos
              </p>
              {[
                "Envie ao cliente o link de consentimento gerado no fluxo operacional externo.",
                "Aguarde o webhook CONSENT_AUTHORISED da Akropoli.",
                "Use o botão abaixo para atualizar o status do cliente no painel.",
              ].map((step, i) => (
                  <div key={i} className="flex items-start gap-3">
                <span className="text-xs font-heading font-semibold text-primary bg-primary/10 w-5 h-5 rounded-full flex items-center justify-center shrink-0 mt-0.5">
                  {i + 1}
                </span>
                    <p className="text-sm text-foreground">{step}</p>
                  </div>
              ))}
            </div>

            {isConnected && (
                <div className="flex items-center gap-3 bg-status-growing/10 border border-status-growing/30 rounded-md p-4">
                  <CheckCircle size={18} className="text-status-growing shrink-0" />
                  <div>
                    <p className="text-sm font-heading font-medium text-status-growing">
                      Consentimento confirmado
                    </p>
                    <p className="text-xs text-muted-foreground">
                      O cliente já está conectado e pronto para sincronização.
                    </p>
                  </div>
                </div>
            )}

            <div className="flex gap-3">
              <button
                  onClick={refreshStatus}
                  disabled={isFetching}
                  className="flex-1 py-3 bg-primary text-primary-foreground rounded-md font-heading font-medium text-sm hover:opacity-90 transition-opacity disabled:opacity-60 flex items-center justify-center gap-2"
              >
                <RefreshCw size={16} className={isFetching ? "animate-spin" : ""} />
                {isFetching ? "Atualizando..." : "Atualizar status"}
              </button>
              <button
                  onClick={() => navigate(`/clients/${clientId}`)}
                  className="flex-1 py-3 border border-border rounded-md font-heading font-medium text-sm text-foreground hover:bg-card transition-colors"
              >
                Voltar ao cliente
              </button>
            </div>
          </div>
        </div>
      </div>
  );
};

export default ConnectAkropoli;
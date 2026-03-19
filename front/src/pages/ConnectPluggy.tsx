import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { clientsApi, pluggyApi } from "@/services/api";
import { ArrowLeft, Link2, CheckCircle, AlertCircle } from "lucide-react";
import { PluggyConnect } from 'pluggy-connect-sdk';

const ConnectPluggy = () => {
  const { clientId } = useParams<{ clientId: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [status, setStatus] = useState<"idle" | "loading" | "success" | "error">("idle");
  const [errorMsg, setErrorMsg] = useState("");

  const { data: client } = useQuery({
    queryKey: ["client", clientId],
    queryFn: () => clientsApi.getById(clientId!),
    enabled: !!clientId,
  });

  const notifyMutation = useMutation({
    mutationFn: ({ itemId }: { itemId: string }) =>
      pluggyApi.notifyItemConnected(clientId!, itemId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["client", clientId] });
      qc.invalidateQueries({ queryKey: ["clients"] });
      setStatus("success");
      setTimeout(() => navigate(`/clients/${clientId}`), 2000);
    },
    onError: (err: any) => {
      setErrorMsg(err.message ?? "Erro ao sincronizar dados.");
      setStatus("error");
    },
  });

  const handleOpenWidget = async () => {
    if (!clientId) return;
    setStatus("loading");
    setErrorMsg("");

    try {
      const tokenData = await pluggyApi.getConnectToken(clientId);

      const pluggyConnect = new PluggyConnect({
        connectToken: tokenData.accessToken,
        includeSandbox: true,
        onSuccess: ({ item }: any) => {
          notifyMutation.mutate({ itemId: item.id });
        },
        onError: (err: any) => {
          setErrorMsg("Não foi possível completar a vinculação.");
          setStatus("error");
        },
        onClose: () => {
          if (status === "loading") setStatus("idle");
        },
      });

      pluggyConnect.init();
    } catch (err: any) {
      setErrorMsg(err.message ?? "Erro ao obter token de conexão. Verifique se o backend está rodando.");
      setStatus("error");
    }
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
        {/* Header */}
        <div className="p-8 text-center border-b border-border">
          <div className="mx-auto bg-primary/10 p-4 rounded-full w-fit mb-4">
            <Link2 size={28} className="text-primary" />
          </div>
          <h1 className="font-heading font-semibold text-xl text-foreground">
            Vincular Conta Bancária
          </h1>
          <p className="text-sm text-muted-foreground mt-2">
            Utilize a tecnologia da Pluggy para conectar as contas do cliente de forma segura.
          </p>
        </div>

        {/* Conteúdo */}
        <div className="p-8 space-y-6">
          {/* Info do cliente */}
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
          </div>

          {/* Como funciona */}
          <div className="space-y-2">
            <p className="text-xs font-heading font-medium text-muted-foreground uppercase tracking-wider">
              Como funciona
            </p>
            {[
              "Uma janela segura da Pluggy será aberta",
              "O cliente seleciona sua instituição financeira",
              "Os dados são sincronizados automaticamente",
            ].map((step, i) => (
              <div key={i} className="flex items-start gap-3">
                <span className="text-xs font-heading font-semibold text-primary bg-primary/10 w-5 h-5 rounded-full flex items-center justify-center shrink-0 mt-0.5">
                  {i + 1}
                </span>
                <p className="text-sm text-foreground">{step}</p>
              </div>
            ))}
          </div>

          {/* Estados de feedback */}
          {status === "success" && (
            <div className="flex items-center gap-3 bg-status-growing/10 border border-status-growing/30 rounded-md p-4">
              <CheckCircle size={18} className="text-status-growing shrink-0" />
              <div>
                <p className="text-sm font-heading font-medium text-status-growing">
                  Conta vinculada com sucesso!
                </p>
                <p className="text-xs text-muted-foreground">
                  Redirecionando para o perfil do cliente...
                </p>
              </div>
            </div>
          )}

          {status === "error" && (
            <div className="flex items-start gap-3 bg-destructive/10 border border-destructive/30 rounded-md p-4">
              <AlertCircle size={18} className="text-destructive shrink-0 mt-0.5" />
              <p className="text-sm text-destructive">{errorMsg}</p>
            </div>
          )}

          {/* CTA */}
          {status !== "success" && (
            <button
              onClick={handleOpenWidget}
              disabled={status === "loading"}
              className="w-full py-3 bg-primary text-primary-foreground rounded-md font-heading font-medium text-sm hover:opacity-90 transition-opacity disabled:opacity-60"
            >
              {status === "loading" ? "Iniciando integração..." : "Abrir Pluggy Connect"}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ConnectPluggy;

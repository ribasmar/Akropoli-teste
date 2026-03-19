import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { clientsApi, CreateClientRequest } from "@/services/api";

const AddClient = () => {
  const navigate = useNavigate();
  const qc = useQueryClient();

  const [form, setForm] = useState<CreateClientRequest>({
    name: "",
    email: "",
    cpf: "",
    phone: "",
  });
  const [error, setError] = useState("");

  const mutation = useMutation({
    mutationFn: clientsApi.create,
    onSuccess: (client) => {
      qc.invalidateQueries({ queryKey: ["clients"] });
      navigate(`/clients/${client.id}`);
    },
    onError: (err: any) => {
      setError(err.message ?? "Erro ao criar cliente.");
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    mutation.mutate(form);
  };

  const formatCpf = (value: string) => {
    return value
      .replace(/\D/g, "")
      .replace(/(\d{3})(\d)/, "$1.$2")
      .replace(/(\d{3})(\d)/, "$1.$2")
      .replace(/(\d{3})(\d{1,2})$/, "$1-$2")
      .slice(0, 14);
  };

  const formatPhone = (value: string) => {
    return value
      .replace(/\D/g, "")
      .replace(/(\d{2})(\d)/, "($1) $2")
      .replace(/(\d{5})(\d)/, "$1-$2")
      .slice(0, 15);
  };

  return (
    <div className="p-6 lg:p-8">
      <div className="max-w-lg">
        <h1 className="font-heading font-semibold text-2xl text-foreground mb-2">
          Adicionar Novo Cliente
        </h1>
        <p className="text-sm text-muted-foreground mb-8">
          Preencha os dados do cliente. Após o cadastro, você poderá conectar a conta bancária via Pluggy.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-heading font-medium text-foreground mb-1">
              Nome completo *
            </label>
            <input
              name="name"
              required
              value={form.name}
              onChange={handleChange}
              placeholder="Maria Oliveira"
              className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body text-sm focus:outline-none focus:ring-2 focus:ring-ring"
              disabled={mutation.isPending}
            />
          </div>

          <div>
            <label className="block text-sm font-heading font-medium text-foreground mb-1">
              E-mail *
            </label>
            <input
              name="email"
              type="email"
              required
              value={form.email}
              onChange={handleChange}
              placeholder="maria@email.com"
              className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body text-sm focus:outline-none focus:ring-2 focus:ring-ring"
              disabled={mutation.isPending}
            />
          </div>

          <div>
            <label className="block text-sm font-heading font-medium text-foreground mb-1">
              CPF *
            </label>
            <input
              name="cpf"
              required
              value={form.cpf}
              onChange={(e) =>
                setForm({ ...form, cpf: e.target.value.replace(/\D/g, "").slice(0, 11) })
              }
              placeholder="000.000.000-00"
              className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body text-sm focus:outline-none focus:ring-2 focus:ring-ring"
              disabled={mutation.isPending}
            />
            <p className="text-xs text-muted-foreground mt-1">
              Somente números (11 dígitos)
            </p>
          </div>

          <div>
            <label className="block text-sm font-heading font-medium text-foreground mb-1">
              Telefone *
            </label>
            <input
              name="phone"
              required
              value={form.phone}
              onChange={(e) =>
                setForm({ ...form, phone: e.target.value.replace(/\D/g, "").slice(0, 11) })
              }
              placeholder="44999990000"
              className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body text-sm focus:outline-none focus:ring-2 focus:ring-ring"
              disabled={mutation.isPending}
            />
            <p className="text-xs text-muted-foreground mt-1">
              Somente números com DDD
            </p>
          </div>

          {error && (
            <p className="text-sm text-destructive font-body">{error}</p>
          )}

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={() => navigate("/clients")}
              className="px-5 py-2.5 border border-border rounded-md font-heading font-medium text-sm text-foreground hover:bg-card transition-colors"
              disabled={mutation.isPending}
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={mutation.isPending}
              className="px-5 py-2.5 bg-primary text-primary-foreground rounded-md font-heading font-medium text-sm hover:opacity-90 transition-opacity disabled:opacity-60"
            >
              {mutation.isPending ? "Cadastrando..." : "Cadastrar Cliente"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddClient;

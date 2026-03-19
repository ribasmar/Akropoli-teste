import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { mockCategories } from "@/data/mockData";

const ClientForm = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    email: "",
    category: mockCategories[0]?.name || "",
    financialNotes: "",
    status: "stable" as const,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    navigate("/clients");
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-2xl space-y-6">
      <div>
        <label className="block text-sm font-heading font-medium text-foreground mb-1">Nome do Cliente</label>
        <input
          type="text"
          required
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <div>
        <label className="block text-sm font-heading font-medium text-foreground mb-1">E-mail</label>
        <input
          type="email"
          required
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body focus:outline-none focus:ring-2 focus:ring-ring"
        />
      </div>
      <div>
        <label className="block text-sm font-heading font-medium text-foreground mb-1">Categoria</label>
        <select
          value={form.category}
          onChange={(e) => setForm({ ...form, category: e.target.value })}
          className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-heading focus:outline-none focus:ring-2 focus:ring-ring"
        >
          {mockCategories.map((cat) => (
            <option key={cat.id} value={cat.name}>{cat.name}</option>
          ))}
        </select>
      </div>
      <div>
        <label className="block text-sm font-heading font-medium text-foreground mb-1">Status</label>
        <select
          value={form.status}
          onChange={(e) => setForm({ ...form, status: e.target.value as any })}
          className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-heading focus:outline-none focus:ring-2 focus:ring-ring"
        >
          <option value="stable">Estável</option>
          <option value="growing">Em Crescimento</option>
          <option value="at-risk">Em Risco</option>
        </select>
      </div>
      <div>
        <label className="block text-sm font-heading font-medium text-foreground mb-1">Notas Financeiras</label>
        <textarea
          value={form.financialNotes}
          onChange={(e) => setForm({ ...form, financialNotes: e.target.value })}
          rows={4}
          className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body focus:outline-none focus:ring-2 focus:ring-ring resize-none"
        />
      </div>
      <div className="flex gap-3">
        <button type="submit" className="px-6 py-2.5 bg-primary text-primary-foreground rounded-md font-heading font-medium text-sm hover:opacity-90 transition-opacity">
          Salvar Cliente
        </button>
        <button type="button" onClick={() => navigate(-1)} className="px-6 py-2.5 border border-border text-foreground rounded-md font-heading font-medium text-sm hover:bg-card transition-colors">
          Cancelar
        </button>
      </div>
    </form>
  );
};

export default ClientForm;

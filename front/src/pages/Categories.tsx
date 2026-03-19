import { useState } from "react";
import { mockCategories, Category } from "@/data/mockData";

const Categories = () => {
  const [categories, setCategories] = useState<Category[]>(mockCategories);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [editDesc, setEditDesc] = useState("");
  const [newName, setNewName] = useState("");
  const [newDesc, setNewDesc] = useState("");

  const startEdit = (cat: Category) => {
    setEditingId(cat.id);
    setEditName(cat.name);
    setEditDesc(cat.description);
  };

  const saveEdit = () => {
    setCategories(categories.map((c) => (c.id === editingId ? { ...c, name: editName, description: editDesc } : c)));
    setEditingId(null);
  };

  const deleteCategory = (id: string) => {
    setCategories(categories.filter((c) => c.id !== id));
  };

  const addCategory = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newName.trim()) return;
    setCategories([...categories, { id: String(Date.now()), name: newName, description: newDesc }]);
    setNewName("");
    setNewDesc("");
  };

  return (
    <div className="p-6 lg:p-8 space-y-8">
      <h1 className="font-heading font-semibold text-2xl text-foreground">Categorias</h1>

      <form onSubmit={addCategory} className="flex flex-col sm:flex-row gap-3 max-w-2xl">
        <input
          value={newName}
          onChange={(e) => setNewName(e.target.value)}
          placeholder="Nome da categoria"
          required
          className="px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body text-sm focus:outline-none focus:ring-2 focus:ring-ring flex-1"
        />
        <input
          value={newDesc}
          onChange={(e) => setNewDesc(e.target.value)}
          placeholder="Descrição"
          className="px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body text-sm focus:outline-none focus:ring-2 focus:ring-ring flex-1"
        />
        <button type="submit" className="px-5 py-2.5 bg-primary text-primary-foreground rounded-md font-heading font-medium text-sm hover:opacity-90 transition-opacity shrink-0">
          Adicionar
        </button>
      </form>

      <div className="border border-border rounded-lg overflow-hidden max-w-2xl">
        {categories.map((cat) => (
          <div key={cat.id} className="px-6 py-4 border-b border-border last:border-0 flex items-center justify-between gap-4">
            {editingId === cat.id ? (
              <div className="flex-1 flex flex-col sm:flex-row gap-2">
                <input value={editName} onChange={(e) => setEditName(e.target.value)} className="px-3 py-1.5 border border-input rounded-md text-sm font-body flex-1" />
                <input value={editDesc} onChange={(e) => setEditDesc(e.target.value)} className="px-3 py-1.5 border border-input rounded-md text-sm font-body flex-1" />
                <button onClick={saveEdit} className="text-sm font-heading text-primary hover:underline">Salvar</button>
                <button onClick={() => setEditingId(null)} className="text-sm font-heading text-muted-foreground hover:text-foreground">Cancelar</button>
              </div>
            ) : (
              <>
                <div>
                  <p className="font-heading font-medium text-foreground">{cat.name}</p>
                  <p className="text-sm text-muted-foreground">{cat.description}</p>
                </div>
                <div className="flex gap-3 shrink-0">
                  <button onClick={() => startEdit(cat)} className="text-sm font-heading text-muted-foreground hover:text-foreground">Editar</button>
                  <button onClick={() => deleteCategory(cat.id)} className="text-sm font-heading text-status-at-risk hover:opacity-80">Excluir</button>
                </div>
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default Categories;

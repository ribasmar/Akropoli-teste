import { useState } from "react";
import { useNavigate } from "react-router-dom";

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSent(true);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4">
      <div className="w-full max-w-sm">
        <h1 className="font-heading font-semibold text-2xl text-foreground text-center mb-2">Recuperação de Senha</h1>
        <p className="text-sm text-muted-foreground text-center mb-8">Insira seu e-mail para receber um link de redefinição.</p>

        {sent ? (
          <div className="text-center space-y-4">
            <div className="bg-card border border-border rounded-lg p-6">
              <p className="text-foreground font-body">Um link de redefinição de senha foi enviado para <strong className="font-heading">{email}</strong>.</p>
            </div>
            <button
              onClick={() => navigate("/")}
              className="text-sm font-heading text-muted-foreground hover:text-foreground transition-colors"
            >
              Voltar para o login
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-heading font-medium text-foreground mb-1">E-mail</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body focus:outline-none focus:ring-2 focus:ring-ring"
                placeholder="voce@empresa.com"
              />
            </div>
            <button
              type="submit"
              className="w-full py-2.5 bg-primary text-primary-foreground rounded-md font-heading font-medium text-sm hover:opacity-90 transition-opacity"
            >
              Enviar Link de Redefinição
            </button>
            <p className="text-center">
              <button
                type="button"
                onClick={() => navigate("/")}
                className="text-sm font-heading text-muted-foreground hover:text-foreground transition-colors"
              >
                Voltar para o login
              </button>
            </p>
          </form>
        )}
      </div>
    </div>
  );
};

export default ForgotPassword;

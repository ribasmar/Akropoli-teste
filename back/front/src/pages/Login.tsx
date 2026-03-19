import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import "../css/Login.css";

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  // Estados de dados
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Estados de estética/animação
  const [revealed, setRevealed] = useState(false);
  const [focused, setFocused] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login({ email, password });
      navigate("/dashboard");
    } catch (err: any) {
      setError(err.message ?? "Credenciais inválidas. Verifique e-mail e senha.");
    } finally {
      setLoading(false);
    }
  };

  return (
      <div className={`login-page ${revealed || focused ? "login-page--revealed" : ""}`}>
        <div className="login-glow" />

        <div className="login-wrapper">
          {/* Logo acima do card */}
          <div className="login-logo">
          <span className={`login-logo__name ${revealed || focused ? "login-logo__name--revealed" : ""}`}>
            Lizard
          </span>
          </div>

          {/* Card */}
          <div
              className="login-card"
              onMouseEnter={() => setRevealed(true)}
              onMouseLeave={() => setRevealed(false)}
          >
            {/* Cover — desliza ao entrar no card ou focar no input */}
            <div className={`login-cover ${focused ? "login-cover--hidden" : ""}`}>
              <div className="login-cover__icon">
                {/* Ajustado para o caminho da sua imagem */}
                <h1>A</h1>
              </div>
              <span className="login-cover__name">Lizard</span>
              <span className="login-cover__hint">Passe o mouse ou clique para entrar</span>
            </div>

            {/* Conteúdo do Formulário */}
            <h2 className="login-card__title">Bem-vindo de volta</h2>
            <p className="login-card__subtitle">
              Entre com suas credenciais para continuar
            </p>

            <form className="login-form" onSubmit={handleLogin}>
              <div className="login-form__field">
                <label className="login-form__label">E-mail</label>
                <input
                    className="login-form__input"
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    onFocus={() => setFocused(true)}
                    onBlur={() => setFocused(false)}
                    placeholder="voce@empresa.com"
                    disabled={loading}
                />
              </div>

              <div className="login-form__field">
                <label className="login-form__label">Senha</label>
                <input
                    className="login-form__input"
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onFocus={() => setFocused(true)}
                    onBlur={() => setFocused(false)}
                    placeholder="••••••••"
                    disabled={loading}
                />
              </div>

              <div className="login-form__forgot">
                <button
                    type="button"
                    className="login-form__forgot-btn"
                    onClick={() => navigate("/forgot-password")}
                >
                  Esqueceu a senha?
                </button>
              </div>

              {error && (
                  <p className="text-sm text-destructive font-body" style={{ color: 'hsl(var(--destructive))', marginBottom: '10px' }}>
                    {error}
                  </p>
              )}

              <button
                  type="submit"
                  className="login-form__submit"
                  disabled={loading}
              >
                {loading ? "Entrando..." : "ENTRAR"}
              </button>
            </form>

            {/* Footer tagline personalizado */}
            <p className="login-tagline">
              Usando <span className="login-tagline__highlight">Open Finance</span> para gerar <span className="login-tagline__highlight">Inteligência de dados</span>
            </p>
          </div>
        </div>
      </div>
  );
};

export default Login;
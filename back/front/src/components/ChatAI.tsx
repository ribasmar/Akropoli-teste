import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

interface Message {
  id: number;
  role: "user" | "assistant";
  content: string;
}

const aiResponses: Record<string, string> = {
  default: "Posso ajudá-lo a analisar as finanças dos clientes, identificar riscos e recomendar ações. Tente perguntar sobre um cliente específico ou uma tendência financeira.",
  risk: "Com base nos dados atuais, Cascade Ventures e Atlas Manufatura apresentam os perfis de maior risco. A taxa de queima da Cascade excede a receita em 50%, e as margens da Atlas caíram abaixo de 5%. Recomendo agendar revisões de emergência para ambos.",
  summarize: "No seu portfólio de 12 clientes: 4 estão em crescimento (33%), 4 estão estáveis (33%) e 4 estão em risco (33%). A receita total gerenciada é de aproximadamente R$18,4M. Os clientes em risco representam R$3,9M em receita e requerem atenção imediata.",
  recommend: "Ações recomendadas: 1) Agendar revisão de emergência com Cascade Ventures para discutir financiamento ponte. 2) Revisar contratos da cadeia de suprimentos da Atlas Manufatura para oportunidades de renegociação. 3) Preparar propostas de investimento em crescimento para Beacon Healthcare e Clearwater Analytics.",
};

function getResponse(input: string): string {
  const lower = input.toLowerCase();
  if (lower.includes("risco") || lower.includes("risk")) return aiResponses.risk;
  if (lower.includes("resum") || lower.includes("summar")) return aiResponses.summarize;
  if (lower.includes("recomend") || lower.includes("ação") || lower.includes("ações")) return aiResponses.recommend;
  return aiResponses.default;
}

interface ChatAIProps {
  onClose: () => void;
}

const ChatAI = ({ onClose }: ChatAIProps) => {
  const [messages, setMessages] = useState<Message[]>([
    { id: 0, role: "assistant", content: "Bem-vindo. Posso ajudá-lo a analisar os dados financeiros dos seus clientes. O que gostaria de saber?" },
  ]);
  const [input, setInput] = useState("");

  const send = () => {
    if (!input.trim()) return;
    const userMsg: Message = { id: Date.now(), role: "user", content: input };
    const aiMsg: Message = { id: Date.now() + 1, role: "assistant", content: getResponse(input) };
    setMessages((prev) => [...prev, userMsg, aiMsg]);
    setInput("");
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.3 }}
      className="flex flex-col h-full"
    >
      <div className="flex items-center justify-between px-6 py-4 border-b border-border">
        <h2 className="font-heading font-medium text-foreground">Assistente IA</h2>
        <button onClick={onClose} className="text-sm font-heading text-muted-foreground hover:text-foreground">
          Fechar
        </button>
      </div>

      <div className="flex-1 overflow-auto p-6 space-y-4">
        {messages.map((msg) => (
          <div key={msg.id} className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}>
            <div
              className={`max-w-[80%] px-4 py-3 rounded-lg text-sm ${
                msg.role === "user"
                  ? "bg-primary text-primary-foreground"
                  : "bg-ai text-ai-foreground"
              }`}
            >
              {msg.content}
            </div>
          </div>
        ))}
      </div>

      <div className="p-4 border-t border-border">
        <div className="flex gap-2">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && send()}
            placeholder="Pergunte sobre seus clientes..."
            className="flex-1 px-4 py-2.5 border border-input rounded-md bg-background text-foreground font-body text-sm focus:outline-none focus:ring-2 focus:ring-ring"
          />
          <button
            onClick={send}
            className="px-5 py-2.5 bg-ai text-ai-foreground rounded-md font-heading font-medium text-sm hover:opacity-90 transition-opacity"
          >
            Enviar
          </button>
        </div>
      </div>
    </motion.div>
  );
};

export default ChatAI;

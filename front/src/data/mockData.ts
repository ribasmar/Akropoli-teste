export type ClientStatus = "growing" | "stable" | "at-risk";

export interface Client {
  id: string;
  name: string;
  email: string;
  category: string;
  status: ClientStatus;
  financialNotes: string;
  lastUpdate: string;
  revenue: number;
  expenses: number;
}

export interface TimelineEvent {
  id: string;
  clientId: string;
  type: "meeting" | "review" | "note" | "alert";
  title: string;
  description: string;
  date: string;
}

export interface Alert {
  id: string;
  clientId: string;
  clientName: string;
  type: "expense-increase" | "payment-delay" | "category-change";
  message: string;
  date: string;
  dismissed: boolean;
}

export interface Category {
  id: string;
  name: string;
  description: string;
}

export const mockCategories: Category[] = [
  { id: "1", name: "Alto Risco", description: "Clientes que necessitam atenção imediata" },
  { id: "2", name: "Estável", description: "Clientes com desempenho financeiro consistente" },
  { id: "3", name: "Em Crescimento", description: "Clientes com crescimento financeiro positivo" },
  { id: "4", name: "VIP", description: "Clientes estratégicos de alto valor" },
];

export const mockClients: Client[] = [
  { id: "1", name: "Meridian Capital Partners", email: "contato@meridian.com", category: "VIP", status: "growing", financialNotes: "Forte desempenho no Q4. Receita cresceu 18% em relação ao ano anterior.", lastUpdate: "2026-03-08", revenue: 2400000, expenses: 1800000 },
  { id: "2", name: "Northfield Industries", email: "financeiro@northfield.com", category: "Estável", status: "stable", financialNotes: "Fluxo de caixa consistente. Sem grandes mudanças previstas.", lastUpdate: "2026-03-07", revenue: 890000, expenses: 720000 },
  { id: "3", name: "Cascade Ventures", email: "ops@cascade.vc", category: "Alto Risco", status: "at-risk", financialNotes: "Taxa de queima acelerando. Runway estimado em 6 meses.", lastUpdate: "2026-03-09", revenue: 340000, expenses: 510000 },
  { id: "4", name: "Sterling & Associados", email: "info@sterling.law", category: "Estável", status: "stable", financialNotes: "Faturamento estável. Pequenas flutuações sazonais.", lastUpdate: "2026-03-05", revenue: 1200000, expenses: 980000 },
  { id: "5", name: "Beacon Healthcare Group", email: "admin@beacon.health", category: "Em Crescimento", status: "growing", financialNotes: "Novos contratos assinados. Previsão de aumento de 25% na receita.", lastUpdate: "2026-03-10", revenue: 3100000, expenses: 2200000 },
  { id: "6", name: "Atlas Manufatura Ltda.", email: "cfo@atlas-mfg.com", category: "Alto Risco", status: "at-risk", financialNotes: "Interrupções na cadeia de suprimentos impactando margens significativamente.", lastUpdate: "2026-03-06", revenue: 1500000, expenses: 1450000 },
  { id: "7", name: "Pinnacle Imóveis", email: "financeiro@pinnacle-re.com", category: "VIP", status: "growing", financialNotes: "Expansão de portfólio em andamento. Posição de mercado forte.", lastUpdate: "2026-03-09", revenue: 4200000, expenses: 2800000 },
  { id: "8", name: "Ironwood Logística", email: "contas@ironwood.com", category: "Estável", status: "stable", financialNotes: "Eficiência operacional melhorada. Margens estáveis.", lastUpdate: "2026-03-04", revenue: 670000, expenses: 540000 },
  { id: "9", name: "Clearwater Analytics", email: "contato@clearwater.io", category: "Em Crescimento", status: "growing", financialNotes: "Métricas SaaS melhorando. ARR crescendo a 30%.", lastUpdate: "2026-03-08", revenue: 980000, expenses: 620000 },
  { id: "10", name: "Granite Construções Ltda.", email: "cobranca@granite.co", category: "Alto Risco", status: "at-risk", financialNotes: "Atrasos em projetos causando problemas de fluxo de caixa. Prazos de pagamento estendidos.", lastUpdate: "2026-03-03", revenue: 2100000, expenses: 2050000 },
  { id: "11", name: "Horizon Biotech", email: "financeiro@horizon-bio.com", category: "VIP", status: "growing", financialNotes: "Série C captada. Investimento em P&D acelerando.", lastUpdate: "2026-03-10", revenue: 1800000, expenses: 1400000 },
  { id: "12", name: "Redwood Consultoria", email: "admin@redwood.co", category: "Estável", status: "stable", financialNotes: "Taxa de utilização mantida em 85%. Backlog saudável.", lastUpdate: "2026-03-07", revenue: 540000, expenses: 420000 },
];

export const mockTimeline: TimelineEvent[] = [
  { id: "1", clientId: "1", type: "meeting", title: "Revisão Trimestral", description: "Discutido desempenho do Q4 e perspectivas do Q1.", date: "2026-03-08" },
  { id: "2", clientId: "1", type: "review", title: "Auditoria Financeira", description: "Demonstrações financeiras anuais revisadas e aprovadas.", date: "2026-02-15" },
  { id: "3", clientId: "1", type: "note", title: "Atualização Estratégica", description: "Cliente considerando expansão para mercados europeus.", date: "2026-01-20" },
  { id: "4", clientId: "1", type: "alert", title: "Marco de Receita", description: "Cliente ultrapassou R$2M de receita trimestral pela primeira vez.", date: "2025-12-31" },
  { id: "5", clientId: "3", type: "alert", title: "Alerta de Taxa de Queima", description: "Taxa de queima mensal excede a receita em 50%.", date: "2026-03-09" },
  { id: "6", clientId: "3", type: "meeting", title: "Revisão de Emergência", description: "Discutidas medidas de corte de custos e financiamento ponte.", date: "2026-03-05" },
  { id: "7", clientId: "5", type: "review", title: "Revisão de Contrato", description: "Novos contratos empresariais revisados. Termos favoráveis.", date: "2026-03-10" },
  { id: "8", clientId: "5", type: "meeting", title: "Planejamento de Crescimento", description: "Mapeado plano de contratação e escala de infraestrutura.", date: "2026-02-28" },
];

export const mockAlerts: Alert[] = [
  { id: "1", clientId: "3", clientName: "Cascade Ventures", type: "expense-increase", message: "Despesas aumentaram 40% nos últimos 30 dias.", date: "2026-03-09", dismissed: false },
  { id: "2", clientId: "6", clientName: "Atlas Manufatura Ltda.", type: "expense-increase", message: "Margens operacionais caíram abaixo de 5%.", date: "2026-03-06", dismissed: false },
  { id: "3", clientId: "10", clientName: "Granite Construções Ltda.", type: "payment-delay", message: "Três faturas com atraso superior a 30 dias.", date: "2026-03-03", dismissed: false },
  { id: "4", clientId: "6", clientName: "Atlas Manufatura Ltda.", type: "category-change", message: "Recomendada mudança de categoria de Estável para Alto Risco.", date: "2026-03-01", dismissed: false },
];

export const mockMonthlyData = [
  { month: "Out", revenue: 180000, expenses: 140000 },
  { month: "Nov", revenue: 195000, expenses: 148000 },
  { month: "Dez", revenue: 210000, expenses: 155000 },
  { month: "Jan", revenue: 200000, expenses: 150000 },
  { month: "Fev", revenue: 220000, expenses: 160000 },
  { month: "Mar", revenue: 240000, expenses: 165000 },
];

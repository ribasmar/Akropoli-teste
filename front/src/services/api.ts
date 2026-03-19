/**
 * api.ts — cliente HTTP centralizado
 * Injeta automaticamente o JWT em todas as requisições autenticadas.
 * Redireciona para /login se o token expirar (401).
 */

const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

// ── Helpers ───────────────────────────────────────────────────────────────────

function getToken(): string | null {
  return localStorage.getItem("token");
}

async function request<T>(
  path: string,
  options: RequestInit = {},
  authenticated = true
): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };

  if (authenticated) {
    const token = getToken();
    if (token) headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    localStorage.removeItem("token");
    window.location.href = "/";
    throw new Error("Sessão expirada. Faça login novamente.");
  }

  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body?.message ?? `Erro ${response.status}`);
  }

  if (response.status === 204) return undefined as T;

  const text = await response.text();
  if (!text || text.trim() === "") return undefined as T;

  try {
    return JSON.parse(text) as T;
  } catch {
    return undefined as T;
  }
}

// ── Auth ──────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  banker: {
    id: string;
    name: string;
    email: string;
  };
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export const authApi = {
  login: (body: LoginRequest) =>
    request<LoginResponse>("/api/v1/auth/login", { method: "POST", body: JSON.stringify(body) }, false),

  register: (body: RegisterRequest) =>
    request<LoginResponse>("/api/v1/auth/register", { method: "POST", body: JSON.stringify(body) }, false),
};

// ── Clients ───────────────────────────────────────────────────────────────────

export interface Analytics {
  currentBalance: number | null;
  projectedBalance: number | null;
  avgMonthlyIncome: number | null;
  avgMonthlyExpenses: number | null;
  savingsRate: number | null;
  financialHealthScore: number | null;
  topSpendingCategory: string | null;
  incomeVolatility: number | null;
}

export interface Client {
  id: string;
  name: string;
  email: string;
  cpf: string;
  phone: string;
  bankerId: string;
  pluggyItemId: string | null;
  connectionStatus: "PENDING" | "CONNECTING" | "UPDATING" | "UPDATED" | "LOGIN_ERROR" | "CONNECTION_ERROR";
  createdAt: string;
  updatedAt: string;
  lastSync: string | null;
  analytics: Analytics | null;
}

export interface CreateClientRequest {
  name: string;
  email: string;
  cpf: string;
  phone: string;
}

export interface UpdateClientRequest {
  name?: string;
  email?: string;
  phone?: string;
}

export const clientsApi = {
  list: () =>
    request<Client[]>("/api/v1/clients"),

  getById: (id: string) =>
    request<Client>(`/api/v1/clients/${id}`),

  create: (body: CreateClientRequest) =>
    request<Client>("/api/v1/clients", { method: "POST", body: JSON.stringify(body) }),

  update: (id: string, body: UpdateClientRequest) =>
    request<Client>(`/api/v1/clients/${id}`, { method: "PUT", body: JSON.stringify(body) }),

  delete: (id: string) =>
    request<void>(`/api/v1/clients/${id}`, { method: "DELETE" }),
};

// ── Pluggy ────────────────────────────────────────────────────────────────────

export interface ConnectTokenResponse {
  accessToken: string;
  expiresAt: string;
}

export interface Account {
  id: string;
  name: string;
  number: string;
  balance: number;
  currencyCode: string;
  type: string;
  subtype: string;
  itemId: string;
}

export interface Transaction {
  id: string;
  description: string;
  currencyCode: string;
  amount: number;
  date: string;
  balance: number;
  category: string;
  type: string;
  accountId: string;
}

export interface Investment {
  id: string;
  name: string;
  balance: number;
  currencyCode: string;
  type: string;
  date: string;
  rate: number;
  itemId: string;
}

export interface TransactionListResponse {
  total: number;
  totalPages: number;
  page: number;
  results: Transaction[];
}

export const pluggyApi = {
  getConnectToken: (clientId: string) =>
    request<ConnectTokenResponse>(`/api/v1/clients/${clientId}/pluggy/connect-token`, { method: "POST" }),

  notifyItemConnected: (clientId: string, itemId: string) =>
    request<void>(`/api/v1/clients/${clientId}/pluggy/items/${itemId}`, { method: "POST" }),

  getAccounts: (clientId: string) =>
    request<{ total: number; results: Account[] }>(`/api/v1/clients/${clientId}/pluggy/accounts`),

  getTransactions: (clientId: string, params?: { from?: string; to?: string; page?: number }) => {
    const qs = new URLSearchParams();
    if (params?.from) qs.set("from", params.from);
    if (params?.to) qs.set("to", params.to);
    if (params?.page) qs.set("page", String(params.page));
    return request<TransactionListResponse>(
      `/api/v1/clients/${clientId}/pluggy/transactions?${qs.toString()}`
    );
  },

  getInvestments: (clientId: string) =>
    request<{ total: number; results: Investment[] }>(`/api/v1/clients/${clientId}/pluggy/investments`),

  sync: (clientId: string) =>
    request<void>(`/api/v1/clients/${clientId}/pluggy/sync`, { method: "POST" }),
};

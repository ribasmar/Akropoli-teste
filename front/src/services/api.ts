/**
 * api.ts — cliente HTTP centralizado
 * Injeta automaticamente o JWT em todas as requisições autenticadas.
 * Redireciona para / se a sessão expirar.
 */

const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export interface BankerProfile {
  id: string;
  name: string;
  email: string;
  role?: string;
}

export interface AuthSession {
  token: string;
  refreshToken?: string;
  banker: BankerProfile;
}

export type ConnectionStatus =
  | "PENDING"
  | "AWAITING_CONSENT"
  | "UPDATING"
  | "UPDATED"
  | "LOGIN_ERROR"
  | "CONNECTION_ERROR"
  | "CONSENT_EXPIRED"
  | "CONSENT_REVOKED";

function getToken(): string | null {
  return localStorage.getItem("token");
}

function clearSession() {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("banker");
}

interface LegacyLoginResponse {
  token: string;
  refreshToken?: string;
  banker: BankerProfile;
}

interface LoginResponse {
  token: string;
  refreshToken?: string;
  id: string;
  name: string;
  email: string;
  role?: string;
}

export function normalizeAuthResponse(raw: LoginResponse | LegacyLoginResponse): AuthSession {
  if ("banker" in raw) {
    return {
      token: raw.token,
      refreshToken: raw.refreshToken,
      banker: raw.banker,
    };
  }

  return {
    token: raw.token,
    refreshToken: raw.refreshToken,
    banker: {
      id: raw.id,
      name: raw.name,
      email: raw.email,
      role: raw.role,
    },
  };
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
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    clearSession();
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

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export const authApi = {
  login: async (body: LoginRequest) =>
    normalizeAuthResponse(
      await request<LoginResponse | LegacyLoginResponse>(
        "/api/v1/auth/login",
        { method: "POST", body: JSON.stringify(body) },
        false
      )
    ),

  register: async (body: RegisterRequest) =>
    normalizeAuthResponse(
      await request<LoginResponse | LegacyLoginResponse>(
        "/api/v1/auth/register",
        { method: "POST", body: JSON.stringify(body) },
        false
      )
    ),
};

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
  cpfMasked: string;
  phone: string;
  bankerId: string;
  akropoliLinkId: string | null;
  connectionStatus: ConnectionStatus;
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
  list: () => request<Client[]>("/api/v1/clients"),
  getById: (id: string) => request<Client>(`/api/v1/clients/${id}`),
  create: (body: CreateClientRequest) =>
    request<Client>("/api/v1/clients", { method: "POST", body: JSON.stringify(body) }),
  update: (id: string, body: UpdateClientRequest) =>
    request<Client>(`/api/v1/clients/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  delete: (id: string) => request<void>(`/api/v1/clients/${id}`, { method: "DELETE" }),
};

export interface Account {
  accountId: string;
  brandName: string;
  number: string;
  type: string;
  compeCode: string;
  ispb: string;
}

interface BackendAccountTransaction {
  transactionId: string;
  transactionName: string;
  type: string;
  creditDebitIndicator: string;
  amount?: {
    amount?: string;
    value?: string;
  };
  transactionDate: string;
}

export interface Transaction {
  id: string;
  description: string;
  amount: number;
  date: string;
  category: string;
  type: string;
}

export interface Investment {
  investmentId: string;
  name?: string;
  productType?: string;
}

function parseAmount(amount?: { amount?: string; value?: string }, indicator?: string) {
  const rawValue = amount?.amount ?? amount?.value ?? "0";
  const numericValue = Number(rawValue);
  return indicator === "DEBITO" ? -Math.abs(numericValue) : Math.abs(numericValue);
}

function normalizeTransaction(transaction: BackendAccountTransaction): Transaction {
  return {
    id: transaction.transactionId,
    description: transaction.transactionName ?? "Transação",
    amount: parseAmount(transaction.amount, transaction.creditDebitIndicator),
    date: transaction.transactionDate,
    category: transaction.transactionName ?? "Outros",
    type: transaction.type ?? transaction.creditDebitIndicator ?? "N/A",
  };
}

export const akropoliApi = {
  getAccounts: (clientId: string) =>
    request<Account[]>(`/api/v1/clients/${clientId}/akropoli/accounts`),

  getTransactions: async (clientId: string) =>
    (
      await request<BackendAccountTransaction[]>(
        `/api/v1/clients/${clientId}/akropoli/transactions`
      )
    ).map(normalizeTransaction),

  getInvestments: (clientId: string) =>
    request<Investment[]>(`/api/v1/clients/${clientId}/akropoli/investments/funds`),

  getResources: (clientId: string) =>
    request<Array<{ type: string }>>(`/api/v1/clients/${clientId}/akropoli/resources`),

  sync: (clientId: string) =>
    request<void>(`/api/v1/clients/${clientId}/akropoli/sync`, { method: "POST" }),
};

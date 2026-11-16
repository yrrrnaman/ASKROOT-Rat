const BASE = import.meta.env.VITE_API_URL ?? '/api';
let accessToken: string | null = null;

export function setToken(t: string | null) { accessToken = t; }

async function request<T>(path: string, init: RequestInit = {}, retry = true): Promise<T> {
  const res = await fetch(BASE + path, {
    ...init,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...init.headers,
    },
  });
  if (res.status === 401 && retry) {
    if (await refresh()) return request<T>(path, init, false);
    logout();
  }
  if (!res.ok) throw new ApiError(res.status, (await res.json().catch(() => ({}))).message ?? res.statusText);
  return res.status === 204 ? (undefined as T) : res.json();
}

async function refresh() {
  const r = await fetch(BASE + '/auth/refresh', { method: 'POST', credentials: 'include' });
  if (!r.ok) return false;
  setToken((await r.json()).accessToken);
  return true;
}

export const api = {
  login: (email: string, password: string) =>
    request<{ accessToken: string }>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  devices: (page = 0) => request<Page<Device>>(`/devices?page=${page}&size=25`),
  device: (id: string) => request<Device>(`/devices/${id}`),
  enroll: () => request<{ code: string; expiresAt: string }>('/devices/enroll', { method: 'POST' }),
  approve: (id: string) => request<Device>(`/devices/${id}/approve`, { method: 'POST' }),
  revoke: (id: string) => request<void>(`/devices/${id}`, { method: 'DELETE' }),
  execute: (deviceId: string, command: string) =>
    request<Execution>('/terminal/execute', { method: 'POST', body: JSON.stringify({ deviceId, command }) }),
  history: (deviceId?: string) => request<Page<Execution>>(`/terminal/history${deviceId ? `?deviceId=${deviceId}` : ''}`),
  audit: (page = 0) => request<Page<AuditEntry>>(`/audit?page=${page}`),
};

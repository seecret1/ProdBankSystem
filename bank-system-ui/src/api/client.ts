import type { JwtAuthenticationDto } from '../types';

const API_BASE: string = (import.meta.env.VITE_API_BASE as string | undefined) ?? '';

export class ApiError extends Error {
    status: number;

    constructor(status: number, message: string) {
        super(message);
        this.status = status;
    }
}

const ACCESS_KEY = 'pb_access';
const REFRESH_KEY = 'pb_refresh';

export const tokenStore = {
    get: (): { access: string | null; refresh: string | null } => ({
        access: localStorage.getItem(ACCESS_KEY),
        refresh: localStorage.getItem(REFRESH_KEY),
    }),
    set(access: string, refresh: string): void {
        localStorage.setItem(ACCESS_KEY, access);
        localStorage.setItem(REFRESH_KEY, refresh);
    },
    clear(): void {
        localStorage.removeItem(ACCESS_KEY);
        localStorage.removeItem(REFRESH_KEY);
    },
};

interface RequestOptions {
    method?: string;
    body?: unknown;
    auth?: boolean;
}

async function parseError(res: Response): Promise<ApiError> {
    let message = `Ошибка ${res.status}`;
    try {
        const data = (await res.json()) as { message?: string };
        if (data && data.message) message = data.message;
    } catch {
        // body is not JSON
    }
    return new ApiError(res.status, message);
}

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const { method = 'GET', body, auth = true } = options;
    const { access, refresh } = tokenStore.get();
    const headers: Record<string, string> = {};
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (auth && access) headers['Authorization'] = `Bearer ${access}`;

    const doFetch = (h: Record<string, string>): Promise<Response> =>
        fetch(`${API_BASE}${path}`, {
            method,
            headers: h,
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });

    let res = await doFetch(headers);

    if (res.status === 401 && auth && refresh) {
        const refreshRes = await fetch(`${API_BASE}/api/v1/auth/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: refresh }),
        });
        if (refreshRes.ok) {
            const data = (await refreshRes.json()) as JwtAuthenticationDto;
            tokenStore.set(data.token, data.refreshToken);
            headers['Authorization'] = `Bearer ${data.token}`;
            res = await doFetch(headers);
        } else {
            tokenStore.clear();
            window.location.assign('/login');
            throw new ApiError(401, 'Сессия истекла. Войдите снова.');
        }
    }

    if (!res.ok) throw await parseError(res);
    if (res.status === 204) return null as T;
    const text = await res.text();
    return text ? (JSON.parse(text) as T) : (null as T);
}
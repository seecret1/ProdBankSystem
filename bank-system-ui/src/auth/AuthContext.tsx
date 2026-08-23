import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { tokenStore } from '../api/client';
import { authApi } from '../api/endpoints';
import type { JwtAuthenticationDto, SignUpRequest } from '../types';

export interface AuthUser {
    id: string;
    email?: string;
    username?: string;
    roles: string[];
    role: string | null;
}

interface AuthContextValue {
    user: AuthUser | null;
    isAuthed: boolean;
    hasRole: (...roles: string[]) => boolean;
    login: (by: 'email' | 'username', login: string, password: string) => Promise<void>;
    signup: (payload: SignUpRequest) => Promise<void>;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function decodePayload(token: string): Record<string, unknown> | null {
    try {
        const part = token.split('.')[1];
        if (!part) return null;
        const b64 = part.replace(/-/g, '+').replace(/_/g, '/');
        const json = decodeURIComponent(
            atob(b64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join(''),
        );
        return JSON.parse(json) as Record<string, unknown>;
    } catch {
        return null;
    }
}

function deriveUser(): AuthUser | null {
    const { access } = tokenStore.get();
    if (!access) return null;
    const p = decodePayload(access);
    if (!p) return null;
    const roles = Array.isArray(p.roles) ? (p.roles as string[]) : [];
    const id = typeof p.userId === 'string' ? p.userId : typeof p.sub === 'string' ? p.sub : '';
    return {
        id,
        email: typeof p.email === 'string' ? p.email : undefined,
        username: typeof p.username === 'string' ? p.username : undefined,
        roles,
        role: roles[0] ?? null,
    };
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(() => deriveUser());

    const applySession = useCallback((data: JwtAuthenticationDto) => {
        tokenStore.set(data.token, data.refreshToken);
        setUser(deriveUser());
    }, []);

    const login = useCallback(
        async (by: 'email' | 'username', login: string, password: string) => {
            const data =
                by === 'email'
                    ? await authApi.signInByEmail(login, password)
                    : await authApi.signInByUsername(login, password);
            applySession(data);
        },
        [applySession],
    );

    const signup = useCallback(
        async (payload: SignUpRequest) => {
            const data = await authApi.signUp(payload);
            applySession(data);
        },
        [applySession],
    );

    const logout = useCallback(async () => {
        const { refresh } = tokenStore.get();
        try {
            if (refresh) await authApi.signOut(refresh);
        } catch {
            // ignore sign-out errors
        }
        tokenStore.clear();
        setUser(null);
    }, []);

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            isAuthed: !!user,
            hasRole: (...roles: string[]) => !!user && roles.some(r => user.roles.includes(r)),
            login,
            signup,
            logout,
        }),
        [user, login, signup, logout],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used within AuthProvider');
    return ctx;
}
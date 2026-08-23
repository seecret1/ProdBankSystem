import { Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '../auth/AuthContext';

interface ProtectedRouteProps {
    roles?: string[];
    children: ReactNode;
}

export function ProtectedRoute({ roles, children }: ProtectedRouteProps) {
    const { isAuthed, hasRole } = useAuth();
    if (!isAuthed) return <Navigate to="/login" replace />;
    if (roles && !hasRole(...roles)) return <Navigate to="/" replace />;
    return <>{children}</>;
}
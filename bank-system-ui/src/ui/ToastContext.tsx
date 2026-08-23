import { createContext, useCallback, useContext, useRef, useState } from 'react';
import type { ReactNode } from 'react';

export type ToastType = 'success' | 'error' | 'info';

interface ToastItem {
    id: number;
    message: string;
    type: ToastType;
}

type ToastFn = (message: string, type?: ToastType) => void;

const ToastContext = createContext<ToastFn>(() => undefined);

export function ToastProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<ToastItem[]>([]);
    const counter = useRef(0);

    const push = useCallback<ToastFn>((message, type = 'info') => {
        counter.current += 1;
        const id = counter.current;
        setToasts(prev => [...prev, { id, message, type }]);
        setTimeout(() => {
            setToasts(prev => prev.filter(t => t.id !== id));
        }, 4000);
    }, []);

    return (
        <ToastContext.Provider value={push}>
            {children}
            <div className="toast-stack">
                {toasts.map(t => (
                    <div key={t.id} className={`toast ${t.type}`}>
                        {t.message}
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    );
}

export function useToast(): ToastFn {
    return useContext(ToastContext);
}
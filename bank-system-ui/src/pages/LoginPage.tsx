import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useToast } from '../ui/ToastContext';

type Mode = 'email' | 'username';

export function LoginPage() {
    const [mode, setMode] = useState<Mode>('email');
    const [login, setLogin] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState(false);

    const { login: doLogin } = useAuth();
    const navigate = useNavigate();
    const toast = useToast();

    const submit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!login.trim() || !password) {
            setError('Заполните все поля');
            return;
        }
        setBusy(true);
        try {
            await doLogin(mode, login.trim(), password);
            toast('Успешный вход', 'success');
            navigate('/', { replace: true });
        } catch (err) {
            setError(err instanceof Error ? err.message : String(err));
        } finally {
            setBusy(false);
        }
    };

    return (
        <div className="auth-wrap">
            <div className="auth-card card">
                <div className="auth-head">
                    <span className="brand-mark lg">П</span>
                    <h1>С возвращением</h1>
                    <p>Войдите в интернет-банк «ПродБанк»</p>
                </div>
                <div className="tabs">
                    <button
                        type="button"
                        className={`tab ${mode === 'email' ? 'active' : ''}`}
                        onClick={() => setMode('email')}
                    >
                        По email
                    </button>
                    <button
                        type="button"
                        className={`tab ${mode === 'username' ? 'active' : ''}`}
                        onClick={() => setMode('username')}
                    >
                        По логину
                    </button>
                </div>
                <form onSubmit={submit}>
                    {mode === 'email' ? (
                        <div className="field">
                            <label>Электронная почта</label>
                            <input
                                className="input"
                                type="email"
                                value={login}
                                onChange={e => setLogin(e.target.value)}
                                placeholder="you@example.com"
                            />
                        </div>
                    ) : (
                        <div className="field">
                            <label>Логин</label>
                            <input
                                className="input"
                                value={login}
                                onChange={e => setLogin(e.target.value)}
                                placeholder="your_username"
                            />
                        </div>
                    )}
                    <div className="field">
                        <label>Пароль</label>
                        <input
                            className="input"
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            placeholder="••••••••"
                        />
                    </div>
                    {error && <div className="form-error visible">{error}</div>}
                    <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
                        {busy ? 'Вход…' : 'Войти'}
                    </button>
                </form>
                <p className="auth-foot">
                    Нет аккаунта? <Link to="/signup">Зарегистрироваться</Link>
                </p>
            </div>
        </div>
    );
}
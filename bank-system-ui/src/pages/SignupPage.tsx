import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useToast } from '../ui/ToastContext';

interface FormValues {
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
    firstName: string;
    lastName: string;
    middleName: string;
    birthDate: string;
}

const EMPTY: FormValues = {
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    middleName: '',
    birthDate: '',
};

export function SignupPage() {
    const [form, setForm] = useState<FormValues>(EMPTY);
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState(false);

    const { signup } = useAuth();
    const navigate = useNavigate();
    const toast = useToast();

    const set = (key: keyof FormValues) => (e: React.ChangeEvent<HTMLInputElement>) =>
        setForm(prev => ({ ...prev, [key]: e.target.value }));

    const submit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        if (form.password !== form.confirmPassword) {
            setError('Пароли не совпадают');
            return;
        }
        if (!form.birthDate) {
            setError('Укажите дату рождения');
            return;
        }
        setBusy(true);
        try {
            await signup({
                username: form.username,
                email: form.email,
                password: form.password,
                confirmPassword: form.confirmPassword,
                firstName: form.firstName,
                lastName: form.lastName,
                middleName: form.middleName || null,
                birthDate: form.birthDate,
            });
            toast('Аккаунт создан. Заполните анкету клиента', 'success');
            navigate('/profile', { replace: true });
        } catch (err) {
            setError(err instanceof Error ? err.message : String(err));
        } finally {
            setBusy(false);
        }
    };

    return (
        <div className="auth-wrap">
            <div className="auth-card card" style={{ maxWidth: 520 }}>
                <div className="auth-head">
                    <span className="brand-mark lg">П</span>
                    <h1>Откройте банк</h1>
                    <p>Регистрация в «ПродБанке» — пара минут</p>
                </div>
                <form onSubmit={submit}>
                    <div className="field-grid">
                        <div className="field">
                            <label>Логин (мин. 8)</label>
                            <input className="input" minLength={8} value={form.username} onChange={set('username')} placeholder="your_username" />
                        </div>
                        <div className="field">
                            <label>Email</label>
                            <input className="input" type="email" value={form.email} onChange={set('email')} placeholder="you@example.com" />
                        </div>
                        <div className="field">
                            <label>Фамилия</label>
                            <input className="input" maxLength={64} value={form.lastName} onChange={set('lastName')} />
                        </div>
                        <div className="field">
                            <label>Имя</label>
                            <input className="input" maxLength={64} value={form.firstName} onChange={set('firstName')} />
                        </div>
                        <div className="field">
                            <label>Отчество</label>
                            <input className="input" maxLength={64} value={form.middleName} onChange={set('middleName')} />
                        </div>
                        <div className="field">
                            <label>Дата рождения</label>
                            <input className="input" type="date" value={form.birthDate} onChange={set('birthDate')} />
                        </div>
                        <div className="field">
                            <label>Пароль (мин. 8)</label>
                            <input className="input" type="password" minLength={8} value={form.password} onChange={set('password')} />
                        </div>
                        <div className="field">
                            <label>Повторите пароль</label>
                            <input className="input" type="password" minLength={8} value={form.confirmPassword} onChange={set('confirmPassword')} />
                        </div>
                    </div>
                    {error && <div className="form-error visible">{error}</div>}
                    <button type="submit" className="btn btn-primary btn-block" disabled={busy}>
                        {busy ? 'Создание…' : 'Создать аккаунт'}
                    </button>
                </form>
                <p className="auth-foot">
                    Уже с нами? <Link to="/login">Войти</Link>
                </p>
            </div>
        </div>
    );
}
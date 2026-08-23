import { useState } from 'react';
import type { FormEvent } from 'react';
import { individualApi, authApi, userApi } from '../api/endpoints';
import { tokenStore } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useToast } from '../ui/ToastContext';

interface IndividualForm {
    passportNumber: string;
    phoneNumber: string;
    address: string;
    city: string;
    zipCode: string;
    countryCode: string;
}

const EMPTY_INDIVIDUAL: IndividualForm = {
    passportNumber: '',
    phoneNumber: '',
    address: '',
    city: '',
    zipCode: '',
    countryCode: '',
};

export function ProfilePage() {
    const { user } = useAuth();
    const toast = useToast();

    const [individual, setIndividual] = useState<IndividualForm>(EMPTY_INDIVIDUAL);
    const [individualError, setIndividualError] = useState<string | null>(null);
    const [individualBusy, setIndividualBusy] = useState(false);

    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [accountError, setAccountError] = useState<string | null>(null);
    const [accountBusy, setAccountBusy] = useState(false);

    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [passwordError, setPasswordError] = useState<string | null>(null);
    const [passwordBusy, setPasswordBusy] = useState(false);

    const setInd = (key: keyof IndividualForm) => (e: React.ChangeEvent<HTMLInputElement>) =>
        setIndividual(prev => ({ ...prev, [key]: e.target.value }));

    const saveIndividual = async (e: FormEvent) => {
        e.preventDefault();
        setIndividualError(null);
        if (!individual.passportNumber || !individual.phoneNumber || !individual.countryCode || !individual.city || !individual.address) {
            setIndividualError('Заполните все поля анкеты');
            return;
        }
        if (!/^[A-Z0-9]{10}$/.test(individual.passportNumber.toUpperCase())) {
            setIndividualError('Паспорт: 10 латинских букв/цифр, например ABC1234567');
            return;
        }
        if (individual.countryCode.length !== 3) {
            setIndividualError('Код страны — ровно 3 буквы, например RUS');
            return;
        }
        const payload = {
            passportNumber: individual.passportNumber.toUpperCase(),
            phoneNumber: individual.phoneNumber,
            address: {
                address: individual.address,
                zipCode: individual.zipCode,
                city: individual.city,
                countryCode: individual.countryCode.toUpperCase(),
            },
        };
        setIndividualBusy(true);
        try {
            try {
                await individualApi.create(payload);
            } catch (err) {
                if (err instanceof Error && 'status' in err && (err as { status: number }).status === 409) {
                    await individualApi.update(payload);
                } else {
                    throw err;
                }
            }
            toast('Анкета сохранена', 'success');
            setIndividual(EMPTY_INDIVIDUAL);
        } catch (err) {
            setIndividualError(err instanceof Error ? err.message : String(err));
        } finally {
            setIndividualBusy(false);
        }
    };

    const saveAccount = async (e: FormEvent) => {
        e.preventDefault();
        setAccountError(null);
        const body: { username?: string; email?: string } = {};
        if (username && username !== user?.username) body.username = username;
        if (email && email !== user?.email) body.email = email;
        if (Object.keys(body).length === 0) {
            setAccountError('Введите новое значение хотя бы одного поля');
            return;
        }
        setAccountBusy(true);
        try {
            await userApi.updateSelf(body);
            toast('Данные аккаунта обновлены', 'success');
            setUsername('');
            setEmail('');
        } catch (err) {
            setAccountError(err instanceof Error ? err.message : String(err));
        } finally {
            setAccountBusy(false);
        }
    };

    const savePassword = async (e: FormEvent) => {
        e.preventDefault();
        setPasswordError(null);
        if (newPassword !== confirmPassword) {
            setPasswordError('Новые пароли не совпадают');
            return;
        }
        if (newPassword.length < 8) {
            setPasswordError('Пароль должен быть не короче 8 символов');
            return;
        }
        setPasswordBusy(true);
        try {
            const data = await authApi.changePassword({
                currentPassword,
                newPassword,
                confirmPassword,
            });
            tokenStore.set(data.token, data.refreshToken);
            toast('Пароль изменён', 'success');
            setCurrentPassword('');
            setNewPassword('');
            setConfirmPassword('');
        } catch (err) {
            setPasswordError(err instanceof Error ? err.message : String(err));
        } finally {
            setPasswordBusy(false);
        }
    };

    return (
        <>
            <h2 className="page-title">Профиль</h2>
            <p className="page-sub">Личные данные, анкета клиента и безопасность</p>
            <div className="grid">
                <div className="card section-card">
                    <div className="section-title">
                        <span className="section-icon yellow">🪪</span> Анкета клиента
                    </div>
                    <p className="section-desc">
                        Паспортные данные и контактный телефон. Страна — 3-буквенный код (например, RUS).
                    </p>
                    <form onSubmit={saveIndividual}>
                        <div className="field-grid">
                            <div className="field">
                                <label>Паспорт (10 символов)</label>
                                <input
                                    className="input"
                                    maxLength={10}
                                    value={individual.passportNumber}
                                    onChange={setInd('passportNumber')}
                                    placeholder="1234567890"
                                />
                                <span className="hint">Только латиница/цифры, без пробелов</span>
                            </div>
                            <div className="field">
                                <label>Телефон</label>
                                <input
                                    className="input"
                                    value={individual.phoneNumber}
                                    onChange={setInd('phoneNumber')}
                                    placeholder="+79990000000"
                                />
                            </div>
                            <div className="field">
                                <label>Адрес</label>
                                <input className="input" value={individual.address} onChange={setInd('address')} placeholder="ул. Ленина, д. 1" />
                            </div>
                            <div className="field">
                                <label>Город</label>
                                <input className="input" value={individual.city} onChange={setInd('city')} placeholder="Москва" />
                            </div>
                            <div className="field">
                                <label>Индекс</label>
                                <input className="input" value={individual.zipCode} onChange={setInd('zipCode')} placeholder="101000" />
                            </div>
                            <div className="field">
                                <label>Страна (код)</label>
                                <input className="input" maxLength={3} value={individual.countryCode} onChange={setInd('countryCode')} placeholder="RUS" />
                            </div>
                        </div>
                        {individualError && <div className="form-error visible">{individualError}</div>}
                        <button type="submit" className="btn btn-primary" disabled={individualBusy}>
                            {individualBusy ? 'Сохранение…' : 'Сохранить анкету'}
                        </button>
                    </form>
                </div>

                <div className="card section-card">
                    <div className="section-title">
                        <span className="section-icon white">👤</span> Данные аккаунта
                    </div>
                    <p className="section-desc">
                        Логин и адрес электронной почты. Оставьте поле пустым, если менять не нужно.
                    </p>
                    <form onSubmit={saveAccount}>
                        <div className="field">
                            <label>Логин</label>
                            <input
                                className="input"
                                minLength={8}
                                value={username}
                                onChange={e => setUsername(e.target.value)}
                                placeholder={user?.username ?? ''}
                            />
                        </div>
                        <div className="field">
                            <label>Email</label>
                            <input
                                className="input"
                                type="email"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                placeholder={user?.email ?? ''}
                            />
                        </div>
                        {accountError && <div className="form-error visible">{accountError}</div>}
                        <button type="submit" className="btn btn-white" disabled={accountBusy}>
                            {accountBusy ? 'Сохранение…' : 'Обновить данные'}
                        </button>
                    </form>
                </div>

                <div className="card section-card">
                    <div className="section-title">
                        <span className="section-icon yellow">🔒</span> Смена пароля
                    </div>
                    <p className="section-desc">Задайте новый пароль для входа в интернет-банк.</p>
                    <form onSubmit={savePassword}>
                        <div className="field">
                            <label>Текущий пароль</label>
                            <input
                                className="input"
                                type="password"
                                value={currentPassword}
                                onChange={e => setCurrentPassword(e.target.value)}
                            />
                        </div>
                        <div className="field">
                            <label>Новый пароль (мин. 8)</label>
                            <input
                                className="input"
                                type="password"
                                minLength={8}
                                value={newPassword}
                                onChange={e => setNewPassword(e.target.value)}
                            />
                        </div>
                        <div className="field">
                            <label>Повторите новый пароль</label>
                            <input
                                className="input"
                                type="password"
                                minLength={8}
                                value={confirmPassword}
                                onChange={e => setConfirmPassword(e.target.value)}
                            />
                        </div>
                        {passwordError && <div className="form-error visible">{passwordError}</div>}
                        <button type="submit" className="btn btn-primary" disabled={passwordBusy}>
                            {passwordBusy ? 'Смена…' : 'Сменить пароль'}
                        </button>
                    </form>
                </div>
            </div>
        </>
    );
}
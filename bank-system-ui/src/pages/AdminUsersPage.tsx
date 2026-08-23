import { useCallback, useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { adminUserApi } from '../api/endpoints';
import type { CreateUserRequest, PageResponse, Role, UserResponse, UserStatus } from '../types';
import { RoleBadge, StatusChip } from '../components/Badges';
import { ConfirmDialog, Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { useToast } from '../ui/ToastContext';

interface FilterState {
    page: number;
    size: number;
    status: string;
    role: string;
    search: string;
}

const INITIAL_FILTERS: FilterState = { page: 0, size: 10, status: '', role: '', search: '' };

function buildParams(f: FilterState): URLSearchParams {
    const p = new URLSearchParams();
    p.set('page.number', String(f.page));
    p.set('page.size', String(f.size));
    p.set('deleted', 'false');
    if (f.status) p.set('status', f.status);
    if (f.role) p.set('role', f.role);
    if (f.search) p.set('firstName', f.search);
    return p;
}

interface UserFormValues {
    username: string;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    middleName: string;
    birthDate: string;
    status: UserStatus;
    role: Role;
}

function initialValues(user: UserResponse | null): UserFormValues {
    return {
        username: user?.username ?? '',
        email: user?.email ?? '',
        password: '',
        firstName: user?.firstName ?? '',
        lastName: user?.lastName ?? '',
        middleName: user?.middleName ?? '',
        birthDate: user?.birthDate ?? '',
        status: user?.status ?? 'PENDING_PROFILE',
        role: user?.role ?? 'ROLE_USER',
    };
}

interface UserFormModalProps {
    user: UserResponse | null;
    onClose: () => void;
    onSaved: () => void;
}

function UserFormModal({ user, onClose, onSaved }: UserFormModalProps) {
    const [form, setForm] = useState<UserFormValues>(() => initialValues(user));
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState(false);
    const toast = useToast();

    const set = <K extends keyof UserFormValues>(key: K, value: UserFormValues[K]) =>
        setForm(prev => ({ ...prev, [key]: value }));

    const submit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        const payload: CreateUserRequest = {
            username: form.username,
            email: form.email,
            password: form.password,
            firstName: form.firstName,
            lastName: form.lastName,
            middleName: form.middleName || null,
            birthDate: form.birthDate || null,
            status: form.status,
            role: form.role,
        };
        setBusy(true);
        try {
            if (user) {
                await adminUserApi.update(user.id, payload);
                toast('Пользователь обновлён', 'success');
            } else {
                await adminUserApi.create(payload);
                toast('Пользователь создан', 'success');
            }
            onSaved();
        } catch (err) {
            setError(err instanceof Error ? err.message : String(err));
        } finally {
            setBusy(false);
        }
    };

    return (
        <Modal title={user ? 'Редактировать пользователя' : 'Создать пользователя'} onClose={onClose} size="lg">
            <form onSubmit={submit}>
                <div className="field-grid">
                    <div className="field">
                        <label>Логин (мин. 8)</label>
                        <input className="input" minLength={8} required value={form.username} onChange={e => set('username', e.target.value)} />
                    </div>
                    <div className="field">
                        <label>Email</label>
                        <input className="input" type="email" required value={form.email} onChange={e => set('email', e.target.value)} />
                    </div>
                    <div className="field">
                        <label>Пароль (мин. 8)</label>
                        <input
                            className="input"
                            type="password"
                            minLength={8}
                            required={!user}
                            value={form.password}
                            onChange={e => set('password', e.target.value)}
                        />
                        {user && <span className="hint">Будет перезаписан</span>}
                    </div>
                    <div className="field">
                        <label>Дата рождения</label>
                        <input className="input" type="date" value={form.birthDate} onChange={e => set('birthDate', e.target.value)} />
                    </div>
                    <div className="field">
                        <label>Фамилия</label>
                        <input className="input" maxLength={100} value={form.lastName} onChange={e => set('lastName', e.target.value)} />
                    </div>
                    <div className="field">
                        <label>Имя</label>
                        <input className="input" maxLength={80} value={form.firstName} onChange={e => set('firstName', e.target.value)} />
                    </div>
                    <div className="field">
                        <label>Отчество</label>
                        <input className="input" maxLength={64} value={form.middleName} onChange={e => set('middleName', e.target.value)} />
                    </div>
                    <div className="field">
                        <label>Статус</label>
                        <select className="input" value={form.status} onChange={e => set('status', e.target.value as UserStatus)}>
                            <option value="PENDING_PROFILE">PENDING_PROFILE</option>
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="BLOCKED">BLOCKED</option>
                        </select>
                    </div>
                    <div className="field">
                        <label>Роль</label>
                        <select className="input" value={form.role} onChange={e => set('role', e.target.value as Role)}>
                            <option value="ROLE_USER">ROLE_USER</option>
                            <option value="ROLE_MANAGER">ROLE_MANAGER</option>
                            <option value="ROLE_ADMIN">ROLE_ADMIN</option>
                        </select>
                    </div>
                </div>
                {error && <div className="form-error visible">{error}</div>}
                <div className="modal-actions">
                    <button type="button" className="btn btn-ghost" onClick={onClose}>
                        Отмена
                    </button>
                    <button type="submit" className="btn btn-primary" disabled={busy}>
                        {busy ? 'Сохранение…' : user ? 'Сохранить' : 'Создать'}
                    </button>
                </div>
            </form>
        </Modal>
    );
}

export function AdminUsersPage() {
    const [filters, setFilters] = useState<FilterState>(INITIAL_FILTERS);
    const [data, setData] = useState<PageResponse<UserResponse> | null>(null);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState<string | null>(null);
    const [reloadKey, setReloadKey] = useState(0);
    const [modalUser, setModalUser] = useState<UserResponse | null | undefined>(undefined);
    const [deleteTarget, setDeleteTarget] = useState<UserResponse | null>(null);
    const toast = useToast();

    const reload = useCallback(() => setReloadKey(k => k + 1), []);

    useEffect(() => {
        let alive = true;
        setLoading(true);
        setLoadError(null);
        adminUserApi
            .list(buildParams(filters))
            .then(res => {
                if (alive) setData(res);
            })
            .catch(err => {
                if (alive) setLoadError(err instanceof Error ? err.message : String(err));
            })
            .finally(() => {
                if (alive) setLoading(false);
            });
        return () => {
            alive = false;
        };
    }, [filters, reloadKey]);

    const applyFilters = (patch: Partial<FilterState>) => {
        setFilters(prev => ({ ...prev, ...patch, page: 0 }));
    };

    const confirmDelete = async () => {
        if (!deleteTarget) return;
        try {
            await adminUserApi.remove(deleteTarget.id);
            toast('Пользователь удалён', 'success');
            setDeleteTarget(null);
            reload();
        } catch (err) {
            toast(err instanceof Error ? err.message : String(err), 'error');
        }
    };

    return (
        <>
            <h2 className="page-title">Пользователи</h2>
            <p className="page-sub">Управление клиентами и сотрудниками банка</p>

            <div className="toolbar">
                <div className="field">
                    <label>Статус</label>
                    <select
                        className="input"
                        value={filters.status}
                        onChange={e => applyFilters({ status: e.target.value })}
                    >
                        <option value="">Все</option>
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="BLOCKED">BLOCKED</option>
                        <option value="PENDING_PROFILE">PENDING_PROFILE</option>
                    </select>
                </div>
                <div className="field">
                    <label>Роль</label>
                    <select className="input" value={filters.role} onChange={e => applyFilters({ role: e.target.value })}>
                        <option value="">Все</option>
                        <option value="ROLE_USER">ROLE_USER</option>
                        <option value="ROLE_MANAGER">ROLE_MANAGER</option>
                        <option value="ROLE_ADMIN">ROLE_ADMIN</option>
                    </select>
                </div>
                <div className="field">
                    <label>Имя</label>
                    <input
                        className="input"
                        value={filters.search}
                        onChange={e => applyFilters({ search: e.target.value })}
                        placeholder="Поиск по имени"
                    />
                </div>
                <button type="button" className="btn btn-ghost" onClick={() => setFilters(INITIAL_FILTERS)}>
                    Сбросить
                </button>
                <button type="button" className="btn btn-white" onClick={() => setModalUser(null)}>
                    + Создать
                </button>
            </div>

            {loading ? (
                <div className="loading">
                    <span className="spinner" /> Загрузка…
                </div>
            ) : loadError ? (
                <div className="card empty">
                    <div className="e-icon">⚠️</div>
                    {loadError}
                </div>
            ) : data && data.data.length > 0 ? (
                <>
                    <div className="table-wrap">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>ФИО</th>
                                    <th>Логин / Email</th>
                                    <th>Статус</th>
                                    <th>Роль</th>
                                    <th>ДР</th>
                                    <th />
                                </tr>
                            </thead>
                            <tbody>
                                {data.data.map(u => (
                                    <tr key={u.id}>
                                        <td>
                                            <div className="cell-main">
                                                {u.firstName} {u.lastName}
                                            </div>
                                            <div className="cell-sub">{u.middleName}</div>
                                        </td>
                                        <td>
                                            <div className="cell-main">{u.username}</div>
                                            <div className="cell-sub">{u.email}</div>
                                        </td>
                                        <td>
                                            <StatusChip status={u.status} />
                                        </td>
                                        <td>
                                            <RoleBadge role={u.role} />
                                        </td>
                                        <td>{u.birthDate || '—'}</td>
                                        <td>
                                            <div className="row-actions">
                                                <button type="button" className="btn btn-ghost btn-sm" title="Редактировать" onClick={() => setModalUser(u)}>
                                                    ✎
                                                </button>
                                                <button type="button" className="btn btn-danger btn-sm" title="Удалить" onClick={() => setDeleteTarget(u)}>
                                                    ✕
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    <Pagination
                        page={filters.page}
                        size={filters.size}
                        totalPages={data.totalPages}
                        totalElements={data.totalElements}
                        onPageChange={page => setFilters(prev => ({ ...prev, page }))}
                        onSizeChange={size => setFilters(prev => ({ ...prev, size, page: 0 }))}
                    />
                </>
            ) : (
                <div className="card empty">
                    <div className="e-icon">🗂</div>
                    Пользователи не найдены
                </div>
            )}

            {modalUser !== undefined && (
                <UserFormModal
                    user={modalUser}
                    onClose={() => setModalUser(undefined)}
                    onSaved={() => {
                        setModalUser(undefined);
                        reload();
                    }}
                />
            )}

            {deleteTarget && (
                <ConfirmDialog
                    title="Удаление пользователя"
                    message={`Точно удалить пользователя «${deleteTarget.username}»?`}
                    confirmLabel="Удалить"
                    onClose={() => setDeleteTarget(null)}
                    onConfirm={confirmDelete}
                />
            )}
        </>
    );
}
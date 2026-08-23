import { useCallback, useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { officeApi } from '../api/endpoints';
import type {
    OfficeCreateRequest,
    OfficeFullResponse,
    OfficeResponse,
    OfficeUpdateRequest,
    PageResponse,
    ScheduleRequest,
} from '../types';
import { ConfirmDialog, Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { useToast } from '../ui/ToastContext';
import { useAuth } from '../auth/AuthContext';

const DEFAULT_SCHEDULE: ScheduleRequest[] = [
    { day: 'MONDAY', openingTime: '09:00', closingTime: '18:00' },
    { day: 'TUESDAY', openingTime: '09:00', closingTime: '18:00' },
    { day: 'WEDNESDAY', openingTime: '09:00', closingTime: '18:00' },
    { day: 'THURSDAY', openingTime: '09:00', closingTime: '18:00' },
    { day: 'FRIDAY', openingTime: '09:00', closingTime: '18:00' },
];

function parseSchedule(raw: string): ScheduleRequest[] | null {
    try {
        const v = JSON.parse(raw);
        if (!Array.isArray(v)) return null;
        const ok = v.every(
            (item: unknown) =>
                !!item &&
                typeof item === 'object' &&
                typeof (item as ScheduleRequest).day === 'string' &&
                typeof (item as ScheduleRequest).openingTime === 'string' &&
                typeof (item as ScheduleRequest).closingTime === 'string',
        );
        return ok ? (v as ScheduleRequest[]) : null;
    } catch {
        return null;
    }
}

interface OfficeFormValues {
    name: string;
    contactPhone: string;
    scheduleRaw: string;
    address: string;
    zipCode: string;
    city: string;
    countryCode: string;
}

function initialValues(office: OfficeFullResponse | null): OfficeFormValues {
    return {
        name: office?.name ?? '',
        contactPhone: office?.contactPhone ?? '',
        scheduleRaw: office ? JSON.stringify(office.scheduleJson, null, 2) : JSON.stringify(DEFAULT_SCHEDULE, null, 2),
        address: office?.address?.address ?? '',
        zipCode: office?.address?.zipCode ?? '',
        city: office?.address?.city ?? '',
        countryCode: 'RU',
    };
}

interface OfficeFormModalProps {
    office: OfficeFullResponse | null;
    onClose: () => void;
    onSaved: () => void;
}

function OfficeFormModal({ office, onClose, onSaved }: OfficeFormModalProps) {
    const [form, setForm] = useState<OfficeFormValues>(() => initialValues(office));
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState(false);
    const toast = useToast();

    const set = <K extends keyof OfficeFormValues>(key: K, value: OfficeFormValues[K]) =>
        setForm(prev => ({ ...prev, [key]: value }));

    const submit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!/^\+?[0-9]{10,15}$/.test(form.contactPhone)) {
            setError('Телефон: 10–15 цифр, возможно с «+»');
            return;
        }
        const schedule = parseSchedule(form.scheduleRaw);
        if (!schedule) {
            setError('График — это JSON-массив с полями day, openingTime, closingTime');
            return;
        }
        setBusy(true);
        try {
            if (office) {
                const payload: OfficeUpdateRequest = {
                    name: form.name,
                    contactPhone: form.contactPhone,
                    scheduleJson: schedule,
                };
                await officeApi.update(office.id, payload);
                toast('Офис обновлён', 'success');
            } else {
                if (!form.address || !form.city || !form.zipCode || !form.countryCode) {
                    setError('Заполните адрес офиса полностью');
                    return;
                }
                const payload: OfficeCreateRequest = {
                    name: form.name,
                    contactPhone: form.contactPhone,
                    scheduleJson: schedule,
                    address: {
                        address: form.address,
                        zipCode: form.zipCode,
                        city: form.city,
                        countryCode: form.countryCode,
                    },
                };
                await officeApi.create(payload);
                toast('Офис создан', 'success');
            }
            onSaved();
        } catch (err) {
            setError(err instanceof Error ? err.message : String(err));
        } finally {
            setBusy(false);
        }
    };

    return (
        <Modal title={office ? 'Редактировать офис' : 'Создать офис'} onClose={onClose} size="lg">
            <form onSubmit={submit}>
                <div className="field-grid">
                    <div className="field">
                        <label>Название</label>
                        <input className="input" required value={form.name} onChange={e => set('name', e.target.value)} />
                    </div>
                    <div className="field">
                        <label>Телефон</label>
                        <input className="input" value={form.contactPhone} onChange={e => set('contactPhone', e.target.value)} placeholder="+74951234567" />
                    </div>
                    <div className="field">
                        <label>Адрес</label>
                        <input className="input" value={form.address} onChange={e => set('address', e.target.value)} placeholder="ул. Ленина, д. 1" />
                    </div>
                    <div className="field">
                        <label>Город</label>
                        <input className="input" value={form.city} onChange={e => set('city', e.target.value)} placeholder="Москва" />
                    </div>
                    <div className="field">
                        <label>Индекс</label>
                        <input className="input" value={form.zipCode} onChange={e => set('zipCode', e.target.value)} placeholder="101000" />
                    </div>
                    <div className="field">
                        <label>Страна (код)</label>
                        <input className="input" maxLength={3} value={form.countryCode} onChange={e => set('countryCode', e.target.value.toUpperCase())} placeholder="RU" />
                    </div>
                </div>
                <div className="field">
                    <label>График работы (JSON)</label>
                    <textarea
                        className="input"
                        rows={8}
                        style={{ fontFamily: 'ui-monospace, monospace', fontSize: 13 }}
                        value={form.scheduleRaw}
                        onChange={e => set('scheduleRaw', e.target.value)}
                    />
                    <span className="hint">{'Массив: [{ day, openingTime, closingTime }, …]'}</span>
                </div>
                {error && <div className="form-error visible">{error}</div>}
                <div className="modal-actions">
                    <button type="button" className="btn btn-ghost" onClick={onClose}>
                        Отмена
                    </button>
                    <button type="submit" className="btn btn-primary" disabled={busy}>
                        {busy ? 'Сохранение…' : office ? 'Сохранить' : 'Создать'}
                    </button>
                </div>
            </form>
        </Modal>
    );
}

interface OfficeRow {
    id?: string;
    name: string;
    contactPhone: string;
    active: boolean;
    city?: string;
    address?: string;
}

export function AdminOfficesPage() {
    const { hasRole } = useAuth();
    const isAdmin = hasRole('ROLE_ADMIN');

    const [data, setData] = useState<PageResponse<OfficeFullResponse> | PageResponse<OfficeResponse> | null>(null);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);
    const [city, setCity] = useState('');
    const [cityQuery, setCityQuery] = useState('');
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState<string | null>(null);
    const [reloadKey, setReloadKey] = useState(0);
    const [modalOffice, setModalOffice] = useState<OfficeFullResponse | null | undefined>(undefined);
    const [blockTarget, setBlockTarget] = useState<OfficeRow | null>(null);
    const [deleteTarget, setDeleteTarget] = useState<OfficeRow | null>(null);
    const toast = useToast();

    const reload = useCallback(() => setReloadKey(k => k + 1), []);

    useEffect(() => {
        let alive = true;
        setLoading(true);
        setLoadError(null);
        const request = cityQuery ? officeApi.findByCity(cityQuery, page, size) : officeApi.list(page, size);
        request
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
    }, [page, size, cityQuery, reloadKey]);

    const rows: OfficeRow[] = (data?.data ?? []).map(o => ({
        id: 'id' in o ? (o as OfficeFullResponse).id : undefined,
        name: o.name,
        contactPhone: o.contactPhone,
        active: o.active,
        city: o.address?.city,
        address: o.address?.address,
    }));

    const confirmBlock = async () => {
        if (!blockTarget?.id) return;
        try {
            await officeApi.block(blockTarget.id);
            toast('Офис деактивирован', 'success');
            setBlockTarget(null);
            reload();
        } catch (err) {
            toast(err instanceof Error ? err.message : String(err), 'error');
        }
    };

    const confirmDelete = async () => {
        if (!deleteTarget?.id) return;
        try {
            await officeApi.remove(deleteTarget.id);
            toast('Офис удалён', 'success');
            setDeleteTarget(null);
            reload();
        } catch (err) {
            toast(err instanceof Error ? err.message : String(err), 'error');
        }
    };

    const searchByCity = (e: FormEvent) => {
        e.preventDefault();
        setCityQuery(city.trim());
        setPage(0);
    };

    return (
        <>
            <h2 className="page-title">Офисы</h2>
            <p className="page-sub">Отделения банка: адреса, телефоны и график работы</p>

            <div className="toolbar">
                <form onSubmit={searchByCity} className="field" style={{ flex: '0 1 320px' }}>
                    <label>Поиск по городу</label>
                    <div style={{ display: 'flex', gap: 8 }}>
                        <input
                            className="input"
                            value={city}
                            onChange={e => setCity(e.target.value)}
                            placeholder="Москва"
                        />
                        <button type="submit" className="btn btn-ghost">
                            Найти
                        </button>
                        {cityQuery && (
                            <button
                                type="button"
                                className="btn btn-ghost"
                                onClick={() => {
                                    setCity('');
                                    setCityQuery('');
                                    setPage(0);
                                }}
                            >
                                Сбросить
                            </button>
                        )}
                    </div>
                </form>
                {isAdmin && (
                    <button type="button" className="btn btn-primary" onClick={() => setModalOffice(null)} style={{ marginLeft: 'auto' }}>
                        + Создать офис
                    </button>
                )}
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
            ) : rows.length > 0 ? (
                <>
                    <div className="table-wrap">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Название</th>
                                    <th>Город</th>
                                    <th>Адрес</th>
                                    <th>Телефон</th>
                                    <th>Статус</th>
                                    <th />
                                </tr>
                            </thead>
                            <tbody>
                                {rows.map((office, i) => (
                                    <tr key={office.id ?? `${office.name}-${i}`}>
                                        <td className="cell-main">{office.name}</td>
                                        <td>{office.city || '—'}</td>
                                        <td>{office.address || '—'}</td>
                                        <td>{office.contactPhone}</td>
                                        <td>
                                            {office.active ? (
                                                <span className="chip white">
                                                    <span className="dot" /> Активен
                                                </span>
                                            ) : (
                                                <span className="chip danger">
                                                    <span className="dot" /> Неактивен
                                                </span>
                                            )}
                                        </td>
                                        <td>
                                            {isAdmin && office.id ? (
                                                <div className="row-actions">
                                                    <button
                                                        type="button"
                                                        className="btn btn-ghost btn-sm"
                                                        title="Редактировать"
                                                        onClick={() => {
                                                        const full = data?.data.find(x => 'id' in x && x.id === office.id);
                                                        setModalOffice((full as OfficeFullResponse | undefined) ?? null);
                                                    }}
                                                    >
                                                        ✎
                                                    </button>
                                                    {office.active && (
                                                        <button
                                                            type="button"
                                                            className="btn btn-ghost btn-sm"
                                                            title="Деактивировать"
                                                            onClick={() => setBlockTarget(office)}
                                                        >
                                                            ⏸
                                                        </button>
                                                    )}
                                                    <button
                                                        type="button"
                                                        className="btn btn-danger btn-sm"
                                                        title="Удалить"
                                                        onClick={() => setDeleteTarget(office)}
                                                    >
                                                        ✕
                                                    </button>
                                                </div>
                                            ) : (
                                                <span className="muted" style={{ fontSize: 13 }}>
                                                    {cityQuery ? 'Результат поиска — только просмотр' : ''}
                                                </span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    <Pagination
                        page={page}
                        size={size}
                        totalPages={data?.totalPages ?? 0}
                        totalElements={data?.totalElements ?? 0}
                        onPageChange={setPage}
                        onSizeChange={s => {
                            setSize(s);
                            setPage(0);
                        }}
                    />
                </>
            ) : (
                <div className="card empty">
                    <div className="e-icon">🏢</div>
                    Офисы не найдены
                </div>
            )}

            {modalOffice !== undefined && (
                <OfficeFormModal
                    office={modalOffice}
                    onClose={() => setModalOffice(undefined)}
                    onSaved={() => {
                        setModalOffice(undefined);
                        reload();
                    }}
                />
            )}

            {blockTarget && (
                <ConfirmDialog
                    title="Деактивация офиса"
                    message={`Деактивировать офис «${blockTarget.name}»?`}
                    confirmLabel="Деактивировать"
                    onClose={() => setBlockTarget(null)}
                    onConfirm={confirmBlock}
                />
            )}

            {deleteTarget && (
                <ConfirmDialog
                    title="Удаление офиса"
                    message={`Точно удалить офис «${deleteTarget.name}»?`}
                    confirmLabel="Удалить"
                    onClose={() => setDeleteTarget(null)}
                    onConfirm={confirmDelete}
                />
            )}
        </>
    );
}
import { useCallback, useEffect, useState } from 'react';
import { adminCardApi } from '../api/endpoints';
import type { CardResponse, CardStatus, PageResponse } from '../types';
import { CardStatusChip, CARD_STATUS_LABELS, CARD_TYPE_LABELS } from '../components/Badges';
import { ConfirmDialog, Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { useToast } from '../ui/ToastContext';

interface FilterState {
    page: number;
    size: number;
    status: string;
    balance: string;
}

const INITIAL_FILTERS: FilterState = { page: 0, size: 10, status: '', balance: '' };

function buildParams(f: FilterState): URLSearchParams {
    const p = new URLSearchParams();
    p.set('page.number', String(f.page));
    p.set('page.size', String(f.size));
    if (f.status) p.set('status', f.status);
    if (f.balance) p.set('balance', f.balance);
    return p;
}

export function AdminCardsPage() {
    const [filters, setFilters] = useState<FilterState>(INITIAL_FILTERS);
    const [data, setData] = useState<PageResponse<CardResponse> | null>(null);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState<string | null>(null);
    const [reloadKey, setReloadKey] = useState(0);
    const [statusTarget, setStatusTarget] = useState<CardResponse | null>(null);
    const [statusValue, setStatusValue] = useState<CardStatus>('ACTIVE');
    const [extendTarget, setExtendTarget] = useState<CardResponse | null>(null);
    const [extendDate, setExtendDate] = useState('');
    const [deleteTarget, setDeleteTarget] = useState<CardResponse | null>(null);
    const toast = useToast();

    const reload = useCallback(() => setReloadKey(k => k + 1), []);

    useEffect(() => {
        let alive = true;
        setLoading(true);
        setLoadError(null);
        adminCardApi
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

    const confirmStatus = async () => {
        if (!statusTarget || !statusTarget.id) return;
        try {
            await adminCardApi.updateStatus(statusTarget.id, statusValue);
            toast('Статус карты обновлён', 'success');
            setStatusTarget(null);
            reload();
        } catch (err) {
            toast(err instanceof Error ? err.message : String(err), 'error');
        }
    };

    const confirmExtend = async () => {
        if (!extendTarget || !extendTarget.id || !extendDate) return;
        try {
            await adminCardApi.extend(extendTarget.id, extendDate);
            toast('Срок карты продлён', 'success');
            setExtendTarget(null);
            reload();
        } catch (err) {
            toast(err instanceof Error ? err.message : String(err), 'error');
        }
    };

    const confirmDelete = async () => {
        if (!deleteTarget || !deleteTarget.id) return;
        try {
            await adminCardApi.remove(deleteTarget.id);
            toast('Карта удалена', 'success');
            setDeleteTarget(null);
            reload();
        } catch (err) {
            toast(err instanceof Error ? err.message : String(err), 'error');
        }
    };

    return (
        <>
            <h2 className="page-title">Карты банка</h2>
            <p className="page-sub">Все выпущенные карты: статусы, балансы и сроки действия</p>

            <div className="banner white">
                <span className="b-icon">ℹ️</span>
                <div className="b-text">
                    <div className="b-title">Ограничение API</div>
                    <div className="b-desc">
                        Операции смены статуса, продления и удаления требуют id карты, но CardResponse его не
                        возвращает. Интерфейс готов — кнопки заработают, когда в card-service добавят поле id.
                    </div>
                </div>
            </div>

            <div className="toolbar">
                <div className="field">
                    <label>Статус</label>
                    <select className="input" value={filters.status} onChange={e => applyFilters({ status: e.target.value })}>
                        <option value="">Все</option>
                        {(Object.keys(CARD_STATUS_LABELS) as CardStatus[]).map(s => (
                            <option key={s} value={s}>
                                {CARD_STATUS_LABELS[s]}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="field">
                    <label>Баланс (равно)</label>
                    <input
                        className="input"
                        inputMode="decimal"
                        value={filters.balance}
                        onChange={e => applyFilters({ balance: e.target.value })}
                        placeholder="1000"
                    />
                </div>
                <button type="button" className="btn btn-ghost" onClick={() => setFilters(INITIAL_FILTERS)}>
                    Сбросить
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
                                    <th>Номер</th>
                                    <th>Тип</th>
                                    <th>Статус</th>
                                    <th>Активация</th>
                                    <th>Срок</th>
                                    <th>Баланс</th>
                                    <th>Лимит</th>
                                    <th />
                                </tr>
                            </thead>
                            <tbody>
                                {data.data.map(card => (
                                    <tr key={card.number}>
                                        <td className="cell-main">{card.number}</td>
                                        <td>{CARD_TYPE_LABELS[card.type] ?? card.type}</td>
                                        <td>
                                            <CardStatusChip status={card.status} />
                                        </td>
                                        <td>{card.dateActivation}</td>
                                        <td>{card.dateExpiry}</td>
                                        <td>{card.balance ?? 0}</td>
                                        <td>{card.spendingLimit ?? '—'}</td>
                                        <td>
                                            <div className="row-actions">
                                                <button
                                                    type="button"
                                                    className="btn btn-ghost btn-sm"
                                                    title={card.id ? 'Изменить статус' : 'Нужен id карты'}
                                                    disabled={!card.id}
                                                    onClick={() => {
                                                        setStatusTarget(card);
                                                        setStatusValue('ACTIVE');
                                                    }}
                                                >
                                                    Статус
                                                </button>
                                                <button
                                                    type="button"
                                                    className="btn btn-ghost btn-sm"
                                                    title={card.id ? 'Продлить срок' : 'Нужен id карты'}
                                                    disabled={!card.id}
                                                    onClick={() => {
                                                        setExtendTarget(card);
                                                        setExtendDate(card.dateExpiry);
                                                    }}
                                                >
                                                    Продлить
                                                </button>
                                                <button
                                                    type="button"
                                                    className="btn btn-danger btn-sm"
                                                    title={card.id ? 'Удалить карту' : 'Нужен id карты'}
                                                    disabled={!card.id}
                                                    onClick={() => setDeleteTarget(card)}
                                                >
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
                    <div className="e-icon">🃏</div>
                    Карты не найдены
                </div>
            )}

            {statusTarget && (
                <Modal title="Изменить статус карты" onClose={() => setStatusTarget(null)}>
                    <div className="field">
                        <label>Карта</label>
                        <span className="cell-main">{statusTarget.number}</span>
                    </div>
                    <div className="field">
                        <label>Новый статус</label>
                        <select className="input" value={statusValue} onChange={e => setStatusValue(e.target.value as CardStatus)}>
                            {(Object.keys(CARD_STATUS_LABELS) as CardStatus[]).map(s => (
                                <option key={s} value={s}>
                                    {CARD_STATUS_LABELS[s]}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="btn btn-ghost" onClick={() => setStatusTarget(null)}>
                            Отмена
                        </button>
                        <button type="button" className="btn btn-primary" onClick={confirmStatus}>
                            Сохранить
                        </button>
                    </div>
                </Modal>
            )}

            {extendTarget && (
                <Modal title="Продлить срок карты" onClose={() => setExtendTarget(null)}>
                    <div className="field">
                        <label>Карта</label>
                        <span className="cell-main">{extendTarget.number}</span>
                    </div>
                    <div className="field">
                        <label>Новая дата окончания</label>
                        <input
                            className="input"
                            type="date"
                            value={extendDate}
                            onChange={e => setExtendDate(e.target.value)}
                        />
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="btn btn-ghost" onClick={() => setExtendTarget(null)}>
                            Отмена
                        </button>
                        <button type="button" className="btn btn-primary" onClick={confirmExtend}>
                            Продлить
                        </button>
                    </div>
                </Modal>
            )}

            {deleteTarget && (
                <ConfirmDialog
                    title="Удаление карты"
                    message={`Точно удалить карту ${deleteTarget.number}?`}
                    confirmLabel="Удалить"
                    onClose={() => setDeleteTarget(null)}
                    onConfirm={confirmDelete}
                />
            )}
        </>
    );
}
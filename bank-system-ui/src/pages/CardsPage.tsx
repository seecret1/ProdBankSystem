import { useCallback, useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { cardApi } from '../api/endpoints';
import type {
    CardReceivingMethod,
    CardRequest,
    CardResponse,
    CardType,
    PageResponse,
} from '../types';
import { CARD_TYPE_LABELS, CardStatusChip } from '../components/Badges';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { useToast } from '../ui/ToastContext';

interface CardForm {
    number: string;
    type: CardType;
    dateActivation: string;
    dateExpiry: string;
    balance: string;
    spendingLimit: string;
    receivingMethod: CardReceivingMethod;
    plannedDeliveryTime: string;
    address: string;
    zipCode: string;
    city: string;
    countryCode: string;
    comment: string;
}

function toDateInput(d: Date): string {
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

function nowPlusYears(years: number): string {
    const d = new Date();
    d.setFullYear(d.getFullYear() + years);
    return toDateInput(d);
}

function initialForm(): CardForm {
    return {
        number: '',
        type: 'DEBIT',
        dateActivation: toDateInput(new Date()),
        dateExpiry: nowPlusYears(3),
        balance: '',
        spendingLimit: '',
        receivingMethod: 'DIGITAL',
        plannedDeliveryTime: '',
        address: '',
        zipCode: '',
        city: '',
        countryCode: 'RU',
        comment: '',
    };
}

function CreateCardModal({ onClose, onSaved }: { onClose: () => void; onSaved: () => void }) {
    const [form, setForm] = useState<CardForm>(initialForm);
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState(false);
    const toast = useToast();

    const set = <K extends keyof CardForm>(key: K, value: CardForm[K]) =>
        setForm(prev => ({ ...prev, [key]: value }));

    const needDelivery = form.receivingMethod === 'DELIVERY_COURIER' || form.type === 'DEBIT_PERSONAL';

    const submit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!/^\d{16}$/.test(form.number.replace(/\s/g, ''))) {
            setError('Номер карты — ровно 16 цифр, без пробелов');
            return;
        }
        if (!form.dateExpiry || form.dateExpiry <= form.dateActivation) {
            setError('Срок действия должен быть позже даты активации');
            return;
        }
        if (needDelivery) {
            if (!form.plannedDeliveryTime) {
                setError('Укажите планируемое время доставки');
                return;
            }
            if (!form.address || !form.city || !form.zipCode || !form.countryCode) {
                setError('Заполните адрес доставки полностью');
                return;
            }
        }
        const payload: CardRequest = {
            number: form.number.replace(/\s/g, ''),
            type: form.type,
            dateActivation: form.dateActivation,
            dateExpiry: form.dateExpiry,
            balance: form.balance ? Number(form.balance) : undefined,
            spendingLimit: form.spendingLimit ? Number(form.spendingLimit) : undefined,
            receivingMethod: form.receivingMethod,
            comment: form.comment || undefined,
        };
        if (needDelivery) {
            payload.cardDeliveryRequest = {
                plannedDeliveryTime: new Date(form.plannedDeliveryTime).toISOString(),
                address: {
                    address: form.address,
                    zipCode: form.zipCode,
                    city: form.city,
                    countryCode: form.countryCode,
                },
            };
        }
        setBusy(true);
        try {
            await cardApi.create(payload);
            toast('Заявка на карту отправлена', 'success');
            onSaved();
        } catch (err) {
            setError(err instanceof Error ? err.message : String(err));
        } finally {
            setBusy(false);
        }
    };

    return (
        <Modal title="Заказать карту" onClose={onClose} size="lg">
            <form onSubmit={submit}>
                <div className="field-grid">
                    <div className="field">
                        <label>Номер карты (16 цифр)</label>
                        <input
                            className="input"
                            inputMode="numeric"
                            maxLength={16}
                            value={form.number}
                            onChange={e => set('number', e.target.value.replace(/\D/g, ''))}
                            placeholder="1234123412341234"
                        />
                    </div>
                    <div className="field">
                        <label>Тип карты</label>
                        <select className="input" value={form.type} onChange={e => set('type', e.target.value as CardType)}>
                            <option value="DEBIT">Дебетовая</option>
                            <option value="DEBIT_PERSONAL">Дебетовая персональная</option>
                            <option value="CREDIT">Кредитная</option>
                        </select>
                    </div>
                    <div className="field">
                        <label>Дата активации</label>
                        <input
                            className="input"
                            type="date"
                            value={form.dateActivation}
                            onChange={e => set('dateActivation', e.target.value)}
                        />
                    </div>
                    <div className="field">
                        <label>Срок действия</label>
                        <input
                            className="input"
                            type="date"
                            value={form.dateExpiry}
                            onChange={e => set('dateExpiry', e.target.value)}
                        />
                    </div>
                    <div className="field">
                        <label>Баланс</label>
                        <input
                            className="input"
                            inputMode="decimal"
                            value={form.balance}
                            onChange={e => set('balance', e.target.value)}
                            placeholder="0"
                        />
                    </div>
                    <div className="field">
                        <label>Лимит трат</label>
                        <input
                            className="input"
                            inputMode="decimal"
                            value={form.spendingLimit}
                            onChange={e => set('spendingLimit', e.target.value)}
                            placeholder="10000"
                        />
                    </div>
                    <div className="field">
                        <label>Способ получения</label>
                        <select
                            className="input"
                            value={form.receivingMethod}
                            onChange={e => set('receivingMethod', e.target.value as CardReceivingMethod)}
                        >
                            <option value="DIGITAL">Цифровая</option>
                            <option value="OFFICE">В офисе</option>
                            <option value="DELIVERY_COURIER">Доставка курьером</option>
                        </select>
                    </div>
                    <div className="field">
                        <label>Комментарий</label>
                        <input className="input" value={form.comment} onChange={e => set('comment', e.target.value)} />
                    </div>
                </div>

                {needDelivery && (
                    <div className="card-pad" style={{ border: '1px solid var(--border)', borderRadius: 14, marginBottom: 16 }}>
                        <div className="section-title" style={{ fontSize: 15, marginBottom: 14 }}>
                            <span className="section-icon yellow">🚚</span> Доставка
                        </div>
                        <div className="field-grid">
                            <div className="field">
                                <label>Планируемое время доставки</label>
                                <input
                                    className="input"
                                    type="datetime-local"
                                    value={form.plannedDeliveryTime}
                                    onChange={e => set('plannedDeliveryTime', e.target.value)}
                                />
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
                    </div>
                )}

                {error && <div className="form-error visible">{error}</div>}
                <div className="modal-actions">
                    <button type="button" className="btn btn-ghost" onClick={onClose}>
                        Отмена
                    </button>
                    <button type="submit" className="btn btn-primary" disabled={busy}>
                        {busy ? 'Отправка…' : 'Заказать карту'}
                    </button>
                </div>
            </form>
        </Modal>
    );
}

function formatBalance(n: number): string {
    return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 }).format(n);
}

export function CardsPage() {
    const [data, setData] = useState<PageResponse<CardResponse> | null>(null);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState<string | null>(null);
    const [reloadKey, setReloadKey] = useState(0);
    const [modalOpen, setModalOpen] = useState(false);

    const reload = useCallback(() => setReloadKey(k => k + 1), []);

    useEffect(() => {
        let alive = true;
        setLoading(true);
        setLoadError(null);
        cardApi
            .myCards(page, size)
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
    }, [page, size, reloadKey]);

    return (
        <>
            <div className="hero">
                <h1>Мои карты</h1>
                <p>
                    Карты банка, выпущенные на ваше имя. Заявка обрабатывается через order-service:
                    статус «Обработка заказа» меняется на «Активна» автоматически после подтверждения.
                </p>
                <div className="chips">
                    <span className="chip white">
                        <span className="dot" />
                        {data?.totalElements ?? 0} карт
                    </span>
                </div>
            </div>

            <div className="toolbar" style={{ justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-primary" onClick={() => setModalOpen(true)}>
                    + Заказать карту
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
                    <div className="card-grid">
                        {data.data.map(card => (
                            <div key={card.number} className="card-visual">
                                <div className="cv-top">
                                    <span>{CARD_TYPE_LABELS[card.type] ?? card.type}</span>
                                    <CardStatusChip status={card.status} />
                                </div>
                                <div className="cv-number">{card.number}</div>
                                <div className="cv-bottom">
                                    <span>
                                        {card.dateActivation} — {card.dateExpiry}
                                    </span>
                                    <span className="cv-balance">{formatBalance(card.balance ?? 0)}</span>
                                </div>
                                {card.status === 'PENDING' && (
                                    <div className="cell-sub" style={{ marginTop: 8 }}>
                                        Статус обновится автоматически после обработки заказа
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                    <Pagination
                        page={page}
                        size={size}
                        totalPages={data.totalPages}
                        totalElements={data.totalElements}
                        onPageChange={setPage}
                        onSizeChange={s => {
                            setSize(s);
                            setPage(0);
                        }}
                    />
                </>
            ) : (
                <div className="card empty">
                    <div className="e-icon">💳</div>
                    У вас пока нет карт. Нажмите «Заказать карту», чтобы оформить первую.
                </div>
            )}

            {modalOpen && (
                <CreateCardModal
                    onClose={() => setModalOpen(false)}
                    onSaved={() => {
                        setModalOpen(false);
                        reload();
                    }}
                />
            )}
        </>
    );
}
import type { CardStatus, CardType, Role, UserStatus } from '../types';

export const ROLE_LABELS: Record<Role, string> = {
    ROLE_USER: 'Клиент',
    ROLE_MANAGER: 'Менеджер',
    ROLE_ADMIN: 'Администратор',
};

export function RoleBadge({ role }: { role: Role }) {
    const cls = role === 'ROLE_ADMIN' ? 'yellow' : role === 'ROLE_MANAGER' ? 'white' : 'muted';
    return <span className={`badge ${cls}`}>{ROLE_LABELS[role] ?? role}</span>;
}

export function StatusChip({ status }: { status: UserStatus }) {
    const cls = status === 'ACTIVE' ? 'white' : status === 'BLOCKED' ? 'danger' : 'yellow';
    const label =
        status === 'ACTIVE' ? 'Активен' : status === 'BLOCKED' ? 'Заблокирован' : 'Ожидает анкеты';
    return (
        <span className={`chip ${cls}`}>
            <span className="dot" />
            {label}
        </span>
    );
}

export function initials(name: string | undefined): string {
    const s = (name ?? '').trim();
    return s ? s[0].toUpperCase() : '?';
}

export const CARD_TYPE_LABELS: Record<CardType, string> = {
    DEBIT: 'Дебетовая',
    DEBIT_PERSONAL: 'Дебетовая персональная',
    CREDIT: 'Кредитная',
};

export const CARD_STATUS_LABELS: Record<CardStatus, string> = {
    PENDING: 'Обработка заказа',
    ACTIVE: 'Активна',
    BLOCKED: 'Заблокирована',
    EXPIRED: 'Истекла',
    EXTENDED: 'Продлена',
};

export const RECEIVING_METHOD_LABELS: Record<string, string> = {
    DIGITAL: 'Цифровая',
    OFFICE: 'В офисе',
    DELIVERY_COURIER: 'Доставка курьером',
};

export function CardStatusChip({ status }: { status: CardStatus }) {
    const cls =
        status === 'ACTIVE' || status === 'EXTENDED'
            ? 'white'
            : status === 'BLOCKED'
              ? 'danger'
              : status === 'EXPIRED'
                ? 'muted'
                : 'yellow';
    return (
        <span className={`chip ${cls}`}>
            <span className="dot" />
            {CARD_STATUS_LABELS[status] ?? status}
        </span>
    );
}
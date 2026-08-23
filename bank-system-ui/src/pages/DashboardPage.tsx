import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

interface QuickCard {
    to: string;
    icon: string;
    cls: 'yellow' | 'white';
    title: string;
    text: string;
}

export function DashboardPage() {
    const { user, hasRole } = useAuth();
    if (!user) return null;

    const cards: QuickCard[] = [
        {
            to: '/profile',
            icon: '🪪',
            cls: 'yellow',
            title: 'Профиль и анкета',
            text: 'Паспортные данные, контактный телефон, логин и пароль — всё в одном месте.',
        },
        {
            to: '/profile',
            icon: '🔒',
            cls: 'white',
            title: 'Безопасность',
            text: 'Смена пароля и актуальность контактных данных вашего аккаунта.',
        },
        {
            to: '/cards',
            icon: '💳',
            cls: 'yellow',
            title: 'Мои карты',
            text: 'Карты банка, заказ новой карты и статус обработки заявки.',
        },
    ];
    if (hasRole('ROLE_ADMIN', 'ROLE_MANAGER')) {
        cards.push(
            {
                to: '/admin/users',
                icon: '👥',
                cls: 'yellow',
                title: 'Пользователи',
                text: 'Список клиентов и сотрудников, поиск, создание и блокировка.',
            },
            {
                to: '/admin/individuals',
                icon: '🗂',
                cls: 'white',
                title: 'Анкеты клиентов',
                text: 'Паспортные данные и контактные телефоны всех клиентов.',
            },
            {
                to: '/admin/cards',
                icon: '🃏',
                cls: 'white',
                title: 'Карты банка',
                text: 'Все выпущенные карты: поиск, фильтр по статусу и балансу.',
            },
            {
                to: '/admin/offices',
                icon: '🏢',
                cls: 'yellow',
                title: 'Офисы',
                text: 'Отделения банка: адреса, график работы, создание и блокировка.',
            },
        );
    }

    return (
        <>
            <div className="hero">
                <h1>
                    Добрый день, <span className="accent">{user.username ?? user.email}</span>!
                </h1>
                <p>Интернет-банк «ПродБанк». Профиль, анкета и безопасность — в одном месте.</p>
                <div className="chips">
                    <span className="chip yellow">
                        <span className="dot" />
                        {user.role ? user.role.replace('ROLE_', '') : 'Клиент'}
                    </span>
                    <span className="chip">
                        <span className="dot" />
                        ID: {user.id}
                    </span>
                </div>
            </div>

            <div className="banner yellow">
                <span className="b-icon">💡</span>
                <div className="b-text">
                    <div className="b-title">Заполните анкету клиента</div>
                    <div className="b-desc">Укажите паспортные данные и телефон — и профиль будет полностью активирован.</div>
                </div>
                <Link to="/profile" className="btn btn-primary btn-sm">
                    Заполнить
                </Link>
            </div>

            <div className="grid grid-3">
                {cards.map(card => (
                    <Link key={card.title} to={card.to} className="quick-card" style={{ textDecoration: 'none' }}>
                        <span className={`qc-icon ${card.cls}`}>{card.icon}</span>
                        <h3>{card.title}</h3>
                        <p>{card.text}</p>
                    </Link>
                ))}
            </div>
        </>
    );
}
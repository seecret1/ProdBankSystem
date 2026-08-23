import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { initials, ROLE_LABELS } from './Badges';

export function Navbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    if (!user) return null;

    const links: Array<{ to: string; label: string }> = [
        { to: '/', label: 'Главная' },
        { to: '/profile', label: 'Профиль' },
        { to: '/cards', label: 'Мои карты' },
    ];
    if (user.roles.includes('ROLE_ADMIN') || user.roles.includes('ROLE_MANAGER')) {
        links.push({ to: '/admin/users', label: 'Пользователи' });
        links.push({ to: '/admin/individuals', label: 'Анкеты' });
        links.push({ to: '/admin/cards', label: 'Карты' });
        links.push({ to: '/admin/offices', label: 'Офисы' });
    }

    const handleLogout = async () => {
        await logout();
        navigate('/login', { replace: true });
    };

    return (
        <header className="navbar">
            <NavLink to="/" className="brand">
                <span className="brand-mark">П</span>
                <span className="brand-name">
                    Прод<em>Банк</em>
                </span>
            </NavLink>
            <nav className="nav-links">
                {links.map(link => (
                    <NavLink
                        key={link.to}
                        to={link.to}
                        className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
                    >
                        {link.label}
                    </NavLink>
                ))}
            </nav>
            <div className="nav-user">
                <span className="nav-avatar">{initials(user.username)}</span>
                <span className="nav-user-meta">
                    <span className="nav-user-name">{user.username ?? user.email}</span>
                    <span className="nav-user-role">{user.role ? ROLE_LABELS[user.role as keyof typeof ROLE_LABELS] ?? user.role : ''}</span>
                </span>
                <button type="button" className="btn btn-ghost btn-sm" onClick={handleLogout}>
                    Выйти
                </button>
            </div>
        </header>
    );
}
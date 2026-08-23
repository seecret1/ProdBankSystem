import { Navigate, Route, Routes } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { SignupPage } from './pages/SignupPage';
import { DashboardPage } from './pages/DashboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { CardsPage } from './pages/CardsPage';
import { AdminUsersPage } from './pages/AdminUsersPage';
import { AdminIndividualsPage } from './pages/AdminIndividualsPage';
import { AdminCardsPage } from './pages/AdminCardsPage';
import { AdminOfficesPage } from './pages/AdminOfficesPage';

export default function App() {
    return (
        <>
            <Navbar />
            <main className="app-main">
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/signup" element={<SignupPage />} />
                    <Route
                        path="/"
                        element={
                            <ProtectedRoute>
                                <DashboardPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <ProfilePage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/cards"
                        element={
                            <ProtectedRoute>
                                <CardsPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/admin/users"
                        element={
                            <ProtectedRoute roles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                                <AdminUsersPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/admin/individuals"
                        element={
                            <ProtectedRoute roles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                                <AdminIndividualsPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/admin/cards"
                        element={
                            <ProtectedRoute roles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                                <AdminCardsPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/admin/offices"
                        element={
                            <ProtectedRoute roles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
                                <AdminOfficesPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
            </main>
        </>
    );
}
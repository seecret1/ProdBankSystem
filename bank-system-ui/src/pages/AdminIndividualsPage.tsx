import { useEffect, useState } from 'react';
import { adminIndividualApi } from '../api/endpoints';
import type { IndividualResponse, PageResponse } from '../types';
import { Pagination } from '../components/Pagination';

export function AdminIndividualsPage() {
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);
    const [data, setData] = useState<PageResponse<IndividualResponse> | null>(null);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState<string | null>(null);

    useEffect(() => {
        let alive = true;
        setLoading(true);
        setLoadError(null);
        adminIndividualApi
            .list(page, size)
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
    }, [page, size]);

    return (
        <>
            <h2 className="page-title">Анкеты клиентов</h2>
            <p className="page-sub">Паспортные данные и контакты (маскируются на сервере)</p>

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
                                    <th>Email</th>
                                    <th>Паспорт</th>
                                    <th>Телефон</th>
                                    <th>Город</th>
                                    <th>Адрес</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data.data.map(i => (
                                    <tr key={i.id}>
                                        <td>
                                            <div className="cell-main">
                                                {i.firstName} {i.lastName}
                                            </div>
                                            <div className="cell-sub">{i.middleName}</div>
                                        </td>
                                        <td>{i.email || '—'}</td>
                                        <td>{i.passportNumber || '—'}</td>
                                        <td>{i.phoneNumber || '—'}</td>
                                        <td>{i.address?.city || '—'}</td>
                                        <td>{i.address?.address || '—'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
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
                    <div className="e-icon">🗂</div>
                    Анкеты не найдены
                </div>
            )}
        </>
    );
}
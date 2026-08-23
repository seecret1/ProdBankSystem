interface PaginationProps {
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
    onPageChange: (page: number) => void;
    onSizeChange: (size: number) => void;
}

const SIZE_OPTIONS = [5, 10, 25, 50];

export function Pagination({ page, size, totalPages, totalElements, onPageChange, onSizeChange }: PaginationProps) {
    return (
        <>
            <div className="toolbar" style={{ marginBottom: 8 }}>
                <div className="field" style={{ maxWidth: 180, flex: '0 0 auto' }}>
                    <label>Показывать</label>
                    <select
                        className="input"
                        value={size}
                        onChange={e => onSizeChange(Number(e.target.value))}
                    >
                        {SIZE_OPTIONS.map(s => (
                            <option key={s} value={s}>
                                {s}
                            </option>
                        ))}
                    </select>
                </div>
            </div>
            <div className="pagination">
                <span>
                    {totalElements} записей · страница {page + 1} из {Math.max(totalPages, 1)}
                </span>
                <div className="pager">
                    <button
                        type="button"
                        className="pager-btn"
                        disabled={page <= 0}
                        onClick={() => onPageChange(page - 1)}
                    >
                        ← Назад
                    </button>
                    <button
                        type="button"
                        className="pager-btn"
                        disabled={page >= totalPages - 1}
                        onClick={() => onPageChange(page + 1)}
                    >
                        Вперёд →
                    </button>
                </div>
            </div>
        </>
    );
}
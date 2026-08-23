import type { ReactNode } from 'react';

interface ModalProps {
    title: string;
    onClose: () => void;
    children: ReactNode;
    size?: 'lg';
}

export function Modal({ title, onClose, children, size }: ModalProps) {
    return (
        <div className="modal-backdrop" onClick={onClose}>
            <div className={`modal ${size === 'lg' ? 'modal-lg' : ''}`} onClick={e => e.stopPropagation()}>
                <div className="modal-head">
                    <h3>{title}</h3>
                    <button type="button" className="modal-close" onClick={onClose} aria-label="Закрыть">
                        ×
                    </button>
                </div>
                {children}
            </div>
        </div>
    );
}

interface ConfirmDialogProps {
    title: string;
    message: string;
    confirmLabel: string;
    onConfirm: () => void;
    onClose: () => void;
}

export function ConfirmDialog({ title, message, confirmLabel, onConfirm, onClose }: ConfirmDialogProps) {
    return (
        <Modal title={title} onClose={onClose}>
            <p className="muted" style={{ lineHeight: 1.6 }}>
                {message}
            </p>
            <div className="modal-actions">
                <button type="button" className="btn btn-ghost" onClick={onClose}>
                    Отмена
                </button>
                <button type="button" className="btn btn-danger" onClick={onConfirm}>
                    {confirmLabel}
                </button>
            </div>
        </Modal>
    );
}
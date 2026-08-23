import { api } from './client';
import type {
    CardRequest,
    CardResponse,
    CardStatus,
    ChangePasswordRequest,
    CreateUserRequest,
    IndividualRequest,
    IndividualResponse,
    JwtAuthenticationDto,
    OfficeCreateRequest,
    OfficeFullResponse,
    OfficeResponse,
    OfficeUpdateRequest,
    PageResponse,
    SignUpRequest,
    UpdateUserRequest,
    UserResponse,
} from '../types';

export const authApi = {
    signInByEmail: (email: string, password: string): Promise<JwtAuthenticationDto> =>
        api('/api/v1/auth/sign-in/email', { method: 'POST', body: { email, password }, auth: false }),

    signInByUsername: (username: string, password: string): Promise<JwtAuthenticationDto> =>
        api('/api/v1/auth/sign-in/username', { method: 'POST', body: { username, password }, auth: false }),

    signUp: (payload: SignUpRequest): Promise<JwtAuthenticationDto> =>
        api('/api/v1/auth/sign-up', { method: 'POST', body: payload, auth: false }),

    signOut: (refreshToken: string): Promise<void> =>
        api('/api/v1/auth/sign-out', { method: 'POST', body: { refreshToken } }),

    changePassword: (payload: ChangePasswordRequest): Promise<JwtAuthenticationDto> =>
        api('/api/v1/auth/change-password', { method: 'POST', body: payload }),
};

export const userApi = {
    updateSelf: (body: UpdateUserRequest): Promise<UserResponse> =>
        api('/api/v1/public/users', { method: 'PATCH', body }),
};

export const individualApi = {
    create: (payload: IndividualRequest): Promise<IndividualResponse> =>
        api('/api/v1/public/individuals', { method: 'POST', body: payload }),

    update: (payload: IndividualRequest): Promise<IndividualResponse> =>
        api('/api/v1/public/individuals', { method: 'PUT', body: payload }),
};

export const adminUserApi = {
    list: (params: URLSearchParams): Promise<PageResponse<UserResponse>> =>
        api(`/api/v1/private/users/filter?${params.toString()}`),

    create: (payload: CreateUserRequest): Promise<UserResponse> =>
        api('/api/v1/private/users', { method: 'POST', body: payload }),

    update: (id: string, payload: CreateUserRequest): Promise<UserResponse> =>
        api(`/api/v1/private/users/${id}`, { method: 'PUT', body: payload }),

    remove: (id: string): Promise<void> =>
        api(`/api/v1/private/users/${id}`, { method: 'DELETE' }),
};

export const adminIndividualApi = {
    list: (number: number, size: number): Promise<PageResponse<IndividualResponse>> =>
        api(`/api/v1/private/individuals?number=${number}&size=${size}`),
};

export const cardApi = {
    myCards: (number: number, size: number): Promise<PageResponse<CardResponse>> =>
        api(`/api/v1/public/cards/your?number=${number}&size=${size}`),

    getByNumber: (cardNumber: string): Promise<CardResponse> =>
        api(`/api/v1/public/cards/card-number/${encodeURIComponent(cardNumber)}`),

    create: (payload: CardRequest): Promise<CardResponse> =>
        api('/api/v1/public/cards', { method: 'POST', body: payload }),
};

export const adminCardApi = {
    list: (params: URLSearchParams): Promise<PageResponse<CardResponse>> =>
        api(`/api/v1/private/cards/filter?${params.toString()}`),

    updateStatus: (id: string, status: CardStatus): Promise<CardResponse> =>
        api(`/api/v1/private/cards/update-status/${id}?status=${status}`, { method: 'PATCH' }),

    extend: (id: string, dateExpiry: string): Promise<CardResponse> =>
        api(`/api/v1/private/cards/extend/${id}?dateExpiry=${dateExpiry}`, { method: 'PATCH' }),

    remove: (id: string): Promise<void> => api(`/api/v1/private/cards/${id}`, { method: 'DELETE' }),

    hardRemove: (id: string): Promise<void> =>
        api(`/api/v1/private/cards/hard-delete/${id}`, { method: 'DELETE' }),
};

export const officeApi = {
    list: (number: number, size: number): Promise<PageResponse<OfficeFullResponse>> =>
        api(`/api/v1/private/offices?number=${number}&size=${size}`),

    findByCity: (city: string, number: number, size: number): Promise<PageResponse<OfficeResponse>> =>
        api(`/api/v1/private/offices/findByCity/${encodeURIComponent(city)}?number=${number}&size=${size}`),

    create: (payload: OfficeCreateRequest): Promise<OfficeFullResponse> =>
        api('/api/v1/private/offices', { method: 'POST', body: payload }),

    update: (id: string, payload: OfficeUpdateRequest): Promise<OfficeResponse> =>
        api(`/api/v1/private/offices/${id}`, { method: 'PUT', body: payload }),

    block: (id: string): Promise<void> => api(`/api/v1/private/offices/${id}`, { method: 'PATCH' }),

    remove: (id: string): Promise<void> => api(`/api/v1/private/offices/${id}`, { method: 'DELETE' }),
};
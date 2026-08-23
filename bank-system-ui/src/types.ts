export type Role = 'ROLE_USER' | 'ROLE_MANAGER' | 'ROLE_ADMIN';

export type UserStatus = 'PENDING_PROFILE' | 'ACTIVE' | 'BLOCKED';

export interface JwtAuthenticationDto {
    token: string;
    refreshToken: string;
}

export interface SignUpRequest {
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
    firstName: string;
    lastName: string;
    middleName?: string | null;
    birthDate: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export interface UpdateUserRequest {
    username?: string;
    email?: string;
}

export interface UserResponse {
    id: string;
    username: string;
    status: UserStatus;
    email: string;
    firstName: string;
    lastName: string;
    middleName?: string | null;
    birthDate?: string | null;
    role: Role;
    createdAt?: string;
    updatedAt?: string;
    deleted?: boolean;
    deletedAt?: string | null;
    deletedBy?: string | null;
}

export interface CreateUserRequest {
    username: string;
    status: UserStatus;
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    middleName?: string | null;
    birthDate?: string | null;
    role: Role;
}

export interface AddressRequest {
    address: string;
    zipCode: string;
    city: string;
    countryCode: string;
}

export interface IndividualRequest {
    passportNumber: string;
    phoneNumber: string;
    address: AddressRequest;
}

export interface AddressResponse {
    address: string;
    zipCode: string;
    city: string;
}

export interface IndividualResponse {
    id: string;
    firstName: string;
    lastName: string;
    middleName?: string | null;
    email: string;
    passportNumber?: string | null;
    phoneNumber?: string | null;
    address?: AddressResponse | null;
}

export interface PageResponse<T> {
    totalElements: number;
    totalPages: number;
    data: T[];
}

export type CardType = 'DEBIT' | 'DEBIT_PERSONAL' | 'CREDIT';

export type CardStatus = 'PENDING' | 'ACTIVE' | 'BLOCKED' | 'EXPIRED' | 'EXTENDED';

export type CardReceivingMethod = 'DIGITAL' | 'OFFICE' | 'DELIVERY_COURIER';

export interface CardResponse {
    id?: string;
    number: string;
    type: CardType;
    dateActivation: string;
    dateExpiry: string;
    status: CardStatus;
    balance: number;
    spendingLimit: number;
    userId: string;
}

export interface CardDeliveryRequest {
    plannedDeliveryTime: string;
    address: AddressRequest;
}

export interface CardRequest {
    number: string;
    type: CardType;
    dateActivation: string;
    dateExpiry: string;
    balance?: number;
    spendingLimit?: number;
    receivingMethod: CardReceivingMethod;
    cardDeliveryRequest?: CardDeliveryRequest;
    comment?: string;
}

export interface ScheduleRequest {
    day: string;
    openingTime: string;
    closingTime: string;
}

export interface OfficeResponse {
    name: string;
    contactPhone: string;
    scheduleJson: unknown;
    active: boolean;
    address: AddressResponse;
}

export interface OfficeFullResponse {
    id: string;
    name: string;
    contactPhone: string;
    scheduleJson: unknown;
    active: boolean;
    address: AddressResponse;
}

export interface OfficeCreateRequest {
    name: string;
    contactPhone: string;
    scheduleJson: ScheduleRequest[];
    address: AddressRequest;
}

export interface OfficeUpdateRequest {
    name: string;
    contactPhone: string;
    scheduleJson: ScheduleRequest[];
}
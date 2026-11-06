export interface AuthUser {
  readonly id: number;
  readonly email: string;
  readonly nom: string;
  readonly prenom: string;
  readonly role: string;
  readonly avatarInitials: string;
}

export interface LoginRequest {
  readonly email: string;
  readonly password: string;
}

export interface LoginResponse {
  readonly token: string;
  readonly user: AuthUser;
}

export interface UserProfileDto {
  readonly id: number;
  readonly email: string;
  readonly nom: string;
  readonly prenom: string;
  readonly role: string;
  readonly avatarInitials: string;
}

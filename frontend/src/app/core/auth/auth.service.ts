import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, map, Observable, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthUser, LoginRequest, LoginResponse, UserProfileDto } from './auth.models';

const TOKEN_KEY = 'ecoli_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly base = environment.apiUrl;

  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  readonly currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    if (this.getToken()) {
      this.loadProfile().subscribe();
    }
  }

  login(email: string, password: string): Observable<AuthUser> {
    const body: LoginRequest = { email, password };
    return this.http.post<LoginResponse>(`${this.base}/auth/login`, body).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN_KEY, res.token);
        this.currentUserSubject.next(res.user);
      }),
      map((res) => res.user)
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.currentUserSubject.next(null);
    void this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  loadProfile(): Observable<AuthUser | null> {
    if (!this.getToken()) {
      this.currentUserSubject.next(null);
      return of(null);
    }
    return this.http.get<UserProfileDto>(`${this.base}/auth/me`).pipe(
      map((dto) => this.dtoToUser(dto)),
      tap((user) => this.currentUserSubject.next(user)),
      catchError(() => {
        this.logout();
        return of(null);
      })
    );
  }

  private dtoToUser(dto: UserProfileDto): AuthUser {
    return {
      id: dto.id,
      email: dto.email,
      nom: dto.nom,
      prenom: dto.prenom,
      role: dto.role,
      avatarInitials: dto.avatarInitials,
    };
  }
}

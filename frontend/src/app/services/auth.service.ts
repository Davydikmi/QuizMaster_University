import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: UserRole;
  groupId?: number;
}

export interface AuthResponse {
  token: string;
  email: string;
  role: UserRole;
}

export interface CurrentUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  enabled: boolean;
  groupId?: number | null;
  groupName?: string | null;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
}

export interface StudyGroup {
  id: number;
  name: string;
  courseNumber: number;
  speciality?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080';
  private readonly tokenKey = 'qm_token';
  private readonly roleKey = 'qm_role';

  private readonly userSubject = new BehaviorSubject<CurrentUser | null>(null);
  readonly user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, payload).pipe(
      tap((response) => this.persistSession(response.token, response.role))
    );
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/register`, payload).pipe(
      tap((response) => this.persistSession(response.token, response.role))
    );
  }

  getStudyGroups(): Observable<StudyGroup[]> {
    return this.http.get<StudyGroup[]>(`${this.apiUrl}/api/study-groups`);
  }

  loadCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${this.apiUrl}/auth/me`).pipe(
      tap((user) => this.userSubject.next(user))
    );
  }

  updateProfile(payload: UpdateProfileRequest): Observable<CurrentUser> {
    const current = this.userSubject.value;
    if (!current) {
      throw new Error('Current user is not loaded');
    }

    return this.http
      .put<CurrentUser>(`${this.apiUrl}/api/users/${current.id}`, {
        id: current.id,
        firstName: payload.firstName,
        lastName: payload.lastName,
        email: payload.email,
        password: payload.password ?? '',
        role: current.role,
        enabled: current.enabled
      })
      .pipe(tap((user) => this.userSubject.next(user)));
  }

  hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRole(): UserRole | null {
    const role = localStorage.getItem(this.roleKey) as UserRole | null;
    return role;
  }

  isAuthenticated(): boolean {
    return this.hasToken();
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.roleKey);
    this.userSubject.next(null);
  }

  private persistSession(token: string, role: UserRole): void {
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.roleKey, role);
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Credentials, User, RegisterData} from '../models/user.models';
import { Observable, BehaviorSubject, tap } from 'rxjs';

export interface LoginResponse {
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser$ = new BehaviorSubject<User | null>(null);
  private readonly apiUrl = 'http://localhost:8080/users';

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  login(credentials: Credentials): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/login`, credentials).pipe(
      tap(user => this.saveUser(user))
    );
  }

  register(data: RegisterData): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/register`, data).pipe(
      tap(user => this.saveUser(user))
    );
  }

  logout(): void {
    localStorage.removeItem('user');
    this.currentUser$.next(null);
  }

  isAuthenticated(): boolean {
    return this.currentUser$.value !== null;
  }

  getCurrentUser(): Observable<User | null> {
    return this.currentUser$.asObservable();
  }

  checkEmailExists(email: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/exists/${email}`);
  }

  getSecurityQuestion(email:string): Observable<string> {
    return this.http.get(`${this.apiUrl}/securityQuestion/${email}`, { 
      responseType: 'text' 
    }) as Observable<string>;
  }

  isValidAnswer(email: string, answer: string): Observable<boolean> {
    return this.http.post<boolean>(`${this.apiUrl}/isValidAnswer`, { email, answer });
  }

  changePassword(email: string, newPassword: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/changePassword`, { email, newPassword });
  }

  private saveUser(user: User): void {
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUser$.next(user);
  }

  private loadUserFromStorage(): void {
    const stored = localStorage.getItem('user');
    if (stored) this.currentUser$.next(JSON.parse(stored));
  }
  
  getCurrentUserEmail(): string | null {
    return this.currentUser$.value?.email || null;
  }
}
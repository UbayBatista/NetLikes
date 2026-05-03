import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Credentials, User, RegisterData} from '../models/user.models';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginResponse {
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUser$ = new BehaviorSubject<User | null>(null);

  
  private readonly apiUrl = 'https://api-db.duckdns.org/users';
  private isLoading$ = new BehaviorSubject<boolean>(true);
  private readonly dbUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  login(credentials: Credentials): Observable<User> {
    return this.http.post<User>(`${this.dbUrl}/login`, credentials).pipe(
      tap(user => this.saveUser(user))
    );
  }

  register(data: RegisterData): Observable<User> {
    return this.http.post<User>(`${this.dbUrl}/register`, data).pipe(
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
    return this.http.get<boolean>(`${this.dbUrl}/exists/${email}`);
  }

  getSecurityQuestion(email:string): Observable<string> {
    return this.http.get(`${this.dbUrl}/securityQuestion/${email}`, { 
      responseType: 'text' 
    }) as Observable<string>;
  }

  isValidAnswer(email: string, answer: string): Observable<boolean> {
    return this.http.post<boolean>(`${this.dbUrl}/isValidAnswer`, { email, answer });
  }

  changePassword(email: string, newPassword: string): Observable<void> {
    return this.http.patch<void>(`${this.dbUrl}/changePassword`, { email, newPassword });
  }

  private saveUser(user: User): void {
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUser$.next(user);
  }

  private loadUserFromStorage(): void {
    const stored = localStorage.getItem('user');
    if(!stored){
      this.isLoading$.next(false);
      return;
    } 

    const user = JSON.parse(stored);
    this.checkEmailExists(user.email).subscribe({
      next: (exists) => {
        if (exists) {
          this.currentUser$.next(user);
        } else {
          localStorage.removeItem('user');
          this.currentUser$.next(null);
        }
        this.isLoading$.next(false);
      },
      error: () => {
        localStorage.removeItem('user');
        this.currentUser$.next(null);
        this.isLoading$.next(false);
      }
    });
  }
  
  getCurrentUserEmail(): string | null {
    return this.currentUser$.value?.email || null;
  }

  isLoading(): Observable<boolean> {
    return this.isLoading$.asObservable();
  }
}
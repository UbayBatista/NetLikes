import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap, take } from 'rxjs';

import { AuthService } from './auth.service';

export interface Follow {
  followerId: string;
  followedId: string;
  state: 'PENDING' | 'ACCEPTED' | 'BLOCKED';
}

export interface LoggedUser {
  email: string;
  username: string;
  foto: string;
}

@Injectable({
  providedIn: 'root',
})
export class FollowService {

  private readonly apiUrl = 'http://localhost:8080/follows';
  private readonly authService = inject(AuthService);

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    
    const userString = localStorage.getItem('user'); 
    let currentUserId = '';

    if (userString) {
      try {
        const user: LoggedUser = JSON.parse(userString);

        currentUserId = user.email; 
        
      } catch (error) {
        console.error('Error al parsear el usuario del localStorage', error);
      }
      }

    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Id': currentUserId 
    });
  }

  requestFollow(targetId: string): Observable<Follow> {
    return this.http.post<Follow>(
      `${this.apiUrl}/${targetId}`, 
      {}, 
      { headers: this.getHeaders() }
    );
  }

  acceptFollow(followerId: string): Observable<Follow> {
    return this.http.post<Follow>(
      `${this.apiUrl}/${followerId}/accept`, 
      {}, 
      { headers: this.getHeaders() }
    );
  }

  blockUser(targetId: string): Observable<Follow> {
    return this.http.post<Follow>(
      `${this.apiUrl}/${targetId}/block`, 
      {}, 
      { headers: this.getHeaders() }
    );
  }

  getFollowing(userId: string): Observable<Follow[]> {
    return this.http.get<Follow[]>(
      `${this.apiUrl}/user/${userId}`, 
      { headers: this.getHeaders() }
    );
  }

  checkFollowStatus(targetEmail: string): Observable<{ state: 'NONE' | 'PENDING' | 'ACCEPTED' }> {
    return this.authService.getCurrentUser().pipe(
      take(1),
      switchMap((currentUser) => {
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'X-User-Id': currentUser?.email || ''
        });
        
        return this.http.get<{ state: 'NONE' | 'PENDING' | 'ACCEPTED' }>(
          `${this.apiUrl}/${targetEmail}/status`, 
          { headers }
        );
      })
    );
  }
}
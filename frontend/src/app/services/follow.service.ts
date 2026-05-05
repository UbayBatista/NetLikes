import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap, take } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

export interface Follow {
  followerId: string;
  followedId: string;
  state: 'PENDING' | 'ACCEPTED' | 'NONE' | 'BLOCKED';
}

export interface LoggedUser {
  email: string;
  userName: string;
  profilePicture: string;
}

@Injectable({
  providedIn: 'root',
})
export class FollowService {

  private readonly apiUrl = `${environment.apiUrl}/follows`;
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

  unblockUser(targetId: string): Observable<Follow> {
    return this.http.post<Follow>(
      `${this.apiUrl}/${targetId}/unblock`, 
      {}, 
      { headers: this.getHeaders() }
    );
  }

  getFollowers(targetEmail: string): Observable<LoggedUser[]> {
    return this.http.get<any[]>(
      `${this.apiUrl}/followersOf/${targetEmail}`, 
      { headers: this.getHeaders() }
    );
  }

  getFollowing(targetEmail: string): Observable<LoggedUser[]> {
    return this.http.get<any[]>(
      `${this.apiUrl}/followsOf/${targetEmail}`, 
      { headers: this.getHeaders() }
    );
  }

  checkFollowStatus(targetEmail: string): Observable<{ state: 'NONE' | 'PENDING' | 'ACCEPTED' | 'BLOCKED' }> {
    return this.authService.getCurrentUser().pipe(
      take(1),
      switchMap((currentUser) => {
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'X-User-Id': currentUser?.email || ''
        });
        
        return this.http.get<{ state: 'NONE' | 'PENDING' | 'ACCEPTED' | 'BLOCKED'}>(
          `${this.apiUrl}/${targetEmail}/status`, 
          { headers }
        );
      })
    );
  }

  unfollow(targetEmail: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${targetEmail}/unfollow`, 
      { headers: this.getHeaders() }
    );
  }

  remove(targetEmail: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${targetEmail}/remove`, 
      { headers: this.getHeaders() }
    );
  }

  getPendingRequests(): Observable<LoggedUser[]> {
    return this.http.get<LoggedUser[]>(
      `${this.apiUrl}/pending`,
      { headers: this.getHeaders() }
    );
  }

  rejectFollow(followerId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${followerId}/reject`,
      { headers: this.getHeaders() }
    );
  }

  getBlockedUsers(): Observable<LoggedUser[]> {
    return this.http.get<LoggedUser[]>(
      `${this.apiUrl}/blocked`, 
      { headers: this.getHeaders() }
    );
  }
}
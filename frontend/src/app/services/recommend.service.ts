import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { AuthService } from './auth.service';
import { switchMap, take } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class RecommendationFollowedService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  
  private apiUrl = 'https://api-db.duckdns.org/api/recommend';

  sendRecommendations(filmId: number, targetEmails: string[]): Observable<any> {
    return this.authService.getCurrentUser().pipe(
      take(1),
      switchMap(user => {
        const headers = new HttpHeaders().set('X-User-Id', user!.email);
        return this.http.post(`${this.apiUrl}/bulk`, { filmId, targetEmails }, { headers, responseType: 'text' });
      })
    );
  }

  getRecentRecipients(): Observable<any[]> {
    return this.authService.getCurrentUser().pipe(
      take(1),
      switchMap(user => {
        const headers = new HttpHeaders().set('X-User-Id', user!.email);
        return this.http.get<any[]>(`${this.apiUrl}/recent`, { headers });
      })
    );
  }

  getRecipientsForFilm(filmId: number): Observable<string[]> {
    return this.authService.getCurrentUser().pipe(
      take(1),
      switchMap(user => {
        const headers = new HttpHeaders().set('X-User-Id', user!.email);
        return this.http.get<string[]>(`${this.apiUrl}/film/${filmId}/recipients`, { headers });
      })
    );
  }

  getRecommendedFilmsWithCount(): Observable<any[]> {
    return this.authService.getCurrentUser().pipe(
      take(1),
      switchMap(user => {
        if (!user || !user.email) return of([]);
        return this.http.get<any[]>(`${this.apiUrl}/stats/${user.email}`);
      })
    );
  }
}
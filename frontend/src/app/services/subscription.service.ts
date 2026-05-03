import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SubscriptionResponse } from '../models/subscription.models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/subscribe_to`;

  getUserSubscriptions(email: string): Observable<SubscriptionResponse[]> {
    return this.http.get<SubscriptionResponse[]>(`${this.baseUrl}/${email}`);
  }

  subscribeToFilm(email: string, filmId: number): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${this.baseUrl}/${email}/film/${filmId}`, {});
  }

  unsubscribeFromFilm(email: string, forumId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${email}/unsubscribe/${forumId}`);
  }
}

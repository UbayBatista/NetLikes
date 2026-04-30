import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
    
  private apiUrl = 'http://localhost:8080/subscribe_to';

  constructor(private http: HttpClient) { }

  getUserSubscription(email: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${email}`);
  }
}
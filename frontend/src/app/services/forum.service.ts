import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ForumService {

  private apiUrl = `${environment.apiUrl}/forum`;

  constructor(private http: HttpClient) { }


  suscribeForum(filmId: number, filmTitle: string,  userEmail: string): Observable<any> {
    const payload = { title: filmTitle,  email: userEmail };
    return this.http.post<any>(`${this.apiUrl}/film/${filmId}`, payload);
  }
}
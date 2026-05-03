import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Genre } from '../models/genre.models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class GenreService {
  private readonly apiUrl = `${environment.apiUrl}/genres`;

  constructor(private http: HttpClient) {}

  getAllGenres(): Observable<Genre[]> {
    return this.http.get<Genre[]>(this.apiUrl);
  }
}
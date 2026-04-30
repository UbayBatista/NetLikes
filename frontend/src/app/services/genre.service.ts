import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Genre } from '../models/genre.models';

@Injectable({ providedIn: 'root' })
export class GenreService {
  private readonly apiUrl = 'https://api-db.duckdns.org/genres';

  constructor(private http: HttpClient) {}

  getAllGenres(): Observable<Genre[]> {
    return this.http.get<Genre[]>(this.apiUrl);
  }
}
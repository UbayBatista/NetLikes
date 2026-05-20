import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, throwError, map } from 'rxjs';
import { GenreGroup } from '../models/film.models';
import { environment } from '../../environments/environment';

export interface LoggedUser {
  email: string;
  userName: string;
  profilePicture: string;
}

@Injectable({
  providedIn: 'root',
})
export class Recommendations {
  private readonly apiUrl = `${environment.apiUrl}/api/recommendations`;

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

  getRecommendations(): Observable<GenreGroup[]> {
    return this.http.get<any[]>(
      `${this.apiUrl}`, 
      { headers: this.getHeaders() }
    ).pipe(
        map(films => this.mappingByGenre(films)),
        catchError(this.handleError)
      );
  }

  private handleError(): Observable<never> {
    return throwError(() => new Error('Something went wrong; please try again later.'));
  }

  private mappingByGenre(films: any[]): GenreGroup[] {
    const genreMap: { [key: string]: any[] } = {};

    films.forEach(film => {

      film.genres.forEach((genre: string) => {
        if (!genreMap[genre]) {
          genreMap[genre] = [];
        }

        genreMap[genre].push({
          id: film.id,
          title: film.title,
          posterPath: film.posterPath
        });
      });

    });

    return Object.keys(genreMap).map(name => ({
      name,
      films: genreMap[name]
    }));
  }
}

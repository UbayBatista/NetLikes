import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError, map, of } from 'rxjs';
import { Film, FilmListItem, GenreGroup } from '../models/film.models';

@Injectable({
  providedIn: 'root',
})
export class FilmService {
  readonly dbUrl = 'http://localhost:8080/films';

  constructor(private http: HttpClient) {}
  
  getFilms(): Observable<FilmListItem[]> {
    return this.http.get<FilmListItem[]>(this.dbUrl)
    .pipe(catchError(this.handleError));
  }

  getFilmsByGenre(): Observable<GenreGroup[]> {
    return this.http.get<any[]>(this.dbUrl).pipe(
      map(films => this.mappingByGenre(films)),
      catchError(this.handleError)
    );
  }

  getFilmById(id: number): Observable<Film> {
    return this.http.get<Film>(`${this.dbUrl}/${id}`)
    .pipe(catchError(this.handleError));
  }

  searchBy(query: string | null): Observable<any[]> {
    if (!query || query.trim() === '') {
      return of([]);
    }
    return this.http.get<any[]>(`${this.dbUrl}/search?query=${query}`).pipe(
      catchError(error => {
        console.error('Error en la búsqueda', error);
        return of([]);
      })
    );
  }

  private handleError(error: any): Observable<never> {
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


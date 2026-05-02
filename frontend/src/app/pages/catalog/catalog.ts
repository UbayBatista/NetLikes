

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Genre } from "../../components/genre/genre";
import { FormsModule } from '@angular/forms';
import { SearchBarComponent } from '../../components/search-bar/search-bar';
import { FilmService } from '../../services/film.service';
import { GenreGroup } from '../../models/film.models';
import { Film } from '../../components/film/film';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';


@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [Film, Genre, CommonModule, FormsModule, SearchBarComponent],
  templateUrl: './catalog.html',
  styleUrl: './catalog.css',
})
export class Catalog implements OnInit {
  genres: GenreGroup[] = [];

  searchResults: Film[] = []; 
  isSearching = false;

  private searchSubject = new Subject<string>();

  constructor(private filmService: FilmService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadFilms();

    this.setupSearchPipeline();
  }

  loadFilms() {
    this.filmService.getFilmsByGenre().subscribe({
      next: (data) => {
        this.genres = data;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error fetching films:', error);
      }
    });
  }

  private setupSearchPipeline() {
    this.searchSubject.pipe(
      debounceTime(300), 
      distinctUntilChanged(),
      switchMap((query) => {
        if (!query || query.trim() === '') {
          this.isSearching = false;
          return of([]); 
        }

        this.isSearching = true;

        return this.filmService.searchBy(query).pipe(
          catchError(err => {
            console.error('Error en la búsqueda del servidor', err);
            return of([]);
          })
        );
      })
    ).subscribe(results => {
      this.searchResults = results;
      this.cdr.detectChanges();
    });
  }

  filters(search: string) {
    this.searchSubject.next(search);
  }
}

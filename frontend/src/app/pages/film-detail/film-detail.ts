import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FilmService } from '../../services/film.service';
import { FilmHeader } from '../../components/film-header/film-header';
import { FilmCast } from '../../components/film-cast/film-cast';
import { FilmTrailers } from '../../components/film-trailers/film-trailers';
import { Film } from '../../models/film.models';

@Component({
  selector: 'app-film-detail',
  standalone: true,
  imports: [
    FilmHeader, 
    FilmCast, 
    FilmTrailers
  ],
  templateUrl: './film-detail.html',
  styleUrl: './film-detail.css'
})
export class FilmDetail implements OnInit {
  
  filmData: Film | null = null;

  private route = inject(ActivatedRoute);
  private filmService = inject(FilmService);

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      const filmId = Number(idParam);
      
      this.filmService.getFilmById(filmId).subscribe({
        next: (realDataFromBackend) => {
          this.filmData = realDataFromBackend;
          this.cdr.detectChanges();
        }
      });
    }
  }
}
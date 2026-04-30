import { Component, Input, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Film } from '../../models/film.models';
import { ForumService } from '../../services/forum.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-film-header',
  standalone: true,
  imports: [DatePipe], 
  templateUrl: './film-header.html',
  styleUrl: './film-header.css',
})
export class FilmHeader implements OnInit {
  [x: string]: any;
  @Input() film!: Film;

  readonly imgBaseUrl = 'https://image.tmdb.org/t/p/w500';

  dominantColor = 'rgba(255, 255, 255, 0)'; 
  isWatched = false;
  isWatchLater = false;
  currentRating: string | null = null; 

  private cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    if (this.film?.posterPath) {
      this.extractColorFromImage(this.imgBaseUrl + this.film.posterPath);
    }
  }

  formatGenres(): string {
    return this.film?.genres?.length 
      ? this.film.genres.join(', ').replace(/, ([^,]*)$/, ' y $1') 
      : '';
  }

  formatRuntime(minutes: number | undefined): string {
    if (!minutes || minutes <= 0) return '';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    
    if (hours === 0) return `${mins}min`;
    return mins === 0 ? `${hours}h` : `${hours}h ${mins}min`;
  }

  toggleWatched() { 
    this.isWatched = !this.isWatched; 
    if (this.isWatched) this.isWatchLater = false; 
    else this.currentRating = null; 
  }

  toggleWatchLater() { 
    this.isWatchLater = !this.isWatchLater; 
    if (this.isWatchLater) {
      this.isWatched = false;
      this.currentRating = null;
    }
  }

  rateFilm(rating: string) { 
    if (!this.isWatched) return; 
    this.currentRating = this.currentRating === rating ? null : rating;
  }

  shareFilm() { console.log('Compartir película:', this.film.title); }

  extractColorFromImage(imageUrl: string) {
    const img = new Image();
    img.crossOrigin = "anonymous";
    img.src = `${imageUrl}?t=${new Date().getTime()}`;

    img.onload = () => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d', { willReadFrequently: true });
      if (!ctx) return;
      canvas.width = img.width; canvas.height = img.height;
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
      const data = ctx.getImageData(0, 0, canvas.width, canvas.height).data;
      let r = 0, g = 0, b = 0, count = 0;
      for (let i = 0; i < data.length; i += 100) { 
        r += data[i]; g += data[i + 1]; b += data[i + 2]; count++;
      }
      this.dominantColor = `rgba(${~~(r/count)}, ${~~(g/count)}, ${~~(b/count)}, 0.35)`;
      this.cdr.detectChanges();
    };
  }

  constructor(private forumService: ForumService, private authService: AuthService) {}

  forumUrl: string | null = null;
  
  suscribeToForum(filmId: number, filmTitle: string) {
    console.log('Botón pulsado. Enviando petición a Spring Boot...');

    this.authService.getCurrentUser().subscribe(user => { 

        if (!user || !user.email) {
          alert('¡Debes iniciar sesión para poder suscribirte a un foro!');
          return; 
        }
      
      
      console.log('Usuario detectado:', user.email, 'Enviando petición a Spring Boot...')

      this.forumService.suscribeForum(filmId, filmTitle, user.email).subscribe({
        next: (response) => {
          console.log('¡Éxito! El ID del foro en Discourse es:', response.discourseTopicId);

          this.forumUrl = `http://localhost/t/${response.discourseTopicId}`;

          alert('¡Foro creado/obtenido con éxito! ID: ' + response.discourseTopicId);

        },
        error: (error) => {
          console.error('Ha ocurrido un error:', error);
          alert('Error al crear el foro. Revisa la consola.');
        }
      });
    
    });

  }
}

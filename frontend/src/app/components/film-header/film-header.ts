import { Component, Input, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Film } from '../../models/film.models';
import { UserInteractionService } from '../../services/user-interaction.service';
import { SubscriptionService } from '../../services/subscription.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-film-header',
  standalone: true,
  imports: [DatePipe], 
  templateUrl: './film-header.html',
  styleUrl: './film-header.css',
})
export class FilmHeader implements OnInit {
  @Input() film!: Film;

  readonly imgBaseUrl = 'https://image.tmdb.org/t/p/w500';

  dominantColor = 'rgba(255, 255, 255, 0)'; 
  isWatched = false;
  isWatchLater = false;
  currentRating: string | null = null; 
  isSubscribed = false;

  private cdr = inject(ChangeDetectorRef);
  private interactionService = inject(UserInteractionService);
  private subscriptionService = inject(SubscriptionService);
  private authService = inject(AuthService);

  ngOnInit(): void {
    if (this.film?.posterPath) {
      this.extractColorFromImage(this.imgBaseUrl + this.film.posterPath);
    }
    this.loadInitialMarkStatus();
    this.loadInitialRateStatus();
    this.loadInitialSubscriptionStatus();
  }

  private loadInitialMarkStatus() {
    if (!this.film?.id) return;
    this.interactionService.getMarkStatus(this.film.id).subscribe({
      next: (mark) => {
        if (mark) {
          this.isWatched = (mark.type === 'SEEN');
          this.isWatchLater = (mark.type === 'WATCHLATER');
        } else {
          this.isWatched = false;
          this.isWatchLater = false;
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al recuperar estado', err)
    });
  }

  private loadInitialRateStatus() {
    if (!this.film?.id) return;
    this.interactionService.getRateStatus(this.film.id).subscribe({
        next: (rate) => {
            if (rate) this.currentRating = rate.score.toLowerCase();
            this.cdr.detectChanges();
        }
    });
  }

  private loadInitialSubscriptionStatus() {
    if (!this.film?.id) return;
    
    this.authService.getCurrentUser().subscribe(user => {
      if (user && user.email) {
        this.subscriptionService.getUserSubscriptions(user.email).subscribe({
          next: (subs) => {
            this.isSubscribed = subs.some(s => s.forum.film.id === this.film.id);
            this.cdr.detectChanges();
          }
        });
      }
    });
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
    const previousState = this.isWatched;
    this.isWatched = !this.isWatched;
    if (this.isWatched) {
      this.isWatchLater = false;
    } else {
      this.currentRating = null;
    }
    
    this.cdr.detectChanges(); 

    this.interactionService.toggleMark(this.film.id, 'SEEN').subscribe({
      error: (err) => {
        this.isWatched = previousState;
        this.cdr.detectChanges();
        console.error('Error en servidor, revirtiendo cambio visual', err);
      }
    });
  }

  toggleWatchLater() { 
    const previousState = this.isWatchLater;
    this.isWatchLater = !this.isWatchLater;
    if (this.isWatchLater) {
      this.isWatched = false;
      this.currentRating = null;
    }
    
    this.cdr.detectChanges(); 

    this.interactionService.toggleMark(this.film.id, 'WATCHLATER').subscribe({
      error: (err) => {
        this.isWatchLater = previousState;
        this.cdr.detectChanges();
        console.error('Error', err);
      }
    });
  }

  rateFilm(rating: string) { 
    if (!this.isWatched) return; 

    const oldRating = this.currentRating;
    this.currentRating = this.currentRating === rating ? null : rating;
    
    this.cdr.detectChanges(); 

    this.interactionService.toggleRate(this.film.id, rating).subscribe({
        error: (err) => {
            this.currentRating = oldRating;
            this.cdr.detectChanges();
        }
    });
  }

  shareFilm() { 
    //TODO: Compartir = Recomendar (en tu perfil y en chat).
  }

  toggleSubscription() {
    console.log('Botón de suscripción pulsado...');
    
    const previousState = this.isSubscribed;
    this.isSubscribed = !this.isSubscribed;
    this.cdr.detectChanges();

    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        if (!user || !user.email) {
          alert('¡Debes iniciar sesión para poder suscribirte a un foro!');
          this.revertSubscription(previousState);
          return;
        }

        const userEmail = user.email;
        console.log('Usuario detectado:', userEmail, 'Enviando petición a Spring Boot...');

        if (this.isSubscribed) {
          this.subscriptionService.subscribeToFilm(userEmail, this.film.id).subscribe({
            next: () => console.log('¡Suscripción exitosa en el backend!'),
            error: (err) => {
              console.error('Error al suscribirse:', err);
              const errorMsg = err.error ? (typeof err.error === 'string' ? err.error : JSON.stringify(err.error)) : err.message;
              alert('Fallo al suscribirse: ' + errorMsg);
              this.revertSubscription(previousState);
            }
          });
        } else {
          this.subscriptionService.unsubscribeFromFilm(userEmail, this.film.id).subscribe({
            next: () => console.log('¡Desuscripción exitosa en el backend!'),
            error: (err) => {
              console.error('Error al desuscribirse:', err);
              alert('Error al desuscribirse. Revisa la consola.');
              this.revertSubscription(previousState);
            }
          });
        }
      },
      error: (err) => {
        console.error('Error del AuthService:', err);
        this.revertSubscription(previousState);
      }
    });
  }

  private revertSubscription(state: boolean) {
    this.isSubscribed = state;
    this.cdr.detectChanges();
  }

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
}
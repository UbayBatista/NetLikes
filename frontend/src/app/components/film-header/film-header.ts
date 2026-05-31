import { Component, Input, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Film } from '../../models/film.models';
import { UserInteractionService } from '../../services/user-interaction.service';
import { SubscriptionService } from '../../services/subscription.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ForumService } from '../../services/forum.service';
import { RecommendPanel } from '../recommend-panel/recommend-panel';

@Component({
  selector: 'app-film-header',
  standalone: true,
  imports: [DatePipe, RecommendPanel], 
  templateUrl: './film-header.html',
  styleUrl: './film-header.css',
})
export class FilmHeader implements OnInit {
  @Input() film!: Film;

  readonly imgBaseUrl = 'https://image.tmdb.org/t/p/w500';

  isProcessingForum = false;
  dominantColor = 'rgba(255, 255, 255, 0)'; 
  isWatched = false;
  isWatchLater = false;
  currentRating: string | null = null; 
  isSubscribed = false;
  isRecommendPanelOpen = false;
  isRecommendedProfile = false;

  private cdr = inject(ChangeDetectorRef);
  private interactionService = inject(UserInteractionService);
  private subscriptionService = inject(SubscriptionService);
  private authService = inject(AuthService);
  private router = inject(Router);

  constructor(private forumService: ForumService) {}

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
      next: (response) => {
        if (response && response.types) {
          this.isWatched = response.types.includes('SEEN');
          this.isWatchLater = response.types.includes('WATCHLATER');
          this.isRecommendedProfile = response.types.includes('RECOMMENDED');
        } else {
          this.isWatched = false;
          this.isWatchLater = false;
          this.isRecommendedProfile = false;
        }
        this.cdr.detectChanges();
      }
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
      if (this.isSubscribed) {
        this.toggleSubscription();
      }
    }
    
    this.cdr.detectChanges();

    this.interactionService.toggleMark(this.film.id, 'SEEN').subscribe({
      error: () => {
        this.isWatched = previousState;
        this.cdr.detectChanges();
      }
    });
  }

  toggleWatchLater() { 
    const previousState = this.isWatchLater;
    this.isWatchLater = !this.isWatchLater;
    
    if (this.isWatchLater) {
      this.isWatched = false;
      this.currentRating = null;

      if (this.isSubscribed) {
        this.toggleSubscription();
      }
    }
    
    this.cdr.detectChanges(); 

    this.interactionService.toggleMark(this.film.id, 'WATCHLATER').subscribe({
      error: () => {
        this.isWatchLater = previousState;
        this.cdr.detectChanges();
      }
    });
  }

  rateFilm(rating: string) { 
    if (!this.isWatched) return; 

    const oldRating = this.currentRating;
    this.currentRating = this.currentRating === rating ? null : rating;
    
    this.cdr.detectChanges(); 

    this.interactionService.toggleRate(this.film.id, rating).subscribe({
        error: () => {
            this.currentRating = oldRating;
            this.cdr.detectChanges();
        }
    });
  }

  toggleSubscription() {
    if (this.isProcessingForum) return;
    
    if (!this.isSubscribed && !this.isWatched) {
      return;
    }

    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        if (!user || !user.email) {
          return;
        }

        const userEmail = user.email;

        this.isProcessingForum = true;
        this.cdr.detectChanges();

        if (!this.isSubscribed) {
          this.subscriptionService.subscribeToFilm(userEmail, this.film.id, this.film.title).subscribe({
            next: () => {
              this.isSubscribed = true; 
              this.isProcessingForum = false; 
              this.cdr.detectChanges();
              this.router.navigate(['/forum'], { queryParams: { filmId: this.film.id } });
            },
            error: (err) => {
              const errorMsg = err.error ? (typeof err.error === 'string' ? err.error : JSON.stringify(err.error)) : err.message;
              this.isProcessingForum = false; 
              this.cdr.detectChanges();
            }
          });
        } else {
          this.subscriptionService.unsubscribeFromFilm(userEmail, this.film.id).subscribe({
            next: () => {
              this.isSubscribed = false; 
              this.isProcessingForum = false; 
              this.cdr.detectChanges();
            },
            error: () => {
              this.isProcessingForum = false; 
              this.cdr.detectChanges();
            }
          });
        }
      }
    });
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

  shareFilm() { 
    this.isRecommendPanelOpen = true; 
  }

  updateRecommendationStatus(newState: boolean) {
    this.isRecommendedProfile = newState;
    this.cdr.detectChanges();
  }

  refreshRecommendationStatus() {
    this.loadInitialMarkStatus();
  }

  closeRecommendPanel() {
    this.isRecommendPanelOpen = false;
  }
}

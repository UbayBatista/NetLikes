import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { FilmHeader } from './film-header';
import { UserInteractionService } from '../../services/user-interaction.service';
import { of, throwError } from 'rxjs'
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { AuthService } from '../../services/auth.service';
import { SubscriptionService } from '../../services/subscription.service';
import { ForumService } from '../../services/forum.service';

describe('FilmHeader Component', () => {
  let component: FilmHeader;
  let fixture: ComponentFixture<FilmHeader>;
  let interactionServiceMock: any;
  let authServiceMock: any;
  let subscriptionServiceMock: any;
  let forumServiceMock: any;

  const mockFilm = {
    id: 1,
    title: 'Película de Prueba',
    releaseDate: '2024-01-01',
    overView: 'Una descripción',
    tagLine: 'Un eslogan épico',
    runtime: 120,
    genres: ['Acción'],
    watchProviders: [{ id: 1, name: 'Netflix', logo: '/logo.png' }],
    posterPath: '/path.jpg',
    ageRating: '18+',
    adult: false,
    cast: [],
    videos: []
  };

  authServiceMock = {
    getCurrentUser: vi.fn().mockReturnValue(of({ email: 'test@test.com' }))
  };
  subscriptionServiceMock = {
    getUserSubscriptions: vi.fn().mockReturnValue(of([])),
    subscribeToFilm: vi.fn().mockReturnValue(of({})),
    unsubscribeFromFilm: vi.fn().mockReturnValue(of({}))
  };
  forumServiceMock = {
    suscribeForum: vi.fn().mockReturnValue(of({ discourseTopicId: 123 }))
  };

  beforeEach(async () => {
    interactionServiceMock = {
      getMarkStatus: vi.fn().mockReturnValue(of(null)),
      getRateStatus: vi.fn().mockReturnValue(of(null)),
      toggleMark: vi.fn().mockReturnValue(of({ status: 'success' })),
      toggleRate: vi.fn().mockReturnValue(of({ score: 'LIKE' }))
    };

    await TestBed.configureTestingModule({
      imports: [FilmHeader],
      providers: [
        provideRouter([]),
        { provide: UserInteractionService, useValue: interactionServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: SubscriptionService, useValue: subscriptionServiceMock },
        { provide: ForumService, useValue: forumServiceMock }
              ]
    }).compileComponents();

    fixture = TestBed.createComponent(FilmHeader);
    component = fixture.componentInstance;

    component.film = { ...mockFilm, id: 1 } as any;
    vi.spyOn(component, 'extractColorFromImage').mockImplementation(() => {})
  });

  describe('UI Rendering Logic', () => {
    it('should display the tagline if it exists (US 2.1)', () => {
      component.film = { ...mockFilm } as any;
      
      fixture.detectChanges(); 

      const compiled = fixture.nativeElement;
      expect(compiled.textContent).toContain('Un eslogan épico');
      expect(compiled.querySelector('h5.fw-bold.mt-4')).toBeTruthy();
    });

    it('should not display the Tagline section if film.tagLine is empty', () => {
      component.film = { ...mockFilm, tagLine: '' } as any;
      
      fixture.detectChanges(); 
      
      const compiled = fixture.nativeElement;
      const headers = Array.from(compiled.querySelectorAll('h5'));
      const hasSlogan = headers.some((h: any) => h.textContent.includes('Eslogan'));
      
      expect(hasSlogan).toBeFalsy();
    });

    it('should not display the "Where to watch" section if there are no providers', () => {
      component.film = { ...mockFilm, watchProviders: [] } as any;
      fixture.detectChanges();

      const compiled = fixture.nativeElement;
      const headers = Array.from(compiled.querySelectorAll('h5'));
      const hasProviders = headers.some((h: any) => h.textContent.includes('Dónde ver'));
      
      expect(hasProviders).toBeFalsy();
    });

    it('should not display the "Description" header if film.overView is empty', () => {
      component.film = { ...mockFilm, overView: '' } as any;
      
      fixture.detectChanges(); 
      
      const compiled = fixture.nativeElement;
      const headers = Array.from(compiled.querySelectorAll('h5'));
      const hasDescriptionHeader = headers.some((h: any) => h.textContent.includes('Descripción'));
      
      expect(hasDescriptionHeader).toBeFalsy();
    });

    it('should not display "Where to watch" if watchProviders is empty', () => {
      component.film = { ...mockFilm, watchProviders: [] } as any;
      fixture.detectChanges();

      const headers = Array.from(fixture.nativeElement.querySelectorAll('h5'));
      const hasProviders = headers.some((h: any) => h.textContent.includes('Dónde ver'));
      expect(hasProviders).toBeFalsy();
    });

    it('should not display "Description" header if overView is empty', () => {
      component.film = { ...mockFilm, overView: '' } as any;
      fixture.detectChanges();

      const headers = Array.from(fixture.nativeElement.querySelectorAll('h5'));
      const hasDescription = headers.some((h: any) => h.textContent.includes('Descripción'));
      expect(hasDescription).toBeFalsy();
    });
  });

  describe('Movie Tracking Logic (US 4.1)', () => {
    
    it('should add to "Watched" and set isWatched to true', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges(); 
      
      component.isWatched = false;
      component.toggleWatched();
      
      expect(component.isWatched).toBe(true);
      expect(interactionServiceMock.toggleMark).toHaveBeenCalledWith(1, 'SEEN');
    });

    it('should remove from "Watch Later" if added to "Watched" (Cross-list logic)', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatchLater = true;
      component.toggleWatched();
      
      expect(component.isWatched).toBe(true);
      expect(component.isWatchLater).toBe(false);
    });

    it('should remove from "Watched" and clear rating if added to "Watch Later"', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatched = true;
      component.currentRating = 'like';
      component.toggleWatchLater();
      
      expect(component.isWatchLater).toBe(true);
      expect(component.isWatched).toBe(false);
      expect(component.currentRating).toBeNull();
    });

    it('should revert the "Watched" change if the server throws an error', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      interactionServiceMock.toggleMark.mockReturnValue(throwError(() => new Error('Server Error')));
      
      component.isWatched = false;
      component.toggleWatched(); 
      
      expect(component.isWatched).toBe(false);
    });
  });

  describe('Recommendation Logic (US 10.2)', () => {
    it('should open the recommend panel when share button is clicked', () => {
      component.shareFilm();
      expect(component.isRecommendPanelOpen).toBe(true);
    });

    it('should close the recommend panel when closeRecommendPanel is called', () => {
      component.isRecommendPanelOpen = true;
      component.closeRecommendPanel();
      expect(component.isRecommendPanelOpen).toBe(false);
    });

    it('should update isRecommendedProfile when status changes in the panel', () => {
      component.updateRecommendationStatus(true);
      expect(component.isRecommendedProfile).toBe(true);
      
      component.updateRecommendationStatus(false);
      expect(component.isRecommendedProfile).toBe(false);
    });

    it('should load initial recommendation status from getMarkStatus', () => {
      interactionServiceMock.getMarkStatus.mockReturnValue(of({ 
        types: ['SEEN', 'RECOMMENDED'] 
      }));
      
      // Forzamos la carga inicial
      (component as any).loadInitialMarkStatus();
      
      expect(component.isRecommendedProfile).toBe(true);
      expect(component.isWatched).toBe(true);
    });
  });

  describe('Movie Rating Logic (US 9.1)', () => {

    it('should not allow rating if the movie is not watched', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatched = false;
      component.rateFilm('love');
      
      expect(component.currentRating).toBeNull();
      expect(interactionServiceMock.toggleRate).not.toHaveBeenCalled();
    });

    it('should apply the rating if the movie is watched', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatched = true;
      component.rateFilm('like');
      
      expect(component.currentRating).toBe('like');
      expect(interactionServiceMock.toggleRate).toHaveBeenCalledWith(1, 'like');
    });

    it('should remove the rating if the same active rating is clicked', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatched = true;
      component.currentRating = 'love';
      component.rateFilm('love');
      
      expect(component.currentRating).toBeNull();
      expect(interactionServiceMock.toggleRate).toHaveBeenCalledWith(1, 'love');
    });

    it('should modify the rating if a different button is clicked', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatched = true;
      component.currentRating = 'dislike';
      component.rateFilm('love');
      
      expect(component.currentRating).toBe('love');
      expect(interactionServiceMock.toggleRate).toHaveBeenCalledWith(1, 'love');
    });
  });
});
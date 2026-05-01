import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { FilmHeader } from './film-header';
import { UserInteractionService } from '../../services/user-interaction.service';
import { of, throwError } from 'rxjs'
import { vi } from 'vitest';

describe('FilmHeader', () => {
  let component: FilmHeader;
  let fixture: ComponentFixture<FilmHeader>;
  let interactionServiceMock: any;

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
        { provide: UserInteractionService, useValue: interactionServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FilmHeader);
    component = fixture.componentInstance;

    component.film = { ...mockFilm, id: 1 } as any;
    vi.spyOn(component, 'extractColorFromImage').mockImplementation(() => {})
  });

  it('debería mostrar el eslogan si existe (HU 2.1)', () => {
    component.film = { ...mockFilm } as any;
    
    fixture.detectChanges(); 

    const compiled = fixture.nativeElement;
    expect(compiled.textContent).toContain('Un eslogan épico');
    expect(compiled.querySelector('h5.fw-bold.mt-4')).toBeTruthy();
  });

  it('NO debería mostrar la sección de Eslogan si film.tagLine está vacío', () => {
    component.film = { ...mockFilm, tagLine: '' } as any;
    
    fixture.detectChanges(); 
    
    const compiled = fixture.nativeElement;
    const headers = Array.from(compiled.querySelectorAll('h5'));
    const hasSlogan = headers.some((h: any) => h.textContent.includes('Eslogan'));
    
    expect(hasSlogan).toBeFalsy();
  });

  it('NO debería mostrar la sección "Dónde ver" si no hay proveedores', () => {
    component.film = { ...mockFilm, watchProviders: [] } as any;
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const headers = Array.from(compiled.querySelectorAll('h5'));
    const hasProviders = headers.some((h: any) => h.textContent.includes('Dónde ver'));
    
    expect(hasProviders).toBeFalsy();
  });

  it('NO debería mostrar el encabezado de "Descripción" si film.overView está vacío', () => {
    component.film = { ...mockFilm, overView: '' } as any;
    
    fixture.detectChanges(); 
    
    const compiled = fixture.nativeElement;
    const headers = Array.from(compiled.querySelectorAll('h5'));
    const hasDescriptionHeader = headers.some((h: any) => h.textContent.includes('Descripción'));
    
    expect(hasDescriptionHeader).toBeFalsy();
  });

  describe('HU4.1: Seguimiento de películas (Listas)', () => {
    
    it('debería añadir a "Vistas" y marcar isWatched como true', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges(); 
      
      component.isWatched = false;
      component.toggleWatched();
      
      expect(component.isWatched).toBe(true);
      expect(interactionServiceMock.toggleMark).toHaveBeenCalledWith(1, 'SEEN');
    });

    it('debería eliminar de "Ver más tarde" si se añade a "Vistas" (Listas cruzadas)', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatchLater = true;
      component.toggleWatched();
      
      expect(component.isWatched).toBe(true);
      expect(component.isWatchLater).toBe(false);
    });

    it('debería eliminar de "Vistas" y borrar la valoración si se añade a "Ver más tarde"', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatched = true;
      component.currentRating = 'like';
      component.toggleWatchLater();
      
      expect(component.isWatchLater).toBe(true);
      expect(component.isWatched).toBe(false);
      expect(component.currentRating).toBeNull();
    });

    it('debería revertir el cambio de "Vistas" si el servidor da error', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      interactionServiceMock.toggleMark.mockReturnValue(throwError(() => new Error('Server Error')));
      
      component.isWatched = false;
      component.toggleWatched(); 
      
      expect(component.isWatched).toBe(false);
    });
  });

  describe('HU9.1: Valorar película vista', () => {

    it('NO debería permitir valorar si la película no está vista', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatched = false;
      component.rateFilm('love');
      
      expect(component.currentRating).toBeNull();
      expect(interactionServiceMock.toggleRate).not.toHaveBeenCalled();
    });

    it('debería aplicar la valoración si la película está vista', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatched = true;
      component.rateFilm('like');
      
      expect(component.currentRating).toBe('like');
      expect(interactionServiceMock.toggleRate).toHaveBeenCalledWith(1, 'like');
    });

    it('debería eliminar la valoración si se pulsa la misma que ya estaba', () => {
      component.film = { ...mockFilm } as any;
      fixture.detectChanges();
      
      component.isWatched = true;
      component.currentRating = 'love';
      component.rateFilm('love');
      
      expect(component.currentRating).toBeNull();
      expect(interactionServiceMock.toggleRate).toHaveBeenCalledWith(1, 'love');
    });

    it('debería modificar la valoración si se pulsa un botón diferente', () => {
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
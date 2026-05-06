import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { FilmDetail } from './film-detail';

describe('FilmDetail Component', () => {
  let component: FilmDetail;
  let fixture: ComponentFixture<FilmDetail>;

  const mockFilm = {
    id: 1,
    title: 'Película de Prueba',
    overView: 'Descripción de prueba',
    posterPath: '/path.jpg',
    releaseDate: '2024-01-01',
    videos: ['video1', 'video2'],
    cast: [],
    genres: [],
    watchProviders: []
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FilmDetail],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(FilmDetail);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create the component successfully', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('Child Components Rendering', () => {
    it('should render the app-film-trailers child component when videos are provided', async () => {
      const videoKeys = ['video1', 'video2'];
      component.filmData = { ...mockFilm, videos: videoKeys } as any;
      
      fixture.detectChanges();
      await fixture.whenStable();

      const trailersComponent = fixture.nativeElement.querySelector('app-film-trailers');
      expect(trailersComponent).toBeTruthy();
    });
  });
});
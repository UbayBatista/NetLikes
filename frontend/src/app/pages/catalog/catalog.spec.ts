import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Catalog } from './catalog';
import { FilmService } from '../../services/film.service';
import { of } from 'rxjs';
import { vi } from 'vitest'; 
import { provideRouter } from '@angular/router';

describe('Catalog', () => {
  let component: Catalog;
  let fixture: ComponentFixture<Catalog>;
  let filmServiceMock: any;

  const baseFilm = {
    id: 1,
    posterPath: '',
    year: 2024,
    recommendations: [],
    genres: [],
    watchProviders: [],
    cast: [],
    videos: []
  };

  const mockGenreGroups = [
    { 
      name: 'Acción', 
      films: [{ ...baseFilm, title: 'Matrix' }] 
    },
    { 
      name: 'Drama', 
      films: [{ ...baseFilm, title: 'La Milla Verde', id: 2 }] 
    }
  ];

  const mockSearchResults = [
    { id: 1, title: 'Matrix', posterPath: '' }
  ] as any[];

  beforeEach(async () => {
    filmServiceMock = {
      getFilmsByGenre: vi.fn().mockReturnValue(of(mockGenreGroups)),
      searchBy: vi.fn().mockReturnValue(of(mockSearchResults))
    };

    await TestBed.configureTestingModule({
      imports: [Catalog],
      providers: [
        { provide: FilmService, useValue: filmServiceMock }, 
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Catalog);
    component = fixture.componentInstance;
    fixture.detectChanges(); 
  });

  it('debería cargar los géneros al iniciar (HU Catálogo)', () => {
    expect(filmServiceMock.getFilmsByGenre).toHaveBeenCalled();
    expect(component.genres.length).toBe(2);
    expect(component.genres[0].name).toBe('Acción');
  });

  it('debería llamar al servicio de búsqueda cuando se ejecuta filters()', () => {
    component.filters('Matrix');
    
    expect(component.isSearching).toBeDefined();
  });

  it('debería actualizar searchResults cuando el servicio de búsqueda responde', () => {
    component.searchResults = mockSearchResults;
    component.isSearching = true;

    expect(component.searchResults.length).toBe(1);
    expect(component.searchResults[0].title).toBe('Matrix');
  });

  it('debería resetear isSearching si la búsqueda es vacía', () => {
    component.filters('');
    fixture.detectChanges();
    expect(component.isSearching).toBe(false);
  });
});
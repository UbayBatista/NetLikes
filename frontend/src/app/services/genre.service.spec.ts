import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { GenreService } from './genre.service';
import { Genre } from '../models/genre.models';

describe('GenreService', () => {
  let service: GenreService;
  let httpMock: HttpTestingController;
  const apiUrl = ' https://vendor-calamity-sneer.ngrok-free.dev/genres';

  const mockGenres: Genre[] = [
    { id: 1, name: 'Acción' },
    { id: 2, name: 'Comedia' },
    { id: 3, name: 'Drama' }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        GenreService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(GenreService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllGenres', () => {
    it('debe obtener la lista de géneros mediante una petición GET', () => {
      service.getAllGenres().subscribe((genres) => {
        expect(genres.length).toBe(3);
        expect(genres).toEqual(mockGenres);
        expect(genres[0].name).toBe('Acción');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');

      req.flush(mockGenres);
    });

    it('debe manejar una lista vacía de géneros', () => {
      service.getAllGenres().subscribe((genres) => {
        expect(genres.length).toBe(0);
      });

      const req = httpMock.expectOne(apiUrl);
      req.flush([]);
    });
  });
});
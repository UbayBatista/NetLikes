import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { GenreService } from './genre.service';
import { Genre } from '../models/genre.models';
import { environment } from '../../environments/environment';

describe('GenreService', () => {
  let service: GenreService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/genres`;

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

  it('should be created successfully', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllGenres() method', () => {
    it('should fetch a list of genres via a GET request', () => {
      service.getAllGenres().subscribe((genres) => {
        expect(genres.length).toBe(3);
        expect(genres).toEqual(mockGenres);
        expect(genres[0].name).toBe('Acción');
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');

      req.flush(mockGenres);
    });

    it('should handle an empty list of genres correctly', () => {
      service.getAllGenres().subscribe((genres) => {
        expect(genres.length).toBe(0);
        expect(genres).toEqual([]);
      });

      const req = httpMock.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });
});
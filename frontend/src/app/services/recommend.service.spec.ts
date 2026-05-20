import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { RecommendationFollowedService } from './recommend.service';
import { AuthService } from './auth.service';
import { of } from 'rxjs';
import { vi } from 'vitest';

describe('RecommendationFollowedService', () => {
  let service: RecommendationFollowedService;
  let httpMock: HttpTestingController;
  let authServiceMock: any;

  beforeEach(() => {
    authServiceMock = {
      getCurrentUser: vi.fn().mockReturnValue(of({ email: 'test@correo.com' }))
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock }
      ]
    });

    service = TestBed.inject(RecommendationFollowedService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('US 10.1: Bulk Recommendations', () => {
    it('should send multiple recommendations via POST', () => {
      service.sendRecommendations(100, ['amigo@test.com']).subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/recommend/bulk');
      expect(req.request.method).toBe('POST');
      expect(req.request.headers.get('X-User-Id')).toBe('test@correo.com');
      expect(req.request.body).toEqual({ filmId: 100, targetEmails: ['amigo@test.com'] });
      req.flush('Recomendaciones enviadas');
    });

    it('should fetch recent recipients via GET', () => {
      service.getRecentRecipients().subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/recommend/recent');
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('X-User-Id')).toBe('test@correo.com');
      req.flush([{ email: 'amigo@test.com' }]);
    });

    it('should fetch already recommended emails for a film via GET', () => {
      service.getRecipientsForFilm(100).subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/recommend/film/100/recipients');
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('X-User-Id')).toBe('test@correo.com');
      req.flush(['amigo@test.com']);
    });
  });

  describe('US 10.1: Recommendation Stats', () => {
    it('should fetch recommended films with their count', () => {
      service.getRecommendedFilmsWithCount().subscribe();

      const req = httpMock.expectOne('http://localhost:8080/api/recommend/stats/test@correo.com');
      expect(req.request.method).toBe('GET');
      req.flush([{ film: { id: 1 }, count: 5 }]);
    });
  });
});
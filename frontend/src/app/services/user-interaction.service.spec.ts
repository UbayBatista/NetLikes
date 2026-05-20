import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { UserInteractionService } from './user-interaction.service';
import { AuthService } from './auth.service';
import { vi } from 'vitest';

describe('UserInteractionService', () => {
  let service: UserInteractionService;
  let httpMock: HttpTestingController;
  let authServiceMock: any;

  beforeEach(() => {
    authServiceMock = {
      getCurrentUserEmail: vi.fn().mockReturnValue('test@correo.com')
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock }
      ]
    });

    service = TestBed.inject(UserInteractionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created successfully', () => {
    expect(service).toBeTruthy();
  });

  describe('Marks Management', () => {
    it('should make a POST request when calling toggleMark', () => {
      service.toggleMark(123, 'SEEN').subscribe();

      const req = httpMock.expectOne('http://localhost:8080/marks/test@correo.com/toggle/123?type=SEEN');
      expect(req.request.method).toBe('POST');
      req.flush({ status: 'added' });
    });

    it('should throw an error when calling toggleMark without a valid email', () => {
      authServiceMock.getCurrentUserEmail.mockReturnValue(null);
      
      expect(() => {
        service.toggleMark(123, 'SEEN');
      }).toThrowError('No hay email');
    });

    it('should make a GET request when calling getMarkStatus', () => {
      service.getMarkStatus(123).subscribe();

      const req = httpMock.expectOne('http://localhost:8080/marks/test@correo.com/status/123');
      expect(req.request.method).toBe('GET');
      req.flush({ type: 'SEEN' });
    });

    it('should make a POST request when calling toggleMark with RECOMMENDED type', () => {
      service.toggleMark(123, 'RECOMMENDED').subscribe();

      const req = httpMock.expectOne('http://localhost:8080/marks/test@correo.com/toggle/123?type=RECOMMENDED');
      expect(req.request.method).toBe('POST');
      req.flush({ status: 'added' });
    });
  });

  describe('Rates Management', () => {
    it('should make a POST request sending the score in UPPERCASE when calling toggleRate', () => {
      service.toggleRate(123, 'love').subscribe();

      const req = httpMock.expectOne('http://localhost:8080/rates/test@correo.com/123?score=LOVE');
      expect(req.request.method).toBe('POST');
      req.flush({ score: 'LOVE' });
    });

    it('should return an observable with null when calling getRateStatus without a valid email', () => {
      authServiceMock.getCurrentUserEmail.mockReturnValue(null);
      
      service.getRateStatus(123).subscribe(result => {
        expect(result).toBeNull();
      });

      httpMock.expectNone('http://localhost:8080/rates/null/123');
    });
  });
});
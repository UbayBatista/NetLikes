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

  it('debería crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  describe('Gestión de Marcas (Marks)', () => {
    it('debería hacer un POST correcto al hacer toggleMark', () => {
      service.toggleMark(123, 'SEEN').subscribe();

      const req = httpMock.expectOne('http://localhost:8080/marks/test@correo.com/toggle/123?type=SEEN');
      expect(req.request.method).toBe('POST');
      req.flush({ status: 'added' });
    });

    it('debería lanzar un error si se hace toggleMark sin un email válido', () => {
      authServiceMock.getCurrentUserEmail.mockReturnValue(null);
      
      expect(() => {
        service.toggleMark(123, 'SEEN');
      }).toThrowError('No hay email');
    });

    it('debería hacer un GET correcto al recuperar el estado (getMarkStatus)', () => {
      service.getMarkStatus(123).subscribe();

      const req = httpMock.expectOne('http://localhost:8080/marks/test@correo.com/status/123');
      expect(req.request.method).toBe('GET');
      req.flush({ type: 'SEEN' });
    });
  });

  describe('Gestión de Valoraciones (Rates)', () => {
    it('debería hacer un POST enviando el score en MAYÚSCULAS', () => {
      service.toggleRate(123, 'love').subscribe();

      const req = httpMock.expectOne('http://localhost:8080/rates/test@correo.com/123?score=LOVE');
      expect(req.request.method).toBe('POST');
      req.flush({ score: 'LOVE' });
    });

    it('debería devolver un observable con null si no hay email al hacer getRateStatus', () => {
      authServiceMock.getCurrentUserEmail.mockReturnValue(null);
      
      service.getRateStatus(123).subscribe(result => {
        expect(result).toBeNull();
      });

      httpMock.expectNone('http://localhost:8080/rates/null/123');
    });
  });
});
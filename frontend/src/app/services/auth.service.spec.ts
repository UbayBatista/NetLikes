import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { User, RegisterData } from '../models/user.models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/users';

  const mockUser: User = {
    userName: 'Juan Perez',
    email: 'test@test.com',
    profilePicture: 'assets/img.png'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe enviar una petición POST en register y guardar en local', () => {
    const registerData: RegisterData = {
      userName: 'Juan', email: 'j@test.com', birthdate: '2000-01-01',
      password: '123', securityQuestion: '?', answer: '!', favoriteGenres: []
    };

    service.register(registerData).subscribe(user => {
      expect(user).toEqual(mockUser);
      expect(localStorage.getItem('user')).toBeTruthy();
    });

    const req = httpMock.expectOne(`${apiUrl}/register`);
    expect(req.request.method).toBe('POST');
    req.flush(mockUser);
  });

  it('checkEmailExists debe llamar a la URL correcta (/exists/email)', () => {
    const email = 'test@test.com';
    service.checkEmailExists(email).subscribe(exists => {
      expect(exists).toBe(true);
    });

    const req = httpMock.expectOne(`${apiUrl}/exists/${email}`);
    expect(req.request.method).toBe('GET');
    req.flush(true);
  });

  it('getSecurityQuestion debe devolver texto plano', () => {
    const email = 'test@test.com';
    const question = '¿Perro o gato?';

    service.getSecurityQuestion(email).subscribe(q => {
      expect(q).toBe(question);
    });

    const req = httpMock.expectOne(`${apiUrl}/securityQuestion/${email}`);
    req.flush(question);
  });
});
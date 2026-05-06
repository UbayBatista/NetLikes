import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { User, RegisterData, Credentials } from '../models/user.models';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/users`;

  const mockUser: User = {
    userName: 'Juan Perez',
    email: 'test@test.com',
    profilePicture: 'assets/img.png'
  };

  beforeEach(() => {
    localStorage.clear();
    
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ]
    });
    
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    const reqs = httpMock.match(req => req.url.includes('/exists/'));
    reqs.forEach(req => req.flush(false));
    
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('Authentication Flow', () => {
    it('should send a POST request on register and save user to localStorage', () => {
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

    it('should send a POST request on login and save user to localStorage', () => {
      const credentials: Credentials = { email: 'test@test.com', password: '123' };

      service.login(credentials).subscribe(user => {
        expect(user).toEqual(mockUser);
        expect(localStorage.getItem('user')).toBe(JSON.stringify(mockUser));
      });

      const req = httpMock.expectOne(`${apiUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(credentials);
      req.flush(mockUser);
    });

    it('should remove user from localStorage and clear currentUser$ on logout', () => {
      localStorage.setItem('user', JSON.stringify(mockUser));
      service['currentUser$'].next(mockUser);

      service.logout();

      expect(localStorage.getItem('user')).toBeNull();
      service.getCurrentUser().subscribe(user => {
        expect(user).toBeNull();
      });
    });
  });

  describe('Validation & Password Recovery', () => {
    it('should call the correct URL via GET to check if email exists', () => {
      const email = 'test@test.com';
      service.checkEmailExists(email).subscribe(exists => {
        expect(exists).toBe(true);
      });

      const reqs = httpMock.match(`${apiUrl}/exists/${email}`);
      expect(reqs.length).toBeGreaterThan(0);
      expect(reqs[0].request.method).toBe('GET');
      reqs.forEach(req => req.flush(true));
    });

    it('should call the correct URL via GET to check if username exists', () => {
      const name = 'UsuarioTest';
      service.checkNameExists(name).subscribe(exists => {
        expect(exists).toBe(true);
      });

      const reqs = httpMock.match(`${apiUrl}/existsName/${name}`);
      expect(reqs.length).toBeGreaterThan(0);
      expect(reqs[0].request.method).toBe('GET');
      reqs.forEach(req => req.flush(true));
    });

    it('should return plain text when fetching security question', () => {
      const email = 'test@test.com';
      const question = '¿Perro o gato?';

      service.getSecurityQuestion(email).subscribe(q => {
        expect(q).toBe(question);
      });

      const req = httpMock.expectOne(`${apiUrl}/securityQuestion/${email}`);
      req.flush(question);
    });

    it('should send a POST request with email and answer to validate security answer', () => {
      service.isValidAnswer('test@test.com', 'Firulais').subscribe(result => {
        expect(result).toBe(true);
      });

      const req = httpMock.expectOne(`${apiUrl}/isValidAnswer`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ email: 'test@test.com', answer: 'Firulais' });
      req.flush(true);
    });

    it('should send a PATCH request with email and new password to change password', () => {
      service.changePassword('test@test.com', 'nuevaPass123').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/changePassword`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({ email: 'test@test.com', newPassword: 'nuevaPass123' });
      req.flush(null);
    });
  });

  describe('Session State Management', () => {
    it('should return true from isAuthenticated when a user is logged in', () => {
      service['currentUser$'].next(mockUser);
      expect(service.isAuthenticated()).toBe(true);
    });

    it('should return false from isAuthenticated when no user is logged in', () => {
      service['currentUser$'].next(null);
      expect(service.isAuthenticated()).toBe(false);
    });

    it('should emit the current user when getCurrentUser is called', () => {
      service['currentUser$'].next(mockUser);
      service.getCurrentUser().subscribe(user => {
        expect(user).toEqual(mockUser);
      });
    });

    it('should return the email of the logged-in user when getCurrentUserEmail is called', () => {
      service['currentUser$'].next(mockUser);
      expect(service.getCurrentUserEmail()).toBe('test@test.com');
    });

    it('should return null from getCurrentUserEmail if no user is logged in', () => {
      service['currentUser$'].next(null);
      expect(service.getCurrentUserEmail()).toBeNull();
    });
  });

  describe('Service Initialization', () => {
    it('should restore the user from localStorage if the email exists in DB', () => {
      TestBed.resetTestingModule();
      localStorage.setItem('user', JSON.stringify(mockUser));

      TestBed.configureTestingModule({
        providers: [
          AuthService,
          provideHttpClient(),
          provideHttpClientTesting()
        ]
      });

      service = TestBed.inject(AuthService);
      httpMock = TestBed.inject(HttpTestingController);

      const req = httpMock.expectOne(`${apiUrl}/exists/${mockUser.email}`);
      req.flush(true);

      service.getCurrentUser().subscribe(user => {
        expect(user).toEqual(mockUser);
      });

      httpMock.verify();
    });

    it('should clear localStorage and user state if the stored email does not exist in DB', () => {
      TestBed.resetTestingModule();
      localStorage.setItem('user', JSON.stringify(mockUser));

      TestBed.configureTestingModule({
        providers: [
          AuthService,
          provideHttpClient(),
          provideHttpClientTesting()
        ]
      });

      service = TestBed.inject(AuthService);
      httpMock = TestBed.inject(HttpTestingController);

      const req = httpMock.expectOne(`${apiUrl}/exists/${mockUser.email}`);
      req.flush(false);

      expect(localStorage.getItem('user')).toBeNull();
      service.getCurrentUser().subscribe(user => {
        expect(user).toBeNull();
      });

      httpMock.verify();
    });

    it('should emit false on isLoading after initializing without a user in localStorage', () => {
      service.isLoading().subscribe(loading => {
        expect(loading).toBe(false);
      });
    });
  });
});
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { User, RegisterData, Credentials} from '../models/user.models';
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

    const reqs = httpMock.match(`${apiUrl}/exists/${email}`);
    expect(reqs.length).toBeGreaterThan(0);
    expect(reqs[0].request.method).toBe('GET');
    reqs.forEach(req => req.flush(true));
  });

  it('checkNameExists debe llamar a la URL correcta (/existsName/name)', () => {
    const name = 'UsuarioTest';
    service.checkNameExists(name).subscribe(exists => {
      expect(exists).toBe(true);
    });

    const reqs = httpMock.match(`${apiUrl}/existsName/${name}`);
    expect(reqs.length).toBeGreaterThan(0);
    expect(reqs[0].request.method).toBe('GET');
    reqs.forEach(req => req.flush(true));
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

  it('login debe enviar POST y guardar usuario en localStorage', () => {
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

  it('logout debe eliminar el usuario de localStorage y limpiar currentUser$', () => {
    localStorage.setItem('user', JSON.stringify(mockUser));
    service['currentUser$'].next(mockUser);

    service.logout();

    expect(localStorage.getItem('user')).toBeNull();
    service.getCurrentUser().subscribe(user => {
      expect(user).toBeNull();
    });
  });

  it('isAuthenticated debe retornar true cuando hay usuario logueado', () => {
    service['currentUser$'].next(mockUser);

    expect(service.isAuthenticated()).toBe(true);
  });

  it('isAuthenticated debe retornar false cuando no hay usuario', () => {
    service['currentUser$'].next(null);

    expect(service.isAuthenticated()).toBe(false);
  });

  it('getCurrentUser debe emitir el usuario actual', () => {
    service['currentUser$'].next(mockUser);

    service.getCurrentUser().subscribe(user => {
      expect(user).toEqual(mockUser);
    });
  });

  it('getCurrentUserEmail debe retornar el email del usuario logueado', () => {
    service['currentUser$'].next(mockUser);

    expect(service.getCurrentUserEmail()).toBe('test@test.com');
  });

  it('getCurrentUserEmail debe retornar null si no hay usuario', () => {
    service['currentUser$'].next(null);

    expect(service.getCurrentUserEmail()).toBeNull();
  });

  it('isValidAnswer debe enviar POST con email y answer', () => {
    service.isValidAnswer('test@test.com', 'Firulais').subscribe(result => {
      expect(result).toBe(true);
    });

    const req = httpMock.expectOne(`${apiUrl}/isValidAnswer`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'test@test.com', answer: 'Firulais' });
    req.flush(true);
  });

  it('changePassword debe enviar PATCH con email y nueva contraseña', () => {
    service.changePassword('test@test.com', 'nuevaPass123').subscribe();

    const req = httpMock.expectOne(`${apiUrl}/changePassword`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ email: 'test@test.com', newPassword: 'nuevaPass123' });
    req.flush(null);
  });

  it('loadUserFromStorage debe restaurar el usuario si el email existe', () => {
    
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

  it('loadUserFromStorage debe limpiar localStorage si el email no existe', () => {
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

  it('isLoading debe emitir false tras inicializar sin usuario en localStorage', () => {
    service.isLoading().subscribe(loading => {
      expect(loading).toBe(false);
    });
  });
});
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { FollowService, Follow, LoggedUser } from './follow.service';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('FollowService', () => {
  let service: FollowService;
  let httpMock: HttpTestingController;
  
  const apiUrl = `${environment.apiUrl}/follows`;

  const mockLoggedUser: LoggedUser = {
    email: 'test@test.com',
    userName: 'UsuarioTest',
    profilePicture: 'assets/img.png'
  };

  const mockFollowResponse: Follow = {
    followerId: 'test@test.com',
    followedId: 'target@test.com',
    state: 'PENDING'
  };

  beforeEach(() => {
    const mockAuthService = {
      getCurrentUser: vi.fn().mockReturnValue(of(mockLoggedUser))
    };

    TestBed.configureTestingModule({
      providers: [
        FollowService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    service = TestBed.inject(FollowService);
    httpMock = TestBed.inject(HttpTestingController);

    localStorage.clear();
    localStorage.setItem('user', JSON.stringify(mockLoggedUser));
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created successfully', () => {
    expect(service).toBeTruthy();
  });

  describe('Headers and LocalStorage handling', () => {
    it('should add the X-User-Id header correctly by reading from localStorage', () => {
      service.requestFollow('target@test.com').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/target@test.com`);
      expect(req.request.headers.get('X-User-Id')).toBe('test@test.com');
      req.flush(mockFollowResponse);
    });

    it('should send an empty X-User-Id when no user is in localStorage', () => {
      localStorage.removeItem('user');

      service.requestFollow('target@test.com').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/target@test.com`);
      expect(req.request.headers.get('X-User-Id')).toBe('');
      req.flush(mockFollowResponse);
    });

    it('should handle localStorage parsing errors and send an empty X-User-Id', () => {
      localStorage.setItem('user', 'invalid-json-format');
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      service.requestFollow('target@test.com').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/target@test.com`);
      expect(req.request.headers.get('X-User-Id')).toBe('');
      expect(consoleSpy).toHaveBeenCalledWith('Error al parsear el usuario del localStorage', expect.any(Error));
      
      consoleSpy.mockRestore();
      req.flush(mockFollowResponse);
    });
  });

  describe('Follow Actions API Endpoints', () => {
    it('should make a POST request to the correct URL for requestFollow', () => {
      service.requestFollow('target@test.com').subscribe(res => {
        expect(res).toEqual(mockFollowResponse);
      });

      const req = httpMock.expectOne(`${apiUrl}/target@test.com`);
      expect(req.request.method).toBe('POST');
      req.flush(mockFollowResponse);
    });

    it('should make a POST request to the correct URL for acceptFollow', () => {
      service.acceptFollow('follower@test.com').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/follower@test.com/accept`);
      expect(req.request.method).toBe('POST');
      req.flush(mockFollowResponse);
    });

    it('should make a DELETE request to the correct URL for unfollow', () => {
      service.unfollow('target@test.com').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/target@test.com/unfollow`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should make a DELETE request to the correct URL for rejectFollow', () => {
      service.rejectFollow('follower@test.com').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/follower@test.com/reject`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should make a DELETE request to the correct URL for remove', () => {
      service.remove('target@test.com').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/target@test.com/remove`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  describe('Social Lists and Status API Endpoints', () => {
    it('should make a GET request to the correct URL for getFollowers', () => {
      const mockUsers: LoggedUser[] = [mockLoggedUser];
      service.getFollowers('target@test.com').subscribe(res => {
        expect(res).toEqual(mockUsers);
      });

      const req = httpMock.expectOne(`${apiUrl}/followersOf/target@test.com`);
      expect(req.request.method).toBe('GET');
      req.flush(mockUsers);
    });

    it('should make a GET request to the correct URL for getFollowing', () => {
      const mockUsers: LoggedUser[] = [mockLoggedUser];
      service.getFollowing('target@test.com').subscribe(res => {
        expect(res).toEqual(mockUsers);
      });

      const req = httpMock.expectOne(`${apiUrl}/followsOf/target@test.com`);
      expect(req.request.method).toBe('GET');
      req.flush(mockUsers);
    });

    it('should make a GET request to the correct URL for getPendingRequests', () => {
      const mockUsers: LoggedUser[] = [mockLoggedUser];
      service.getPendingRequests().subscribe(res => {
        expect(res).toEqual(mockUsers);
      });

      const req = httpMock.expectOne(`${apiUrl}/pending`);
      expect(req.request.method).toBe('GET');
      req.flush(mockUsers);
    });

    it('should use AuthService and make a GET request to the correct URL for checkFollowStatus', () => {
      const mockStatus = { state: 'ACCEPTED' as const };
      
      service.checkFollowStatus('target@test.com').subscribe(res => {
        expect(res.state).toBe('ACCEPTED');
      });

      const req = httpMock.expectOne(`${apiUrl}/target@test.com/status`);
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('X-User-Id')).toBe('test@test.com');
      req.flush(mockStatus);
    });
  });

  describe('Block User API Endpoints', () => {
    it('should make a POST request to the correct URL for blockUser', () => {
      service.blockUser('target@test.com').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/target@test.com/block`);
      expect(req.request.method).toBe('POST');
      req.flush(mockFollowResponse);
    });

    it('should make a POST request to the correct URL for unblockUser', () => {
      service.unblockUser('target@test.com').subscribe();

      const req = httpMock.expectOne(`${apiUrl}/target@test.com/unblock`);
      expect(req.request.method).toBe('POST');
      req.flush(mockFollowResponse);
    });

    it('should make a GET request to the correct URL for getBlockedUsers', () => {
      const mockUsers: LoggedUser[] = [mockLoggedUser];
      service.getBlockedUsers().subscribe(res => {
        expect(res).toEqual(mockUsers);
      });

      const req = httpMock.expectOne(`${apiUrl}/blocked`);
      expect(req.request.method).toBe('GET');
      req.flush(mockUsers);
    });
  });
});
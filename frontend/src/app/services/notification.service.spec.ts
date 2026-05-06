import { TestBed } from '@angular/core/testing';
import { NotificationService } from './notification.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { provideRouter } from '@angular/router';

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;
  let authServiceMock: any;

  beforeEach(() => {
    authServiceMock = {
      getCurrentUserEmail: vi.fn().mockReturnValue('test@test.com')
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        NotificationService,
        { provide: AuthService, useValue: authServiceMock },
        provideRouter([])
      ]
    });

    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created successfully', () => {
    expect(service).toBeTruthy();
  });

  describe('Notification Management', () => {
    it('should mark all notifications as read, set unread count to 0, and send a PUT request when markAllAsRead is called', () => {
      const mockNotifs: any[] = [{ read: false }, { read: false }];
      (service as any).notificationsSubject.next(mockNotifs);
      (service as any).unreadCountSubject.next(2);

      service.markAllAsRead();

      const req = httpMock.expectOne('https://api-db.duckdns.org/notifications/test@test.com/read-all');
      expect(req.request.method).toBe('PUT');
      
      req.flush({}); 

      let currentCount = 0;
      service.unreadCount$.subscribe(c => currentCount = c);
      expect(currentCount).toBe(0);

      let currentNotifs: any[] = [];
      service.notifications$.subscribe(n => currentNotifs = n);
      expect(currentNotifs.every(n => n.read === true)).toBe(true);
    });
  });
});
import { TestBed } from '@angular/core/testing';
import { NotificationService } from './notification.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';

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
        { provide: AuthService, useValue: authServiceMock }
      ]
    });

    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debería marcar todas como leídas y poner el contador a 0', () => {
    const mockNotifs: any[] = [{ read: false }, { read: false }];
    (service as any).notificationsSubject.next(mockNotifs);
    (service as any).unreadCountSubject.next(2);

    service.markAllAsRead();

    const req = httpMock.expectOne('http://localhost:8080/notifications/test@test.com/read-all');
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
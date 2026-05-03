import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { BehaviorSubject, of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { Header } from './header';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.models';

const mockUser: User = {
  userName: 'Juan',
  email: 'juan@test.com',
  profilePicture: 'assets/custom.jpg'
};

describe('Header Component', () => {
  let component: Header;
  let fixture: ComponentFixture<Header>;
  let router: Router;
  let authServiceMock: any;
  let notificationServiceMock: any;
  let unreadCountSubject: BehaviorSubject<number>;

  beforeEach(async () => {
    unreadCountSubject = new BehaviorSubject<number>(0);

    authServiceMock = {
      getCurrentUser: vi.fn().mockReturnValue(of(mockUser))
    };

    notificationServiceMock = {
      unreadCount$: unreadCountSubject.asObservable(),
      initNotifications: vi.fn(),
      disconnect: vi.fn(),
      notifications$: of([])
    };

    await TestBed.configureTestingModule({
      imports: [Header],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: ActivatedRoute, useValue: { params: of({}) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('debería inicializar las notificaciones al arrancar', () => {
    expect(notificationServiceMock.initNotifications).toHaveBeenCalled();
  });

  it('HU 8.2: NO debería mostrar la burbuja roja si unreadCount es 0', () => {
    unreadCountSubject.next(0);
    fixture.detectChanges();

    const badge = fixture.nativeElement.querySelector('.badge.bg-danger');
    expect(badge).toBeNull();
  });

  it('HU 8.2: Debería mostrar la burbuja roja con el número correcto cuando unreadCount > 0', () => {
    unreadCountSubject.next(3);
    fixture.detectChanges();

    const badge = fixture.nativeElement.querySelector('.badge.bg-danger');
    expect(badge).toBeTruthy();
    expect(badge?.textContent?.trim()).toBe('3');
  });

  it('should show user profile picture when user is logged in', () => {
    fixture.detectChanges();

    const img = fixture.nativeElement.querySelector('img');
    expect(img?.src).toContain('assets/custom.jpg');
  });

  it('should show default profile picture when user has no picture', () => {
    authServiceMock.getCurrentUser.mockReturnValue(
      of({ ...mockUser, profilePicture: null })
    );

    fixture = TestBed.createComponent(Header);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const img = fixture.nativeElement.querySelector('img');
    expect(img?.src).toContain('assets/ProfilePicture.jpg');
  });

  it('goToProfile() should navigate to /profile/:userName', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    component.userName = 'my-profile';

    component.goToProfile();

    expect(navigateSpy).toHaveBeenCalledWith(['/profile', 'my-profile']);
  });
});
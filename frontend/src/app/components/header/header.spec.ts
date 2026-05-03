import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Header } from './header';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { BehaviorSubject, of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('Header Component', () => {
  let component: Header;
  let fixture: ComponentFixture<Header>;
  let authServiceMock: any;
  let notificationServiceMock: any;
  
  let unreadCountSubject: BehaviorSubject<number>;

  beforeEach(async () => {
    unreadCountSubject = new BehaviorSubject<number>(0);

    authServiceMock = {
      getCurrentUser: vi.fn().mockReturnValue(of({ email: 'mi@email.com', profilePicture: '' }))
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
        { provide: AuthService, useValue: authServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock },
        { provide: ActivatedRoute, useValue: { params: of({}) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });


  it('debería inicializar las notificaciones al arrancar', () => {
    expect(notificationServiceMock.initNotifications).toHaveBeenCalled();
  });

  it('HU 8.2: NO debería mostrar la burbuja roja si unreadCount es 0', () => {
    unreadCountSubject.next(0);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const badge = compiled.querySelector('.badge.bg-danger');
    
    expect(badge).toBeNull();
  });

  it('HU 8.2: Debería mostrar la burbuja roja con el número correcto cuando unreadCount > 0', () => {
    unreadCountSubject.next(3);
    fixture.detectChanges(); 

    const compiled = fixture.nativeElement as HTMLElement;
    const badge = compiled.querySelector('.badge.bg-danger');
    
    expect(badge).toBeTruthy();
    expect(badge?.textContent?.trim()).toBe('3');
  });
});
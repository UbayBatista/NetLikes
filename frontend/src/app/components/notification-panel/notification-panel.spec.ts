import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NotificationPanel } from './notification-panel';
import { FollowService } from '../../services/follow.service';
import { NotificationService } from '../../services/notification.service';
import { of } from 'rxjs';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('NotificationPanel Component', () => {
  let component: NotificationPanel;
  let fixture: ComponentFixture<NotificationPanel>;
  let followServiceMock: any;
  let notificationServiceMock: any;

  const mockRequests = [
    { email: 'paco@gmail.com', userName: 'Paco', profilePicture: '' },
    { email: 'elena@gmail.com', userName: 'Elena', profilePicture: '' }
  ];

  beforeEach(async () => {
    followServiceMock = {
      getPendingRequests: vi.fn().mockReturnValue(of(mockRequests)),
      acceptFollow: vi.fn().mockReturnValue(of({})),
      rejectFollow: vi.fn().mockReturnValue(of({}))
    };

    notificationServiceMock = {
      notifications$: of([]),
      markAllAsRead: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [NotificationPanel],
      providers: [
        { provide: FollowService, useValue: followServiceMock },
        { provide: NotificationService, useValue: notificationServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationPanel);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });


  it('debería cargar las solicitudes pendientes al cambiar a la vista de solicitudes', () => {
    component.showRequests();
    expect(followServiceMock.getPendingRequests).toHaveBeenCalled();
    expect(component.pendingRequests.length).toBe(2);
  });

  it('HU 7.4: Al pulsar "Aceptar", debería llamar a acceptFollow y quitar al usuario de la lista', () => {
    component.pendingRequests = [...mockRequests];
    
    component.accept('paco@gmail.com');

    expect(followServiceMock.acceptFollow).toHaveBeenCalledWith('paco@gmail.com');
    
    expect(component.pendingRequests.length).toBe(1);
    expect(component.pendingRequests[0].email).toBe('elena@gmail.com');
  });

  it('HU 7.4: Al pulsar "Rechazar", debería llamar a rejectFollow y quitar al usuario de la lista', () => {
    component.pendingRequests = [...mockRequests];
    
    component.reject('paco@gmail.com');

    expect(followServiceMock.rejectFollow).toHaveBeenCalledWith('paco@gmail.com');
    expect(component.pendingRequests.length).toBe(1);
    expect(component.pendingRequests[0].email).toBe('elena@gmail.com');
  });
});
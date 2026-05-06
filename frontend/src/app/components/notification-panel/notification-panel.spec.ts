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

  describe('View Switching Logic', () => {
    it('should load pending requests when switching to the requests view', () => {
      component.showRequests();
      
      expect(followServiceMock.getPendingRequests).toHaveBeenCalled();
      expect(component.pendingRequests.length).toBe(2);
    });
  });

  describe('Follow Requests Logic (US 7.4)', () => {
    it('should call acceptFollow and remove the user from the list when "Accept" is clicked', () => {
      component.pendingRequests = [...mockRequests];
      
      component.accept('paco@gmail.com');

      expect(followServiceMock.acceptFollow).toHaveBeenCalledWith('paco@gmail.com');
      expect(component.pendingRequests.length).toBe(1);
      expect(component.pendingRequests[0].email).toBe('elena@gmail.com');
    });

    it('should call rejectFollow and remove the user from the list when "Reject" is clicked', () => {
      component.pendingRequests = [...mockRequests];
      
      component.reject('paco@gmail.com');

      expect(followServiceMock.rejectFollow).toHaveBeenCalledWith('paco@gmail.com');
      expect(component.pendingRequests.length).toBe(1);
      expect(component.pendingRequests[0].email).toBe('elena@gmail.com');
    });
  });
});
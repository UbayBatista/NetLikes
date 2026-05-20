import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RecommendPanel } from './recommend-panel';
import { UserInteractionService } from '../../services/user-interaction.service';
import { AuthService } from '../../services/auth.service';
import { FollowService } from '../../services/follow.service';
import { RecommendationFollowedService } from '../../services/recommend.service';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('RecommendPanel Component', () => {
  let component: RecommendPanel;
  let fixture: ComponentFixture<RecommendPanel>;

  let interactionServiceMock: any;
  let authServiceMock: any;
  let followServiceMock: any;
  let recFollowedServiceMock: any;

  const mockUsers = [
    { userName: 'Paco', email: 'paco@test.com', profilePicture: '/paco.jpg' },
    { userName: 'Elena', email: 'elena@test.com', profilePicture: '' }
  ];

  beforeEach(async () => {
    interactionServiceMock = {
      toggleMark: vi.fn().mockReturnValue(of({ status: 'success' }))
    };

    authServiceMock = {
      getCurrentUser: vi.fn().mockReturnValue(of({ email: 'currentUser@test.com' }))
    };

    followServiceMock = {
      getFollowing: vi.fn().mockReturnValue(of(mockUsers))
    };

    recFollowedServiceMock = {
      getRecipientsForFilm: vi.fn().mockReturnValue(of(['luis@test.com'])),
      getRecentRecipients: vi.fn().mockReturnValue(of([{ email: 'elena@test.com' }])),
      sendRecommendations: vi.fn().mockReturnValue(of({}))
    };

    await TestBed.configureTestingModule({
      imports: [RecommendPanel],
      providers: [
        { provide: UserInteractionService, useValue: interactionServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: FollowService, useValue: followServiceMock },
        { provide: RecommendationFollowedService, useValue: recFollowedServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RecommendPanel);
    component = fixture.componentInstance;
    component.filmId = 123;
  });

  describe('Initialization (US 10.2 & 10.1)', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should load initial state and fetch following users when opened', () => {
      component.initialRecommended = true;
      component.isOpen = true;
      
      component.ngOnChanges({
        isOpen: {
          currentValue: true,
          previousValue: false,
          firstChange: true,
          isFirstChange: () => true
        }
      } as any);

      expect(component.addToProfile).toBe(true);
      expect(followServiceMock.getFollowing).toHaveBeenCalledWith('currentUser@test.com');
      
      expect(recFollowedServiceMock.getRecipientsForFilm).toHaveBeenCalledWith(123);
      expect(recFollowedServiceMock.getRecentRecipients).toHaveBeenCalled();
    });
  });

  describe('User Interaction', () => {
    it('should emit closed event when close() is called', () => {
      const emitSpy = vi.spyOn(component.closed, 'emit');
      component.close();
      expect(emitSpy).toHaveBeenCalled();
    });

    it('should toggle addToProfile when toggleMyRecommendation is called', () => {
      component.addToProfile = false;
      component.toggleMyRecommendation();
      expect(component.addToProfile).toBe(true);
      
      component.toggleMyRecommendation();
      expect(component.addToProfile).toBe(false);
    });

    it('should add/remove user from selectedUsers when toggleUser is called', () => {
      expect(component.selectedUsers).not.toContain('Paco');
      
      component.toggleUser('Paco');
      expect(component.selectedUsers).toContain('Paco');

      component.toggleUser('Paco');
      expect(component.selectedUsers).not.toContain('Paco');
    });
  });

  describe('Search Logic', () => {
    beforeEach(() => {
      component.followingUsers = [
        { name: 'Paco', email: 'paco@test.com' },
        { name: 'Elena', email: 'elena@test.com' }
      ];
      component.topRecentEmails = [];
      component.selectedUsers = [];
      component.displayUsers = [...component.followingUsers];
    });

    it('should filter followers based on query', () => {
      component.handleSearch('elena');
      expect(component.displayUsers.length).toBe(1);
      expect(component.displayUsers[0].name).toBe('Elena');
    });

    it('should reset filtered followers when query is empty', () => {
      component.handleSearch('');
      expect(component.displayUsers.length).toBe(2);
    });

    it('should be case insensitive', () => {
      component.handleSearch('PACO');
      expect(component.displayUsers.length).toBe(1);
      expect(component.displayUsers[0].name).toBe('Paco');
    });
  });

  describe('Submit Logic (US 10.1 & 10.2)', () => {
    it('should call toggleMark and emit status if addToProfile changed', () => {
      component.initialRecommended = false;
      component.addToProfile = true;
      const emitSpy = vi.spyOn(component.recommendedStatusChanged, 'emit');

      component.submitRecommendation();

      expect(interactionServiceMock.toggleMark).toHaveBeenCalledWith(123, 'RECOMMENDED');
      expect(emitSpy).toHaveBeenCalledWith(true);
    });

    it('should open confirmation modal if users are selected', () => {
      component.selectedUsers = ['paco@test.com'];
      component.submitRecommendation();

      expect(component.showConfirmModal).toBe(true);
      expect(component.confirmMessage).toContain('1 seguidor');
      expect(recFollowedServiceMock.sendRecommendations).not.toHaveBeenCalled(); 
    });

    it('should send bulk recommendation when modal is confirmed', () => {
      component.selectedUsers = ['paco@test.com', 'elena@test.com'];
      
      component.handleConfirmation(true);

      expect(component.showConfirmModal).toBe(false);
      expect(recFollowedServiceMock.sendRecommendations).toHaveBeenCalledWith(123, ['paco@test.com', 'elena@test.com']);
    });

    it('should NOT send bulk recommendation when modal is cancelled', () => {
      component.selectedUsers = ['paco@test.com'];
      
      component.handleConfirmation(false);

      expect(component.showConfirmModal).toBe(false);
      expect(recFollowedServiceMock.sendRecommendations).not.toHaveBeenCalled();
    });

    it('should close panel after submitting empty', () => {
      const emitSpy = vi.spyOn(component.closed, 'emit');
      component.selectedUsers = [];
      
      component.submitRecommendation();
      expect(emitSpy).toHaveBeenCalled();
    });
  });
});
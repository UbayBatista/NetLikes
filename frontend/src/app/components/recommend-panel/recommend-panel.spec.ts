import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RecommendPanel } from './recommend-panel';
import { UserInteractionService } from '../../services/user-interaction.service';
import { AuthService } from '../../services/auth.service';
import { FollowService } from '../../services/follow.service';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('RecommendPanel Component', () => {
  let component: RecommendPanel;
  let fixture: ComponentFixture<RecommendPanel>;

  let interactionServiceMock: any;
  let authServiceMock: any;
  let followServiceMock: any;

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

    await TestBed.configureTestingModule({
      imports: [RecommendPanel],
      providers: [
        { provide: UserInteractionService, useValue: interactionServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: FollowService, useValue: followServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RecommendPanel);
    component = fixture.componentInstance;
    component.filmId = 123;
  });

  describe('Initialization (US 10.2)', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should load initial state and fetch following users when opened', () => {
      component.initialRecommended = true;
      
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
      expect(component.followingUsers.length).toBe(2);
      expect(component.filteredFollowers.length).toBe(2);
      
      expect(component.followingUsers[0].name).toBe('Paco');
      expect(component.followingUsers[1].pic).toBe('assets/ProfilePicture.jpg');
    });
  });

  describe('User Interaction (US 10.2)', () => {
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
      component.filteredFollowers = [...component.followingUsers];
    });

    it('should filter followers based on query', () => {
      component.handleSearch('elena');
      expect(component.filteredFollowers.length).toBe(1);
      expect(component.filteredFollowers[0].name).toBe('Elena');
    });

    it('should reset filtered followers when query is empty', () => {
      component.handleSearch('');
      expect(component.filteredFollowers.length).toBe(2);
    });

    it('should be case insensitive', () => {
      component.handleSearch('PACO');
      expect(component.filteredFollowers.length).toBe(1);
      expect(component.filteredFollowers[0].name).toBe('Paco');
    });
  });

  describe('Submit Logic (US 10.2)', () => {
    it('should call toggleMark and emit status if addToProfile changed', () => {
      component.initialRecommended = false;
      component.addToProfile = true;
      const emitSpy = vi.spyOn(component.recommendedStatusChanged, 'emit');

      component.submitRecommendation();

      expect(interactionServiceMock.toggleMark).toHaveBeenCalledWith(123, 'RECOMMENDED');
      expect(emitSpy).toHaveBeenCalledWith(true);
    });

    it('should NOT call toggleMark if addToProfile did NOT change', () => {
      component.initialRecommended = true;
      component.addToProfile = true;

      component.submitRecommendation();

      expect(interactionServiceMock.toggleMark).not.toHaveBeenCalled();
    });

    it('should revert addToProfile state if toggleMark throws an error', () => {
      component.initialRecommended = false;
      component.addToProfile = true;
      interactionServiceMock.toggleMark.mockReturnValue(throwError(() => new Error('Server error')));

      component.submitRecommendation();

      expect(component.addToProfile).toBe(false);
    });

    it('should close panel after submitting', () => {
      const emitSpy = vi.spyOn(component.closed, 'emit');
      component.submitRecommendation();
      expect(emitSpy).toHaveBeenCalled();
    });
  });
});
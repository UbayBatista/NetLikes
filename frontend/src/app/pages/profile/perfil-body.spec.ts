import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComplete } from './profile-body';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { ProfileService } from '../../services/profile.service';
import { FollowService } from '../../services/follow';
import { AuthService } from '../../services/auth.service';

describe('ProfileBody', () => {
  let component: ProfileComplete;
  let fixture: ComponentFixture<ProfileComplete>;

  beforeEach(async () => {
    const mockProfileService = {
      getProfile: vi.fn().mockReturnValue(of({ userName: 'TestUser', email: 'test@test.com', followers: 10, following: 5 })),
      isMyProfile: vi.fn().mockReturnValue(of(true)),
      loadProfile: vi.fn(),
      updatePrivacy: vi.fn()
    };

    const mockFollowService = {
      checkFollowStatus: vi.fn().mockReturnValue(of({ state: 'NONE' })),
      requestFollow: vi.fn().mockReturnValue(of({ state: 'PENDING' })),
      unfollow: vi.fn().mockReturnValue(of({})),
      getFollowers: vi.fn().mockReturnValue(of([])),
      getFollowing: vi.fn().mockReturnValue(of([]))
    };

    const mockAuthService = {
      logout: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [ProfileComplete],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ username: 'TestUser' }), 
            snapshot: { paramMap: { get: () => 'TestUser' } }
          }
        },
        { provide: ProfileService, useValue: mockProfileService },
        { provide: FollowService, useValue: mockFollowService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComplete);
    component = fixture.componentInstance;
    
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('follow request logic', () => {
    let followService: any;

    beforeEach(() => {
      followService = TestBed.inject(FollowService);
    });

    it('should request follow and change state to PENDING', () => {

      expect(component['followStateSubject'].value).toBe('NONE');
      const startFollowers = component.followersCount$.value;

      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(followService.requestFollow).toHaveBeenCalledWith('target@test.com');
      expect(component['followStateSubject'].value).toBe('PENDING');
      expect(component.followersCount$.value).toBe(startFollowers);
    });

    it('should request follow, change state to ACCEPTED and add a follower if it is accepted', () => {
      followService.requestFollow = vi.fn().mockReturnValue(of({ state: 'ACCEPTED' }));
      const startFollowers = component.followersCount$.value;

      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(followService.requestFollow).toHaveBeenCalledWith('target@test.com');
      expect(component['followStateSubject'].value).toBe('ACCEPTED');
      expect(component.followersCount$.value).toBe(startFollowers + 1);
    });

    it('should unfollow and return to NONE if the state were PENDING', () => {
      component['followStateSubject'].next('PENDING');

      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(followService.unfollow).toHaveBeenCalledWith('target@test.com');
      expect(component['followStateSubject'].value).toBe('NONE');
    });

    it('should open confirmation modal if state were ACCEPTED', () => {

      component['followStateSubject'].next('ACCEPTED');
      
      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(component.showConfirmModal).toBe(true);
      expect(component['actionToConfirm']).toBe('UNFOLLOW');
      expect(component.confirmModalMessage).toContain('dejar de seguir a @TargetUser');
      expect(followService.unfollow).not.toHaveBeenCalled(); 
    });
  });

  
});
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
});
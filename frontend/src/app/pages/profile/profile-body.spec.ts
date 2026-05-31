import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComplete } from './profile-body';
import { provideRouter, ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ProfileService } from '../../services/profile.service';
import { FollowService } from '../../services/follow.service';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { MyProfile } from '../../models/user.models';

describe('ProfileComplete Component', () => {
  let component: ProfileComplete;
  let fixture: ComponentFixture<ProfileComplete>;
  let followService: any;
  let profileService: any;
  let authService: any;
  let userService: any;
  let router: Router;

  beforeEach(async () => {
    const mockProfileService = {
      getProfile: vi.fn().mockReturnValue(of({ userName: 'TestUser', email: 'test@test.com', followers: 10, following: 5 })),
      isMyProfile: vi.fn().mockReturnValue(of(true)),
      loadProfile: vi.fn(),
      updateBio: vi.fn().mockReturnValue(of({})),
      updateAvatar: vi.fn().mockReturnValue(of({})),
      updateListVisibility: vi.fn().mockReturnValue(of({})),
      updatePrivacy: vi.fn()
    };

    const mockFollowService = {
      checkFollowStatus: vi.fn().mockReturnValue(of({ state: 'NONE' })),
      requestFollow: vi.fn().mockReturnValue(of({ state: 'PENDING' })),
      unfollow: vi.fn().mockReturnValue(of({})),
      getFollowers: vi.fn().mockReturnValue(of([])),
      getFollowing: vi.fn().mockReturnValue(of([])),
      remove: vi.fn().mockReturnValue(of({})),
      blockUser: vi.fn().mockReturnValue(of({}))
    };

    const mockAuthService = {
      logout: vi.fn(),
      updateStoredUser: vi.fn()
    };

    const mockUserService = {
      deleteUser: vi.fn().mockReturnValue(of({}))
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
        { provide: AuthService, useValue: mockAuthService },
        { provide: UserService, useValue: mockUserService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComplete);
    component = fixture.componentInstance;
    
    followService = TestBed.inject(FollowService);
    profileService = TestBed.inject(ProfileService);
    authService = TestBed.inject(AuthService);
    userService = TestBed.inject(UserService);
    router = TestBed.inject(Router);

    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Follow Request Logic', () => {
    it('should request follow and change state to PENDING when user is not followed', () => {
      expect(component['followStateSubject'].value).toBe('NONE');
      const startFollowers = component.followersCount$.value;

      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(followService.requestFollow).toHaveBeenCalledWith('target@test.com');
      expect(component['followStateSubject'].value).toBe('PENDING');
      expect(component.followersCount$.value).toBe(startFollowers);
    });

    it('should change state to ACCEPTED and increase followers count when following a public user', () => {
      followService.requestFollow = vi.fn().mockReturnValue(of({ state: 'ACCEPTED' }));
      const startFollowers = component.followersCount$.value;

      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(followService.requestFollow).toHaveBeenCalledWith('target@test.com');
      expect(component['followStateSubject'].value).toBe('ACCEPTED');
      expect(component.followersCount$.value).toBe(startFollowers + 1);
    });
  });

  describe('Unfollow Logic', () => {
    beforeEach(() => {
      component.itsMe$ = of(false);
    });

    it('should display confirmation modal when trying to unfollow an accepted user', () => {
      component['followStateSubject'].next('ACCEPTED');
      
      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(component.showConfirmModal).toBe(true);
      expect(component['actionToConfirm']).toBe('UNFOLLOW');
      expect(component.confirmModalMessage).toContain('dejar de seguir a @TargetUser');
      expect(followService.unfollow).not.toHaveBeenCalled();
    });

    it('should unfollow user and update counters when unfollow is confirmed', () => {
      component['followStateSubject'].next('ACCEPTED');
      component.onFollowRequest('TargetUser', 'target@test.com'); 
      const startFollowers = component.followersCount$.value;

      component.handleConfirmation(true);

      expect(component.showConfirmModal).toBe(false); 
      expect(followService.unfollow).toHaveBeenCalledWith('target@test.com'); 
      expect(component['followStateSubject'].value).toBe('NONE');
      expect(component.followersCount$.value).toBe(Math.max(0, startFollowers - 1)); 
    });
  });

  describe('Remove Follower Logic', () => {
    beforeEach(() => {
      component.itsMe$ = of(true);
      component.socialData = [
        { name: 'UsuarioMolesto', email: 'molesto@test.com', avatar: '' },
        { name: 'BuenAmigo', email: 'amigo@test.com', avatar: '' }
      ];
      component.followersCount$.next(2);
    });

    it('should display confirmation modal when removing a follower', () => {
      component.handleSocialAction({ user: { name: 'UsuarioMolesto', email: 'molesto@test.com' }, type: 'Seguidores' });

      expect(component.showConfirmModal).toBe(true);
      expect(component['actionToConfirm']).toBe('REMOVE_FOLLOWER');
      expect(followService.remove).not.toHaveBeenCalled();
    });

    it('should remove follower and update list when removal is confirmed', () => {
      component.handleSocialAction({ user: { name: 'UsuarioMolesto', email: 'molesto@test.com' }, type: 'Seguidores' });

      component.handleConfirmation(true);

      expect(component.showConfirmModal).toBe(false); 
      expect(followService.remove).toHaveBeenCalledWith('molesto@test.com'); 
      expect(component.socialData.length).toBe(1);
      expect(component.followersCount$.value).toBe(1);
    });
  });

  describe('Cancel Follow Request Logic', () => {
    it('should cancel request and change button text to Seguir when state is PENDING', () => {
      component['followStateSubject'].next('PENDING');
      let currentButtonText = '';
      component.followButtonText$.subscribe(text => currentButtonText = text);
      expect(currentButtonText).toBe('Pendiente');

      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(followService.unfollow).toHaveBeenCalledWith('target@test.com');
      expect(component['followStateSubject'].value).toBe('NONE');
      expect(currentButtonText).toBe('Seguir');
    });
  });

  describe('Logout Logic', () => {
    it('should clear session and navigate to home when logout is triggered', () => {
      const navigateSpy = vi.spyOn(router, 'navigate');

      component.logout();

      expect(authService.logout).toHaveBeenCalled();
      expect(navigateSpy).toHaveBeenCalledWith(['/']);
    });
  });

  describe('Block User Logic', () => {
    it('should display confirmation modal when block is requested', () => {
      component.onBlockRequest('molesto@test.com', 'UsuarioMolesto');

      expect(component.showConfirmModal).toBe(true);
      expect(component['actionToConfirm']).toBe('BLOCK');
      expect(component['actionUser']).toBe('molesto@test.com');
      expect(followService.blockUser).not.toHaveBeenCalled();
    });

    it('should block user and change state to BLOCKED when block is confirmed', () => {
      component.onBlockRequest('molesto@test.com', 'UsuarioMolesto');
      
      component.handleConfirmation(true);

      expect(component.showConfirmModal).toBe(false);
      expect(followService.blockUser).toHaveBeenCalledWith('molesto@test.com');
      expect(component['followStateSubject'].value).toBe('BLOCKED');
    });

    it('should close modal and not block user when block is cancelled', () => {
      component.onBlockRequest('molesto@test.com', 'UsuarioMolesto');
      const prevState = component['followStateSubject'].value;

      component.handleConfirmation(false);

      expect(component.showConfirmModal).toBe(false);
      expect(followService.blockUser).not.toHaveBeenCalled();
      expect(component['followStateSubject'].value).toBe(prevState);
    });

    it('should hide profile and display not found message when user is BLOCKED', () => {
      component['followStateSubject'].next('BLOCKED');
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.textContent).toContain('Usuario no encontrado');
      expect(compiled.querySelector('app-profile-header')).toBeNull();
    });
  });

  describe('Access Other User Profile', () => {
    it('should call loadProfile with the username from the route', () => {
      expect(profileService.loadProfile).toHaveBeenCalledWith('TestUser');
    });

    it('should check follow status of the visited profile', () => {
      expect(followService.checkFollowStatus).toHaveBeenCalledWith('test@test.com');
    });

    it('should set isMyProfile to false when visiting another user', () => {
      expect(profileService.isMyProfile).toHaveBeenCalled();
    });
  });

  describe('Account Privacy Toggle', () => {
    it('should call updatePrivacy with true when making a public account private', () => {
      component.onPrivacyChange(true);
      expect(profileService.updatePrivacy).toHaveBeenCalledWith(true);
    });

    it('should call updatePrivacy with false when making a private account public', () => {
      component.onPrivacyChange(false);
      expect(profileService.updatePrivacy).toHaveBeenCalledWith(false);
    });
  });

  describe('Account Deletion Logic', () => {
    const userEmail = 'test@test.com';

    it('should open password verification modal when delete process starts', () => {
      component.startDeleteProcess(userEmail);

      expect(component.isPasswordModalOpen).toBe(true);
      expect(component['actionUser']).toBe(userEmail);
    });

    it('should transition to confirmation modal when password is successfully verified', () => {
      component['actionUser'] = userEmail;
      
      component.onPasswordVerified();

      expect(component.isPasswordModalOpen).toBe(false);
      expect(component['actionToConfirm']).toBe('DELETE');
      expect(component.showConfirmModal).toBe(true);
      expect(component.confirmModalMessage).toContain('borrar permanentemente tu cuenta');
    });

    it('should call userService.deleteUser, logout and navigate to home when deletion is confirmed', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    userService.deleteUser.mockReturnValue(of({})); 

    component['actionToConfirm'] = 'DELETE';
    component['actionUser'] = 'test@test.com';

    component.handleConfirmation(true);

    expect(userService.deleteUser).toHaveBeenCalled();
    expect(authService.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
    });

    it('should reset actionUser and close modal if deletion is cancelled', () => {
      component['actionToConfirm'] = 'DELETE';
      component['actionUser'] = userEmail;

      component.handleConfirmation(false);

      expect(component.showConfirmModal).toBe(false);
      expect(userService.deleteUser).not.toHaveBeenCalled();
      expect(component['actionUser']).toBe('');
    });
  });

  describe('Change Password Flow', () => {
    const userEmail = 'test@test.com';

    it('should start change password process by setting mode to CHANGE and opening password modal', () => {
      authService.getCurrentUser = vi.fn().mockReturnValue(of({ email: userEmail }));
      
      component.startChangePasswordProcess();

      expect(component['actionUser']).toBe(userEmail);
      expect(component.passwordModalMode).toBe('CHANGE');
      expect(component.isPasswordModalOpen).toBe(true);
    });

    it('should transition to recover modal bypassing security question when password is verified in CHANGE mode', () => {
      component.passwordModalMode = 'CHANGE';
      component.isPasswordModalOpen = true;

      component.onPasswordVerified();

      expect(component.isPasswordModalOpen).toBe(false);
      expect(component.skipSecurityQuestion).toBe(true);
      expect(component.isRecoverModalOpen).toBe(true);
      expect(component.showConfirmModal).toBe(false);
    });

    it('should open recover modal WITH security question when forgot password is clicked', () => {
      component.isPasswordModalOpen = true;

      component.onForgotPasswordClicked();

      expect(component.isPasswordModalOpen).toBe(false);
      expect(component.skipSecurityQuestion).toBe(false);
      expect(component.isRecoverModalOpen).toBe(true);
    });
  });

  describe('Profile Sections Rendering', () => {
    it('should display the "Películas Recomendadas" section if the profile has recommended films', () => {
      profileService.getProfile.mockReturnValue(of({
        userName: 'TestUser',
        email: 'test@test.com',
        followers: 10,
        following: 5,
        watchedFilms: [],
        laterFilms: [],
        recommendedFilms: [{ id: 1, title: 'Inception', posterPath: '/inception.jpg' }],
        showWatchedFilms: true,
        showFilmsToWatchLater: true,
        showRecommendedFilms: true
      }));
      profileService.isMyProfile.mockReturnValue(of(true));

      fixture = TestBed.createComponent(ProfileComplete);
      component = fixture.componentInstance;
      
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      
      expect(compiled.textContent).toContain('Películas Recomendadas');
    });
  });

  describe('Bio Edit Logic', () => {
    it('should show save modal when toggleEdit is called with pending changes', () => {
      component.isEditing = true;
      component.thereIsChanges = true;

      component.toggleEdit();

      expect(component.showSaveModal).toBe(true);
      expect(component.isEditing).toBe(true);
    });

    it('should toggle isEditing when there are no pending changes', () => {
      component.isEditing = false;
      component.thereIsChanges = false;

      component.toggleEdit();

      expect(component.isEditing).toBe(true);
      expect(component.showSaveModal).toBe(false);
    });

    it('should update pendingBio and thereIsChanges when onBioSave is called with changes', () => {
      component.onBioSave({ bio: 'Nueva bio', hasChanges: true });

      expect(component.pendingBio).toBe('Nueva bio');
      expect(component.thereIsChanges).toBe(true);
    });

    it('should update pendingBio and set thereIsChanges false when onBioSave has no changes', () => {
      component.onBioSave({ bio: 'Bio original', hasChanges: false });

      expect(component.pendingBio).toBe('Bio original');
      expect(component.thereIsChanges).toBe(false);
    });

    it('should not call updateBio and should reset state when save is cancelled', () => {
      component.pendingBio = 'Nueva bio';
      component.thereIsChanges = true;

      component.handleSaveConfirmation(false);

      expect(profileService.updateBio).not.toHaveBeenCalled();
      expect(component.isEditing).toBe(false);
      expect(component.thereIsChanges).toBe(false);
      expect(component.pendingBio).toBe('');
    });
  });

  describe('Save Confirmation Flow (handleSaveConfirmation)', () => {
    let mockBioComponent: any;

    beforeEach(() => {
      mockBioComponent = {
        updateOriginalBio: vi.fn(),
        discardChanges: vi.fn()
      };
      component.bioComponent = mockBioComponent;

      component.profile$ = of({
        profilePicture: null,
        userName: 'TestUser',
        email: 'test@test.com',
        bio: '',
        isPrivate: false,
        followers: 10,
        following: 5,
        watchedFilms: [],
        laterFilms: [],
        recommendedFilms: [],
        showFilmsToWatchLater: true,
        showRecommendedFilms: true,
        showWatchedFilms: true
      } as MyProfile);
    });

    it('should call updateBio and updateOriginalBio when confirmed is true and bio has changed', () => {
      component.pendingBio = 'Nueva biografía de prueba';
      
      component.handleSaveConfirmation(true);

      expect(profileService.updateBio).toHaveBeenCalledWith('test@test.com', 'Nueva biografía de prueba');
      expect(mockBioComponent.updateOriginalBio).toHaveBeenCalledWith('Nueva biografía de prueba');
      expect(component.showSaveModal).toBe(false);
      expect(component.thereIsChanges).toBe(false);
      expect(component.isEditing).toBe(false);
      expect(component.pendingBio).toBe('')
    });

    it('should call updateAvatar and updateStoredUser when confirmed is true and avatar has changed', () => {
      component.pendingAvatar = 'avatar_seed_123';

      component.handleSaveConfirmation(true);

      expect(profileService.updateAvatar).toHaveBeenCalledWith('test@test.com', 'avatar_seed_123');
      expect(authService.updateStoredUser).toHaveBeenCalledWith({ profilePicture: 'avatar_seed_123' });
      expect(component.pendingAvatar).toBe('');
      expect(component.thereIsChanges).toBe(false);
    });

    it('should call updateListVisibility for each item in pendingVisibility when confirmed is true', () => {
      component.pendingVisibility = [
        { type: 'WatchedFilms', isVisible: false },
        { type: 'RecommendedFilms', isVisible: true }
      ];

      component.handleSaveConfirmation(true);

      expect(profileService.updateListVisibility).toHaveBeenCalledWith('test@test.com', 'WatchedFilms', false);
      expect(profileService.updateListVisibility).toHaveBeenCalledWith('test@test.com', 'RecommendedFilms', true);
      expect(component.pendingVisibility).toEqual([]);
      expect(component.thereIsChanges).toBe(false);
    });

    it('should call discardChanges on bioComponent and reset state without hitting services when confirmed is false', () => {
      component.pendingBio = 'Cambio ignorado';
      component.pendingAvatar = 'avatar_ignorado';
      component.pendingVisibility = [{ type: 'FilmsToWatchLater', isVisible: false }];
      component.thereIsChanges = true;
      component.isEditing = true;

      component.handleSaveConfirmation(false);

      expect(mockBioComponent.discardChanges).toHaveBeenCalled();
      expect(profileService.updateBio).not.toHaveBeenCalled();
      expect(profileService.updateAvatar).not.toHaveBeenCalled();
      expect(profileService.updateListVisibility).not.toHaveBeenCalled();

      expect(component.showSaveModal).toBe(false);
      expect(component.pendingBio).toBe('');
      expect(component.pendingAvatar).toBe('');
      expect(component.pendingVisibility).toEqual([]);
      expect(component.thereIsChanges).toBe(false);
      expect(component.isEditing).toBe(false);
    });
  });
});
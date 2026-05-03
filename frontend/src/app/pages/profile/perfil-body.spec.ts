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

  describe('Lógica de confirmación de Unfollow (BDD)', () => {
    let followService: any;
    let profileService: any;

    beforeEach(() => {
      followService = TestBed.inject(FollowService);
      profileService = TestBed.inject(ProfileService);
      
      component.itsMe$ = of(false);
    });

    it('Dado un usuario en el perfil de un seguido, Cuando pulse dejar de seguir, Entonces muestra confirmación', () => {
      component['followStateSubject'].next('ACCEPTED');
      
      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(component.showConfirmModal).toBe(true);
      expect(component['actionToConfirm']).toBe('UNFOLLOW');
      expect(component.confirmModalMessage).toContain('dejar de seguir a @TargetUser');
      expect(followService.unfollow).not.toHaveBeenCalled(); // Aún no ejecuta la acción
    });

    it('Dado el mensaje de confirmación, Cuando pulse Confirmar, Entonces se cierra, deja de seguir y actualiza estado', () => {
      component['followStateSubject'].next('ACCEPTED');
      component.onFollowRequest('TargetUser', 'target@test.com'); 
      const startFollowers = component.followersCount$.value;

      component.handleUnfollowConfirmation(true);

      expect(component.showConfirmModal).toBe(false); 
      expect(followService.unfollow).toHaveBeenCalledWith('target@test.com'); 
      expect(component['followStateSubject'].value).toBe('NONE');
      expect(component.followersCount$.value).toBe(Math.max(0, startFollowers - 1)); 
    });

    it('Dado el mensaje de confirmación, Cuando pulse Cancelar, Entonces se cierra y no hay cambios', () => {
      component['followStateSubject'].next('ACCEPTED');
      component.onFollowRequest('TargetUser', 'target@test.com');

      component.handleUnfollowConfirmation(false);

      expect(component.showConfirmModal).toBe(false); 
      expect(followService.unfollow).not.toHaveBeenCalled(); 
      expect(component['followStateSubject'].value).toBe('ACCEPTED');
      expect(component['userToFollow']).toBe(''); 
    });
  });

  describe('Lógica de Eliminar Seguidor (BDD)', () => {
    let followService: any;

    beforeEach(() => {
      followService = TestBed.inject(FollowService);
      
      followService.remove = vi.fn().mockReturnValue(of({}));

      component.itsMe$ = of(true);

      component.socialData = [
        { name: 'UsuarioMolesto', email: 'molesto@test.com', avatar: '' },
        { name: 'BuenAmigo', email: 'amigo@test.com', avatar: '' }
      ];
      
      component.followersCount$.next(2);
    });

    it('Dado un usuario en Seguidores, Cuando pulse Eliminar seguidor, Entonces muestra confirmación', () => {
      component.handleSocialAction({
        user: { name: 'UsuarioMolesto', email: 'molesto@test.com' },
        type: 'Seguidores'
      });

      expect(component.showConfirmModal).toBe(true);
      expect(component['actionToConfirm']).toBe('REMOVE_FOLLOWER');
      expect(component.confirmModalMessage).toContain('eliminar a @UsuarioMolesto de tus seguidores');
      
      expect(followService.remove).not.toHaveBeenCalled();
    });

    it('Dado el mensaje de confirmación, Cuando pulse Confirmar, Entonces se cierra, elimina seguidor y actualiza lista', () => {
      component.handleSocialAction({
        user: { name: 'UsuarioMolesto', email: 'molesto@test.com' },
        type: 'Seguidores'
      });


      component.handleUnfollowConfirmation(true);

      expect(component.showConfirmModal).toBe(false); 
      expect(followService.remove).toHaveBeenCalledWith('molesto@test.com'); 

      expect(component.socialData.length).toBe(1);
      expect(component.socialData[0].email).toBe('amigo@test.com');

      expect(component.followersCount$.value).toBe(1);
    });

    it('Dado el mensaje de confirmación, Cuando pulse Cancelar, Entonces se cierra y no hay cambios', () => {
      component.handleSocialAction({
        user: { name: 'UsuarioMolesto', email: 'molesto@test.com' },
        type: 'Seguidores'
      });

      component.handleUnfollowConfirmation(false);

      expect(component.showConfirmModal).toBe(false);
      expect(followService.remove).not.toHaveBeenCalled(); 

      expect(component.socialData.length).toBe(2);
      expect(component.followersCount$.value).toBe(2);
      expect(component['userToFollow']).toBe('');
    });
  });
});
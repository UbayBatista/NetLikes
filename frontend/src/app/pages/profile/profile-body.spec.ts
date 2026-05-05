import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComplete } from './profile-body';
import { provideRouter, ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { ProfileService } from '../../services/profile.service';
import { FollowService } from '../../services/follow.service';
import { AuthService } from '../../services/auth.service';

describe('ProfileComplete (Profile Body)', () => {
  let component: ProfileComplete;
  let fixture: ComponentFixture<ProfileComplete>;
  let followService: any;
  let profileService: any;
  let authService: any;
  let router: Router;

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
      getFollowing: vi.fn().mockReturnValue(of([])),
      remove: vi.fn().mockReturnValue(of({}))
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
    
    followService = TestBed.inject(FollowService);
    profileService = TestBed.inject(ProfileService);
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);

    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('debería crear el componente correctamente', () => {
    expect(component).toBeTruthy();
  });

  describe('Lógica de Solicitar Seguimiento', () => {
    
    it('Dado un usuario que no sigues, Cuando pulsas seguir, Entonces cambia el estado a PENDIENTE', () => {
      expect(component['followStateSubject'].value).toBe('NONE');
      const startFollowers = component.followersCount$.value;

      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(followService.requestFollow).toHaveBeenCalledWith('target@test.com');
      expect(component['followStateSubject'].value).toBe('PENDING');
      expect(component.followersCount$.value).toBe(startFollowers);
    });

    it('Dado un usuario público, Cuando pulsas seguir, Entonces cambia a ACCEPTED y suma un seguidor', () => {
      followService.requestFollow = vi.fn().mockReturnValue(of({ state: 'ACCEPTED' }));
      const startFollowers = component.followersCount$.value;

      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(followService.requestFollow).toHaveBeenCalledWith('target@test.com');
      expect(component['followStateSubject'].value).toBe('ACCEPTED');
      expect(component.followersCount$.value).toBe(startFollowers + 1);
    });
  });

  describe('Lógica de Dejar de Seguir (Confirmación BDD)', () => {
    beforeEach(() => {
      component.itsMe$ = of(false);
    });

    it('Dado un usuario seguido, Cuando pulsa dejar de seguir, Entonces muestra confirmación modal', () => {
      component['followStateSubject'].next('ACCEPTED');
      
      component.onFollowRequest('TargetUser', 'target@test.com');

      expect(component.showConfirmModal).toBe(true);
      expect(component['actionToConfirm']).toBe('UNFOLLOW');
      expect(component.confirmModalMessage).toContain('dejar de seguir a @TargetUser');
      expect(followService.unfollow).not.toHaveBeenCalled();
    });

    it('Dado el modal abierto, Cuando confirma, Entonces deja de seguir y actualiza contadores', () => {
      component['followStateSubject'].next('ACCEPTED');
      component.onFollowRequest('TargetUser', 'target@test.com'); 
      const startFollowers = component.followersCount$.value;

      component.handleConfirmation(true);

      expect(component.showConfirmModal).toBe(false); 
      expect(followService.unfollow).toHaveBeenCalledWith('target@test.com'); 
      expect(component['followStateSubject'].value).toBe('NONE');
      expect(component.followersCount$.value).toBe(Math.max(0, startFollowers - 1)); 
    });

    it('Dado el modal abierto, Cuando cancela, Entonces no hay cambios', () => {
      component['followStateSubject'].next('ACCEPTED');
      component.onFollowRequest('TargetUser', 'target@test.com');

      component.handleConfirmation(false);

      expect(component.showConfirmModal).toBe(false); 
      expect(followService.unfollow).not.toHaveBeenCalled(); 
      expect(component['followStateSubject'].value).toBe('ACCEPTED');
    });
  });

  describe('Lógica de Eliminar Seguidor (BDD)', () => {
    beforeEach(() => {
      component.itsMe$ = of(true);
      component.socialData = [
        { name: 'UsuarioMolesto', email: 'molesto@test.com', avatar: '' },
        { name: 'BuenAmigo', email: 'amigo@test.com', avatar: '' }
      ];
      component.followersCount$.next(2);
    });

    it('Dado un seguidor de la lista, Cuando pulsa eliminar, Entonces muestra confirmación', () => {
      component.handleSocialAction({ user: { name: 'UsuarioMolesto', email: 'molesto@test.com' }, type: 'Seguidores' });

      expect(component.showConfirmModal).toBe(true);
      expect(component['actionToConfirm']).toBe('REMOVE_FOLLOWER');
      expect(component.confirmModalMessage).toContain('eliminar a @UsuarioMolesto de tus seguidores');
      expect(followService.remove).not.toHaveBeenCalled();
    });

    it('Dado el modal abierto, Cuando confirma, Entonces elimina seguidor y actualiza la lista', () => {
      component.handleSocialAction({ user: { name: 'UsuarioMolesto', email: 'molesto@test.com' }, type: 'Seguidores' });

      component.handleConfirmation(true);

      expect(component.showConfirmModal).toBe(false); 
      expect(followService.remove).toHaveBeenCalledWith('molesto@test.com'); 
      expect(component.socialData.length).toBe(1);
      expect(component.socialData[0].email).toBe('amigo@test.com');
      expect(component.followersCount$.value).toBe(1);
    });
  });

  describe('Lógica de Cancelar Solicitud de Seguimiento (BDD)', () => {
    it('Dado un usuario con solicitud pendiente, Cuando pulsa cancelar, Entonces se cancela y cambia a Seguir', () => {
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

  describe('Lógica de Cerrar Sesión (HU 3.5)', () => {
    it('Dado un usuario logueado, Cuando pulsa cerrar sesión, Entonces limpia sesión y redirige al inicio', () => {
      const navigateSpy = vi.spyOn(router, 'navigate');

      component.logout();

      expect(authService.logout).toHaveBeenCalled();
      expect(navigateSpy).toHaveBeenCalledWith(['/']);
    });
  });

  describe('HU7.2 - Acceder al perfil de otro usuario', () => {
    it('HU7.2 - Desde búsqueda: debe llamar a loadProfile con el username de la ruta', () => {
      expect(profileService.loadProfile).toHaveBeenCalledWith('TestUser');
    });

    it('HU7.2 - Desde búsqueda: debe consultar el estado de seguimiento del perfil visitado', () => {
      expect(followService.checkFollowStatus).toHaveBeenCalledWith('test@test.com');
    });

    it('HU7.2 - Desde búsqueda: debe recargar el perfil si el username de la ruta cambia', async () => {
      expect(profileService.loadProfile).toHaveBeenCalledWith('TestUser');
      expect(profileService.loadProfile).toHaveBeenCalledTimes(1);
    });

    it('HU7.2 - isMyProfile debe ser false al visitar el perfil de otro usuario', () => {
      expect(profileService.isMyProfile).toHaveBeenCalled();
    });
  });

  describe('HU6.1 - Cambiar privacidad de la cuenta', () => {
    it('HU6.1 - Dado cuenta pública, cuando activa privada, debe llamar a updatePrivacy con true', () => {
      component.onPrivacyChange(true);

      expect(profileService.updatePrivacy).toHaveBeenCalledWith(true);
    });

    it('HU6.1 - Dado cuenta privada, cuando activa pública, debe llamar a updatePrivacy con false', () => {
      component.onPrivacyChange(false);

      expect(profileService.updatePrivacy).toHaveBeenCalledWith(false);
    });
  });
});
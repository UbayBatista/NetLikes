import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { SocialModal } from './social-modal';

describe('HU7.2 - Acceder al perfil de otro usuario', () => {
  describe('SocialModal - Navegación desde lista de seguidores/seguidos', () => {
    let component: SocialModal;
    let fixture: ComponentFixture<SocialModal>;
    let routerMock: any;

    beforeEach(async () => {
      routerMock = { navigate: vi.fn() };

      await TestBed.configureTestingModule({
        imports: [SocialModal],
        providers: [
          provideRouter([]),
          { provide: Router, useValue: routerMock },
          { provide: ActivatedRoute, useValue: { params: of({}) } }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(SocialModal);
      component = fixture.componentInstance;
    });

    it('HU7.2 - Desde seguidores: debe navegar al perfil del usuario pulsado', () => {
      component.title = 'Seguidores';
      component.users = [{ name: 'ana', avatar: '' }];
      fixture.detectChanges();

      component.goToProfile('ana');

      expect(routerMock.navigate).toHaveBeenCalledWith(['/profile', 'ana']);
    });

    it('HU7.2 - Desde seguidores: debe cerrar el modal al navegar al perfil', () => {
      component.title = 'Seguidores';
      component.users = [];
      fixture.detectChanges();

      const closeSpy = vi.spyOn(component.close, 'emit');
      component.goToProfile('ana');

      expect(closeSpy).toHaveBeenCalled();
    });

    it('HU7.2 - Desde seguidos: debe navegar al perfil del usuario pulsado', () => {
      component.title = 'Seguidos';
      component.users = [{ name: 'carlos', avatar: '' }];
      fixture.detectChanges();

      component.goToProfile('carlos');

      expect(routerMock.navigate).toHaveBeenCalledWith(['/profile', 'carlos']);
    });

    it('HU7.2 - Desde seguidos: debe cerrar el modal al navegar al perfil', () => {
      component.title = 'Seguidos';
      component.users = [];
      fixture.detectChanges();

      const closeSpy = vi.spyOn(component.close, 'emit');
      component.goToProfile('carlos');

      expect(closeSpy).toHaveBeenCalled();
    });

    it('HU7.2 - changeTab debe cambiar entre Seguidores y Seguidos y emitir el evento', () => {
      component.title = 'Seguidores';
      component.users = [];
      fixture.detectChanges();

      const tabChangeSpy = vi.spyOn(component.tabChange, 'emit');
      component.changeTab('Seguidos');

      expect(component.title).toBe('Seguidos');
      expect(tabChangeSpy).toHaveBeenCalledWith('Seguidos');
    });

    it('HU7.2 - changeTab no debe emitir si la pestaña ya está activa', () => {
      component.title = 'Seguidores';
      component.users = [];
      fixture.detectChanges();

      const tabChangeSpy = vi.spyOn(component.tabChange, 'emit');
      component.changeTab('Seguidores');

      expect(tabChangeSpy).not.toHaveBeenCalled();
    });
  });
});
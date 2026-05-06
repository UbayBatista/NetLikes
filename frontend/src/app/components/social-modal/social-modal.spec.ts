import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { SocialModal } from './social-modal';

describe('SocialModal Component (US 7.2)', () => {
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

  describe('Profile Navigation', () => {
    it('should navigate to the clicked user profile from the followers list', () => {
      component.title = 'Seguidores';
      component.users = [{ name: 'ana', avatar: '' }];
      fixture.detectChanges();

      component.goToProfile('ana');

      expect(routerMock.navigate).toHaveBeenCalledWith(['/profile', 'ana']);
    });

    it('should close the modal when navigating to a profile from the followers list', () => {
      component.title = 'Seguidores';
      component.users = [];
      fixture.detectChanges();

      const closeSpy = vi.spyOn(component.close, 'emit');
      component.goToProfile('ana');

      expect(closeSpy).toHaveBeenCalled();
    });

    it('should navigate to the clicked user profile from the following list', () => {
      component.title = 'Seguidos';
      component.users = [{ name: 'carlos', avatar: '' }];
      fixture.detectChanges();

      component.goToProfile('carlos');

      expect(routerMock.navigate).toHaveBeenCalledWith(['/profile', 'carlos']);
    });

    it('should close the modal when navigating to a profile from the following list', () => {
      component.title = 'Seguidos';
      component.users = [];
      fixture.detectChanges();

      const closeSpy = vi.spyOn(component.close, 'emit');
      component.goToProfile('carlos');

      expect(closeSpy).toHaveBeenCalled();
    });
  });

  describe('Tab Switching Logic', () => {
    it('should switch between Seguidores and Seguidos tabs and emit the tabChange event', () => {
      component.title = 'Seguidores';
      component.users = [];
      fixture.detectChanges();

      const tabChangeSpy = vi.spyOn(component.tabChange, 'emit');
      component.changeTab('Seguidos');

      expect(component.title).toBe('Seguidos');
      expect(tabChangeSpy).toHaveBeenCalledWith('Seguidos');
    });

    it('should not emit tabChange event if the target tab is already active', () => {
      component.title = 'Seguidores';
      component.users = [];
      fixture.detectChanges();

      const tabChangeSpy = vi.spyOn(component.tabChange, 'emit');
      component.changeTab('Seguidores');

      expect(tabChangeSpy).not.toHaveBeenCalled();
    });
  });
});
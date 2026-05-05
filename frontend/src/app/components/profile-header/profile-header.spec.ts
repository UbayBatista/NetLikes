import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';

import { ProfileHeader } from './profile-header';

describe('ProfileHeader', () => {
  let component: ProfileHeader;
  let fixture: ComponentFixture<ProfileHeader>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileHeader, CommonModule]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileHeader);
    component = fixture.componentInstance;
  });

  it('should display the userName correctly', () => {
    component.userName = 'Juan Pérez';
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Juan Pérez');
  });

  it('should use default profile picture when userPicture is null', () => {
    component.userPicture = null;
    expect(component.userPicture).toBe('assets/ProfilePicture.jpg');
  });

  it('should use provided picture when userPicture is set', () => {
    component.userPicture = 'assets/custom.jpg';
    expect(component.userPicture).toBe('assets/custom.jpg');
  });

  it('should display followers and following counts', () => {
    component.followers = 10;
    component.following = 5;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('10');
    expect(compiled.textContent).toContain('5');
  });

  it('toggleMenu() should toggle openMenu', () => {
    expect(component.openMenu).toBeFalsy();
    component.toggleMenu();
    expect(component.openMenu).toBeTruthy();
    component.toggleMenu();
    expect(component.openMenu).toBeFalsy();
  });

  it('handleMainAction() should emit editClick when otherUser is No', () => {
    let emitted = false;
    component.otherUser = false;
    component.editClick.subscribe(() => emitted = true);

    component.handleMainAction();

    expect(emitted).toBeTruthy();
  });

  it('handleMainAction() should emit followClick when otherUser is Yes', () => {
    let emitted = false;
    component.otherUser = true;
    component.followClick.subscribe(() => emitted = true);

    component.handleMainAction();

    expect(emitted).toBeTruthy();
  });

  it('openSocial() should emit Seguidores', () => {
    let emitted: string | null = null;
    component.openSocialModal.subscribe(val => emitted = val);

    component.openSocial('Seguidores');

    expect(emitted).toBe('Seguidores');
  });

  it('openSocial() should emit Seguidos', () => {
    let emitted: string | null = null;
    component.openSocialModal.subscribe(val => emitted = val);

    component.openSocial('Seguidos');

    expect(emitted).toBe('Seguidos');
  });

  it('togglePrivacy() should emit negated value of isPrivate', () => {
    let emitted: boolean | null = null;
    component.isPrivate = false;
    component.privacyChange.subscribe(val => emitted = val);

    component.togglePrivacy();

    expect(emitted).toBe(true);
  });

  it('logout() should emit logOut event', () => {
    let emitted = false;
    component.logOut.subscribe(() => emitted = true);

    component.logout();

    expect(emitted).toBeTruthy();
  });
});

describe('HU6.1 - Cambiar privacidad de la cuenta', () => {
  describe('ProfileHeader - Toggle de privacidad', () => {
    let component: ProfileHeader;
    let fixture: ComponentFixture<ProfileHeader>;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ProfileHeader]
      }).compileComponents();

      fixture = TestBed.createComponent(ProfileHeader);
      component = fixture.componentInstance;
    });

    it('HU6.1 - Dado cuenta pública, cuando activa privada, debe emitir true', () => {
      component.isPrivate = false;
      fixture.detectChanges();

      const privacySpy = vi.spyOn(component.privacyChange, 'emit');
      component.togglePrivacy();

      expect(privacySpy).toHaveBeenCalledWith(true);
    });

    it('HU6.1 - Dado cuenta privada, cuando activa pública, debe emitir false', () => {
      component.isPrivate = true;
      fixture.detectChanges();

      const privacySpy = vi.spyOn(component.privacyChange, 'emit');
      component.togglePrivacy();

      expect(privacySpy).toHaveBeenCalledWith(false);
    });

    it('HU6.1 - togglePrivacy debe emitir el valor contrario al actual', () => {
      component.isPrivate = false;
      fixture.detectChanges();

      const privacySpy = vi.spyOn(component.privacyChange, 'emit');

      component.togglePrivacy();
      expect(privacySpy).toHaveBeenCalledWith(true);

      component.isPrivate = true;
      component.togglePrivacy();
      expect(privacySpy).toHaveBeenCalledWith(false);
    });
  });
});
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';

import { ProfileHeader } from './profile-header';

describe('ProfileHeader Component', () => {
  let component: ProfileHeader;
  let fixture: ComponentFixture<ProfileHeader>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileHeader, CommonModule]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileHeader);
    component = fixture.componentInstance;
  });

  describe('UI Rendering', () => {
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
  });

  describe('Menu Toggle Logic', () => {
    it('should toggle openMenu boolean state when toggleMenu is called', () => {
      expect(component.openMenu).toBeFalsy();
      
      component.toggleMenu();
      expect(component.openMenu).toBeTruthy();
      
      component.toggleMenu();
      expect(component.openMenu).toBeFalsy();
    });
  });

  describe('Basic Event Emitters', () => {
    it('should emit editClick when handleMainAction is called and otherUser is false', () => {
      let emitted = false;
      component.otherUser = false;
      component.editClick.subscribe(() => emitted = true);

      component.handleMainAction();

      expect(emitted).toBeTruthy();
    });

    it('should emit followClick when handleMainAction is called and otherUser is true', () => {
      let emitted = false;
      component.otherUser = true;
      component.followClick.subscribe(() => emitted = true);

      component.handleMainAction();

      expect(emitted).toBeTruthy();
    });

    it('should emit openSocialModal with "Seguidores" type', () => {
      let emitted: string | null = null;
      component.openSocialModal.subscribe(val => emitted = val);

      component.openSocial('Seguidores');

      expect(emitted).toBe('Seguidores');
    });

    it('should emit openSocialModal with "Seguidos" type', () => {
      let emitted: string | null = null;
      component.openSocialModal.subscribe(val => emitted = val);

      component.openSocial('Seguidos');

      expect(emitted).toBe('Seguidos');
    });

    it('should emit logOut event when logout is called', () => {
      let emitted = false;
      component.logOut.subscribe(() => emitted = true);

      component.logout();

      expect(emitted).toBeTruthy();
    });
  });

  describe('Block and Delete User Logic', () => {
    it('should emit block event and close menu when applyBlock is called', () => {
      let emitted = false;
      component.block.subscribe(() => emitted = true);
      component.openMenu = true;

      component.applyBlock();

      expect(emitted).toBeTruthy();
      expect(component.openMenu).toBeFalsy();
    });

    it('should emit openBlockedModal event and close menu when showBlockedUsers is called', () => {
      let emitted = false;
      component.openBlockedModal.subscribe(() => emitted = true);
      component.openMenu = true;

      component.showBlockedUsers();

      expect(emitted).toBeTruthy();
      expect(component.openMenu).toBeFalsy();
    });

    it('should emit delete event and close menu when deleteUser is called', () => {
      let emitted = false;
      component.delete.subscribe(() => emitted = true);
      component.openMenu = true;

      component.deleteUser();

      expect(emitted).toBeTruthy();
      expect(component.openMenu).toBeFalsy();
    });
  });

  describe('Account Privacy Logic (US 6.1)', () => {
    it('should emit true when privacy is toggled from a public account', () => {
      component.isPrivate = false;
      fixture.detectChanges();

      const privacySpy = vi.spyOn(component.privacyChange, 'emit');
      component.togglePrivacy();

      expect(privacySpy).toHaveBeenCalledWith(true);
    });

    it('should emit false when privacy is toggled from a private account', () => {
      component.isPrivate = true;
      fixture.detectChanges();

      const privacySpy = vi.spyOn(component.privacyChange, 'emit');
      component.togglePrivacy();

      expect(privacySpy).toHaveBeenCalledWith(false);
    });

    it('should emit the negated value of the current privacy state', () => {
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
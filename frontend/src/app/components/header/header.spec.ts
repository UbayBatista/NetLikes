import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';

import { Header } from './header';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.models';

const mockUser: User = {
  userName: 'Juan',
  email: 'juan@test.com',
  profilePicture: 'assets/custom.jpg'
};

describe('Header', () => {
  let component: Header;
  let fixture: ComponentFixture<Header>;
  let router: Router;

  async function setup(user: User | null) {
    const authServiceMock = { getCurrentUser: () => of(user) };

    await TestBed.configureTestingModule({
      imports: [Header],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  }

  it('should show user profile picture when user is logged in', async () => {
    await setup(mockUser);
    fixture.detectChanges();

    const img = fixture.nativeElement.querySelector('img');
    expect(img?.src).toContain('assets/custom.jpg');
  });

  it('should show default profile picture when user has no picture', async () => {
    await setup({ ...mockUser, profilePicture: null as any });
    fixture.detectChanges();

    const img = fixture.nativeElement.querySelector('img');
    expect(img?.src).toContain('assets/ProfilePicture.jpg');
  });

  it('goToProfile() should navigate to /profile/:userName', async () => {
    await setup(mockUser);
    const navigateSpy = vi.spyOn(router, 'navigate');
    component.userName = 'my-profile';

    component.goToProfile();

    expect(navigateSpy).toHaveBeenCalledWith(['/profile', 'my-profile']);
  });

  it('openBox() should set boxOpen to true and clear hasNotifications', async () => {
    await setup(mockUser);
    component.hasNotifications = true;

    component.openBox();

    expect(component.boxOpen).toBeTruthy();
    expect(component.hasNotifications).toBeFalsy();
  });
});
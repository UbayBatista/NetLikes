import { describe, it, expect, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { CanActivateFn, Router } from '@angular/router';
import { of } from 'rxjs';

import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { User } from '../models/user.models';

const mockUser: User = {
  userName: 'Juan',
  email: 'juan@test.com',
  profilePicture: ''
};

function setupTestBed(user: User | null) {
  const authServiceMock = {
    isLoading: () => of(false),
    getCurrentUser: () => of(user)
  };

  const routerMock = { navigate: () => {} };

  TestBed.configureTestingModule({
    providers: [
      { provide: AuthService, useValue: authServiceMock },
      { provide: Router, useValue: routerMock }
    ]
  });

  return { routerMock };
}

function runGuard(guard: CanActivateFn): Promise<boolean> {
  return new Promise(resolve => {
    TestBed.runInInjectionContext(() => {
      const result = guard({} as any, {} as any);
      (result as any).subscribe((val: boolean) => resolve(val));
    });
  });
}

describe('authGuard', () => {
  beforeEach(() => TestBed.resetTestingModule());

  it('should allow access when user is logged in', async () => {
    setupTestBed(mockUser);
    const result = await runGuard(authGuard);
    expect(result).toBe(true);
  });

  it('should deny access and redirect to /login when user is not logged in', async () => {
    const { routerMock } = setupTestBed(null);
    const navigateSpy = vi.spyOn(routerMock, 'navigate');

    const result = await runGuard(authGuard);

    expect(result).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
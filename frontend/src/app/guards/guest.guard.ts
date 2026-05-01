import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, take,filter, switchMap } from 'rxjs';

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isLoading().pipe(
      filter(loading => !loading),
      take(1),
      switchMap(() => authService.getCurrentUser().pipe(take(1))),
      map(user => {
          if (!user) return true;
          router.navigate(['/home']);
          return false;
      })
  );
};
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../../features/auth/services/auth';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (auth.isLoggedIn() && auth.isAdmin()) return true;

  if (auth.isLoggedIn()) {
    router.navigate(['/intranet']);
    return false;
  }

  router.navigate(['/login']);
  return false;
};

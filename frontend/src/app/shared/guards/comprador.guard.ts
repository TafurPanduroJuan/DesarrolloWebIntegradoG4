import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../../features/auth/services/auth';

export const compradorGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (auth.isLoggedIn() && auth.hasRole('COMPRADOR')) return true;

  if (auth.isLoggedIn()) {
    // Usuario logueado pero con otro rol: lo mandamos a SU intranet, no a login.
    router.navigate(['/intranet']);
    return false;
  }

  router.navigate(['/login']);
  return false;
};

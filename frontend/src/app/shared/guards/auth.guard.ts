import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../../features/auth/services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(Auth);
  const router = inject(Router);
  const usuario = auth.getUsuarioActual();

  // Si no hay sesión → redirigir a login
  if (!usuario) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  // RNF07: Bloquear agricultores con estado "pending"
  if (usuario.rol === 'agricultor' && usuario.status === 'pending') {
    auth.logout();
    router.navigate(['/login'], { 
      queryParams: { pending: 'true' }, 
      replaceUrl: true 
    });
    return false;
  }

  // RNF08: Bloquear usuarios inactivos
  if (usuario.status === 'inactive') {
    auth.logout();
    router.navigate(['/login'], { 
      state: { mensaje: 'Tu cuenta ha sido desactivada.' } 
    });
    return false;
  }

  return true;
};
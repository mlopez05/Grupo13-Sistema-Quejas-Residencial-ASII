import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session';

export const roleGuard: CanActivateFn = (route) => {

  const sessionService = inject(SessionService);
  const router = inject(Router);

  const session = sessionService.obtenerSesion();

  const rolesPermitidos = route.data['roles'] as string[];

  if (session && rolesPermitidos.includes(session.rol)) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { SessionService } from '../services/session';

export const authGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (!sessionService.estaAutenticado()) {
    return router.createUrlTree(['/login']);
  }

  /* CON CONTRASEÑA TEMPORAL PRIMERO DEBE CAMBIARLA */
  if (sessionService.debeCambiarClave()) {
    return router.createUrlTree(['/cambiar-clave-inicial']);
  }

  return true;
};

/* SOLO PARA QUIEN DEBE CAMBIAR SU CONTRASEÑA TEMPORAL */
export const claveTemporalGuard: CanActivateFn = () => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (!sessionService.estaAutenticado()) {
    return router.createUrlTree(['/login']);
  }

  if (!sessionService.debeCambiarClave()) {
    return router.createUrlTree([sessionService.rutaInicio()]);
  }

  return true;
};

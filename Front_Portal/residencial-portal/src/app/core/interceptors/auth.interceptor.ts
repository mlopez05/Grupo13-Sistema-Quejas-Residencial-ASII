import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionService } from '../services/session';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionService = inject(SessionService);
  const session = sessionService.obtenerSesion();

  const esRutaPublica =
    req.url.includes('/api/auth/login') ||
    req.url.includes('/api/registro/residente');

  if (!session?.token || esRutaPublica) {
    return next(req);
  }

  const requestConToken = req.clone({
    setHeaders: {
      Authorization: `Bearer ${session.token}`,
    },
  });

  return next(requestConToken);
};
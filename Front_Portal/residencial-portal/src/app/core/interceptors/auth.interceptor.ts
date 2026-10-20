import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { SessionService } from '../services/session';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionService = inject(SessionService);
  const router = inject(Router);
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

  /* SI LA SESIÓN VENCIÓ SE CIERRA */
  return next(requestConToken).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        sessionService.cerrarSesion();
        router.navigate(['/login']);
      }

      return throwError(() => error);
    })
  );
};

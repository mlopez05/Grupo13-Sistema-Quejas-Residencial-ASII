import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { SessionService } from './session';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { CambiarClaveRequest, LoginRequest, LoginResponse, RecuperacionRequest, RegistroRequest, RegistroRequestPortal, RegistroResponse, ResetPasswordRequest } from '../models/auth.models';
import { finalize } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = environment.authUrl;

  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);

  registrarUsuario(request: RegistroRequest): Observable<RegistroResponse> {
    return this.http.post<RegistroResponse>(
      `${this.apiUrl}/api/registro/residente`,
      request
    );
  }

  registrarAdministrativo(
    request: RegistroRequestPortal
  ): Observable<RegistroResponse> {

    return this.http.post<RegistroResponse>(
      `${this.apiUrl}/api/registro/staff`,
      request
    );
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${this.apiUrl}/api/auth/login`,
      request
    );
  }

  solicitarRecuperacion(
    request: RecuperacionRequest
  ): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/api/auth/forgot-password`,
      request
    );
  }

  restablecerClave(
    request: ResetPasswordRequest
  ): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/api/auth/reset-password`,
      request
    )
  }

  /* CAMBIA LA CONTRASEÑA DESDE EL PERFIL */
  cambiarClave(request: CambiarClaveRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/api/auth/change-password`, request);
  }

  cerrarSesion(): void {
    const sesion = this.sessionService.obtenerSesion();

    if (!sesion) {
      this.router.navigate(['/login']);
      return;
    }

    const headers = new HttpHeaders({
      Authorization: `Bearer ${sesion.token}`,
    });

    this.http
      .post<void>(`${this.apiUrl}/api/auth/logout`, {}, { headers })
      .pipe(
        finalize(() => {
          this.sessionService.cerrarSesion();
          this.router.navigate(['/login']);
        })
      )
      .subscribe({
        error: (error) => {
          console.error(
            'Error al registrar el cierre de sesión:',
            error
          );
        },
      });
  }
}

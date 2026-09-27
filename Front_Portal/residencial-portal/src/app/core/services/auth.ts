import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SessionService } from './session';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { LoginRequest, LoginResponse, RegistroRequest, RegistroRequestPortal, RegistroResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080';

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

  cerrarSesion(): void {
    this.sessionService.cerrarSesion();
    this.router.navigate(['/login']);
  }
}

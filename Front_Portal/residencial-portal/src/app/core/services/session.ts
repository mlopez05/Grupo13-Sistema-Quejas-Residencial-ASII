import { Injectable } from '@angular/core';
import { AuthSession } from '../models/auth.models';

@Injectable({
  providedIn: 'root',
})
export class SessionService {
  private readonly storageKey = 'portal_residencial_session';

  constructor() {
    /* SI CAMBIA LA SESIÓN EN OTRA PESTAÑA SE RECARGA LA PÁGINA */
    window.addEventListener('storage', (event) => {
      if (event.key === this.storageKey || event.key === null) {
        window.location.reload();
      }
    });
  }

  guardarSesion(session: AuthSession): void {
    localStorage.setItem(this.storageKey, JSON.stringify(session));
  }

  obtenerSesion(): AuthSession | null {
    const session = localStorage.getItem(this.storageKey);

    if (!session) {
      return null;
    }

    return JSON.parse(session) as AuthSession;
  }

  cerrarSesion(): void {
    localStorage.removeItem(this.storageKey);
  }

  estaAutenticado(): boolean {
    return this.obtenerSesion() !== null;
  }

  /* INDICA SI DEBE CAMBIAR SU CONTRASEÑA TEMPORAL */
  debeCambiarClave(): boolean {
    return this.obtenerSesion()?.debeCambiarClave === true;
  }

  marcarClaveDefinida(): void {
    const sesion = this.obtenerSesion();

    if (sesion) {
      this.guardarSesion({ ...sesion, debeCambiarClave: false });
    }
  }

  /* PANTALLA DE INICIO SEGÚN EL ROL */
  rutaInicio(): string {
    const rol = this.obtenerSesion()?.rol;

    return rol === 'Administrador' || rol === 'Supervisor' || rol === 'Analista'
      ? '/admin/dashboard'
      : '/dashboard';
  }
}
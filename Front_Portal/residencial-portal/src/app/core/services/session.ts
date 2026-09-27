import { Injectable } from '@angular/core';
import { AuthSession } from '../models/auth.models';

@Injectable({
  providedIn: 'root',
})
export class SessionService {
  private readonly storageKey = 'portal_residencial_session';

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
}
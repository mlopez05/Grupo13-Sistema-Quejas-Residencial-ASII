import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessionService } from '../../core/services/session';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout {

  private readonly sessionService = inject(SessionService);
  private readonly authService = inject(AuthService);

  readonly session = this.sessionService.obtenerSesion();

  cerrarSesion(): void {
    this.authService.cerrarSesion();
  }
}
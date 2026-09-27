import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessionService } from '../../core/services/session';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css',
})
export class AdminLayout {

  private readonly sessionService = inject(SessionService);

  readonly session = this.sessionService.obtenerSesion();

  cerrarSesion(): void {
    this.sessionService.cerrarSesion();
    window.location.href = '/login';
  }
}
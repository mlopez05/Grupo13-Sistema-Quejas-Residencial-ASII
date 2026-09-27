import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessionService } from '../../core/services/session';

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayout {
  private readonly sessionService = inject(SessionService);

  readonly session = this.sessionService.obtenerSesion();

  cerrarSesion(): void {
    this.sessionService.cerrarSesion();
    window.location.href = '/login';
  }
}
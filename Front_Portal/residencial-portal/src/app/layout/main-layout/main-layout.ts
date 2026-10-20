import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessionService } from '../../core/services/session';
import { NotificacionesService } from '../../core/services/notificaciones';
import { CampanaNotificaciones } from '../../features/notificaciones/campana-notificaciones/campana-notificaciones';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../core/iconos';

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CampanaNotificaciones, LucideAngularModule],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayout implements OnInit, OnDestroy {

  readonly icono = ICONOS;

  private readonly sessionService = inject(SessionService);
  private readonly notificacionesService = inject(NotificacionesService);

  readonly session = this.sessionService.obtenerSesion();

  /* CONECTA LAS ALERTAS AL ENTRAR Y LAS CIERRA AL SALIR */
  ngOnInit(): void {
    this.notificacionesService.iniciar();
  }

  ngOnDestroy(): void {
    this.notificacionesService.detener();
  }

  cerrarSesion(): void {
    this.notificacionesService.detener();
    this.sessionService.cerrarSesion();
    window.location.href = '/login';
  }
}

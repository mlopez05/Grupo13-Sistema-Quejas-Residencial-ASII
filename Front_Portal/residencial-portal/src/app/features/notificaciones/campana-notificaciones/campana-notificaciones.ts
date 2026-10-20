import { Component, ElementRef, HostListener, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { NotificacionesService } from '../../../core/services/notificaciones';
import { SessionService } from '../../../core/services/session';
import { NotificacionResponse } from '../../../core/models/notificacion.models';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';

/* CAMPANA DE ALERTAS */
@Component({
  selector: 'app-campana-notificaciones',
  imports: [DatePipe, LucideAngularModule],
  templateUrl: './campana-notificaciones.html',
  styleUrl: './campana-notificaciones.css',
})
export class CampanaNotificaciones {

  readonly icono = ICONOS;

  private readonly elementRef = inject(ElementRef);
  private readonly router = inject(Router);
  private readonly sessionService = inject(SessionService);
  readonly notificaciones = inject(NotificacionesService);

  readonly abierta = signal(false);

  alternar(): void {
    this.abierta.update(valor => !valor);
  }

  abrir(notificacion: NotificacionResponse): void {
    this.notificaciones.marcarLeida(notificacion);
    this.abierta.set(false);

    if (notificacion.idCaso !== null) {
      /* RUTA DEL CASO SEGÚN EL USUARIO */
      const base = this.sessionService.obtenerSesion()?.rol === 'Residente' ? '/casos' : '/admin/casos';
      this.router.navigate([base, notificacion.idCaso]);
    }
  }

  marcarTodas(): void {
    this.notificaciones.marcarTodasLeidas();
  }

  /* CIERRA LA LISTA AL HACER CLIC FUERA */
  @HostListener('document:click', ['$event'])
  alHacerClicFuera(event: MouseEvent): void {
    if (this.abierta() && !this.elementRef.nativeElement.contains(event.target)) {
      this.abierta.set(false);
    }
  }
}

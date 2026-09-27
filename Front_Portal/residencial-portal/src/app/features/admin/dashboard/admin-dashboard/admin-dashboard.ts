import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../../../core/services/session';

@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard {

  private readonly sessionService = inject(SessionService);

  readonly session = this.sessionService.obtenerSesion();

  readonly resumen = {
    usuarios: 128,
    residentes: 115,
    administrativos: 13,
    casosPendientes: 8,
    casosEnProceso: 12,
    casosResueltos: 94,
  };

  readonly actividadReciente = [
    {
      descripcion: 'Nuevo caso registrado',
      detalle: 'Fuga de agua - Casa 24',
      fecha: 'Hace 15 minutos',
    },
    {
      descripcion: 'Caso actualizado',
      detalle: 'Iluminación área común',
      fecha: 'Hace 1 hora',
    },
    {
      descripcion: 'Nuevo usuario registrado',
      detalle: 'Residente',
      fecha: 'Hace 2 horas',
    },
    {
      descripcion: 'Caso resuelto',
      detalle: 'Problema de acceso vehicular',
      fecha: 'Hace 3 horas',
    },
  ];
}
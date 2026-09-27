import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../core/services/session';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly sessionService = inject(SessionService);

  readonly session = this.sessionService.obtenerSesion();

  readonly resumen = {
    total: 5,
    pendientes: 2,
    enProceso: 1,
    resueltos: 2,
  };

  readonly casosRecientes = [
    {
      numero: '#0012',
      titulo: 'Fuga de agua',
      categoria: 'Incidente',
      fecha: '28 Ago 2026',
      estado: 'Pendiente',
    },
    {
      numero: '#0011',
      titulo: 'Iluminación de área común',
      categoria: 'Incidente',
      fecha: '26 Ago 2026',
      estado: 'En proceso',
    },
    {
      numero: '#0010',
      titulo: 'Sugerencia para área verde',
      categoria: 'Sugerencia',
      fecha: '24 Ago 2026',
      estado: 'Resuelto',
    },
  ];
}
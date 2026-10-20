import { Component, inject } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';
import { SessionService } from '../../../core/services/session';
import { ReporteCasos } from './reporte-casos/reporte-casos';
import { ReporteAuditoria } from './reporte-auditoria/reporte-auditoria';

/* REPORTES */
@Component({
  selector: 'app-reportes',
  imports: [LucideAngularModule, ReporteCasos, ReporteAuditoria],
  templateUrl: './reportes.html',
})
export class Reportes {

  readonly icono = ICONOS;

  readonly esAdministrador = inject(SessionService).obtenerSesion()?.rol === 'Administrador';

  pestana: 'casos' | 'auditoria' = 'casos';
}

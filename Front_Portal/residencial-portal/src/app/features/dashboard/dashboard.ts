import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { SessionService } from '../../core/services/session';
import { CasosService } from '../../core/services/casos';
import { NotificacionesService } from '../../core/services/notificaciones';
import { CasoResponse, ESTILO_ESTADO, EstadoCaso } from '../../core/models/caso.models';
import { NovedadCaso } from '../../core/models/notificacion.models';
import { LucideAngularModule, LucideIconData } from 'lucide-angular';
import { ICONOS } from '../../core/iconos';

interface Tarjeta {
  titulo: string;
  valor: number;
  detalle: string;
  icono: LucideIconData;
  claseIcono: string;
  /* FILTRO AL HACER CLIC */
  estado?: EstadoCaso;
}

const EN_ESPERA: EstadoCaso[] = ['NUEVO', 'ASIGNADO'];
const EN_ATENCION: EstadoCaso[] = ['EN_ANALISIS', 'PENDIENTE_AUTORIZACION', 'EN_PROCESO'];

/* PANEL DE INICIO DEL RESIDENTE */
@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe, LucideAngularModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {

  readonly icono = ICONOS;

  private readonly sessionService = inject(SessionService);
  private readonly casosService = inject(CasosService);
  private readonly notificacionesService = inject(NotificacionesService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly session = this.sessionService.obtenerSesion();
  readonly estiloEstado = ESTILO_ESTADO;

  casos: CasoResponse[] = [];
  cargando = true;
  errorCarga = '';

  ngOnInit(): void {
    this.cargarCasos();
  }

  cargarCasos(): void {
    this.cargando = true;
    this.errorCarga = '';

    this.casosService.listarMisCasos().subscribe({
      next: (casos) => {
        this.casos = casos;
        this.cargando = false;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.errorCarga = error.error?.mensaje ?? 'No se pudo cargar el resumen de tus casos.';
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  get tarjetas(): Tarjeta[] {
    const contar = (estados: EstadoCaso[]) => this.casos.filter(c => estados.includes(c.estado)).length;
    const rechazados = contar(['RECHAZADO']);

    return [
      {
        titulo: 'Total de casos',
        valor: this.casos.length,
        detalle: 'Casos que has reportado',
        icono: ICONOS.ClipboardList,
        claseIcono: 'bg-blue-50 text-blue-600',
      },
      {
        titulo: 'En espera',
        valor: contar(EN_ESPERA),
        detalle: 'Registrados, pendientes de análisis',
        icono: ICONOS.Clock,
        claseIcono: 'bg-amber-50 text-amber-600',
      },
      {
        titulo: 'En atención',
        valor: contar(EN_ATENCION),
        detalle: 'En análisis, autorización o ejecución',
        icono: ICONOS.RefreshCw,
        claseIcono: 'bg-purple-50 text-purple-600',
      },
      {
        titulo: 'Finalizados',
        valor: contar(['FINALIZADO']),
        detalle: rechazados > 0 ? `Resueltos · ${rechazados} rechazado(s)` : 'Casos resueltos',
        icono: ICONOS.CircleCheck,
        claseIcono: 'bg-emerald-50 text-emerald-600',
        estado: 'FINALIZADO',
      },
    ];
  }

  /* CASOS RECIENTES */
  get recientes(): CasoResponse[] {
    return [...this.casos]
      .sort((a, b) => b.fechaActualizacion.localeCompare(a.fechaActualizacion))
      .slice(0, 5);
  }

  get totalConNovedad(): number {
    return this.casos.filter(caso => this.novedad(caso) !== null).length;
  }

  novedad(caso: CasoResponse): NovedadCaso | null {
    return this.notificacionesService.novedadDe(caso.id);
  }
}

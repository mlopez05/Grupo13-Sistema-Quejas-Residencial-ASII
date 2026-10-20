import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { SessionService } from '../../../../core/services/session';
import { CasosService } from '../../../../core/services/casos';
import { UsuarioService } from '../../../../core/services/usuario';
import { NotificacionesService } from '../../../../core/services/notificaciones';
import { CasoResponse, ESTILO_ESTADO, EstadoCaso } from '../../../../core/models/caso.models';
import { UsuarioResponse } from '../../../../core/models/usuario.models';
import { NovedadCaso } from '../../../../core/models/notificacion.models';
import { LucideAngularModule, LucideIconData } from 'lucide-angular';
import { ICONOS } from '../../../../core/iconos';

interface Tarjeta {
  titulo: string;
  valor: number;
  detalle: string;
  icono: LucideIconData;
  claseIcono: string;
  /* FILTRO AL HACER CLIC */
  estado?: EstadoCaso;
  /* RESALTA LA TARJETA */
  destacar?: boolean;
}

/* PANEL DE INICIO DEL PERSONAL */
@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink, DatePipe, LucideAngularModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit {

  readonly icono = ICONOS;

  private readonly sessionService = inject(SessionService);
  private readonly casosService = inject(CasosService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly notificacionesService = inject(NotificacionesService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly session = this.sessionService.obtenerSesion();
  readonly rol = this.session?.rol ?? '';
  readonly esAdministrador = this.rol === 'Administrador';
  readonly estiloEstado = ESTILO_ESTADO;

  casos: CasoResponse[] = [];
  usuarios: UsuarioResponse[] = [];
  cargando = true;
  errorCarga = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.errorCarga = '';

    forkJoin({
      casos: this.casosService.listarBandeja(),
      usuarios: this.esAdministrador ? this.usuarioService.listarUsuarios() : of([] as UsuarioResponse[]),
    }).subscribe({
      next: ({ casos, usuarios }) => {
        this.casos = casos;
        this.usuarios = usuarios;
        this.cargando = false;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.errorCarga = error.error?.mensaje ?? 'No se pudo cargar el resumen.';
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  get subtitulo(): string {
    return this.rol === 'Analista'
      ? 'Resumen de los casos que tienes asignados.'
      : 'Resumen de la actividad del residencial.';
  }

  get tarjetasCasos(): Tarjeta[] {
    const contar = (estados: EstadoCaso[]) => this.casos.filter(c => estados.includes(c.estado)).length;

    if (this.rol === 'Analista') {
      return [
        { titulo: 'Por iniciar', valor: contar(['ASIGNADO']), detalle: 'Asignados, sin análisis iniciado',
          icono: ICONOS.Inbox, claseIcono: 'bg-indigo-50 text-indigo-600', estado: 'ASIGNADO',
          destacar: contar(['ASIGNADO']) > 0 },
        { titulo: 'En análisis', valor: contar(['EN_ANALISIS']), detalle: 'Pendientes de solicitar autorización',
          icono: ICONOS.Search, claseIcono: 'bg-amber-50 text-amber-600', estado: 'EN_ANALISIS' },
        { titulo: 'Esperando autorización', valor: contar(['PENDIENTE_AUTORIZACION']), detalle: 'En manos del Supervisor',
          icono: ICONOS.Clock, claseIcono: 'bg-orange-50 text-orange-600', estado: 'PENDIENTE_AUTORIZACION' },
        { titulo: 'En proceso', valor: contar(['EN_PROCESO']), detalle: 'Autorizados, por ejecutar y cerrar',
          icono: ICONOS.Wrench, claseIcono: 'bg-blue-50 text-blue-600', estado: 'EN_PROCESO' },
      ];
    }

    const rechazados = contar(['RECHAZADO']);
    const pendientes = contar(['PENDIENTE_AUTORIZACION']);

    return [
      { titulo: 'Sin asignar', valor: contar(['NUEVO']), detalle: 'Esperando analista',
        icono: ICONOS.Inbox, claseIcono: 'bg-sky-50 text-sky-600', estado: 'NUEVO',
        destacar: this.rol === 'Supervisor' && contar(['NUEVO']) > 0 },
      { titulo: 'Pendientes de autorización', valor: pendientes, detalle: 'Requieren decisión del Supervisor',
        icono: ICONOS.Clock, claseIcono: 'bg-orange-50 text-orange-600', estado: 'PENDIENTE_AUTORIZACION',
        destacar: this.rol === 'Supervisor' && pendientes > 0 },
      { titulo: 'En atención', valor: contar(['ASIGNADO', 'EN_ANALISIS', 'EN_PROCESO']),
        detalle: 'Asignados, en análisis o en proceso', icono: ICONOS.RefreshCw, claseIcono: 'bg-purple-50 text-purple-600' },
      { titulo: 'Finalizados', valor: contar(['FINALIZADO']),
        detalle: rechazados > 0 ? `Resueltos · ${rechazados} rechazado(s)` : 'Casos resueltos',
        icono: ICONOS.CircleCheck, claseIcono: 'bg-emerald-50 text-emerald-600', estado: 'FINALIZADO' },
    ];
  }

  get tarjetasUsuarios(): Tarjeta[] {
    const residentes = this.usuarios.filter(u => u.rol === 'Residente').length;
    const inactivos = this.usuarios.filter(u => u.estado === 'Inactivo').length;

    return [
      { titulo: 'Total de usuarios', valor: this.usuarios.length, detalle: 'Usuarios registrados',
        icono: ICONOS.Users, claseIcono: 'bg-blue-50 text-blue-600' },
      { titulo: 'Residentes', valor: residentes, detalle: 'Usuarios residenciales',
        icono: ICONOS.House, claseIcono: 'bg-sky-50 text-sky-600' },
      { titulo: 'Personal', valor: this.usuarios.length - residentes, detalle: 'Analistas, supervisores y administradores',
        icono: ICONOS.UserCog, claseIcono: 'bg-indigo-50 text-indigo-600' },
      { titulo: 'Inactivos', valor: inactivos, detalle: 'Sin acceso al portal',
        icono: ICONOS.Ban, claseIcono: 'bg-slate-100 text-slate-600' },
    ];
  }

  /* CASOS RECIENTES */
  get recientes(): CasoResponse[] {
    return [...this.casos]
      .sort((a, b) => b.fechaActualizacion.localeCompare(a.fechaActualizacion))
      .slice(0, 6);
  }

  get totalConNovedad(): number {
    return this.casos.filter(caso => this.novedad(caso) !== null).length;
  }

  novedad(caso: CasoResponse): NovedadCaso | null {
    return this.notificacionesService.novedadDe(caso.id);
  }

  claseFilaReciente(caso: CasoResponse): string {
    switch (this.novedad(caso)) {
      case 'nuevo': return 'border-amber-400 bg-amber-50 hover:bg-amber-100';
      case 'actualizado': return 'border-blue-500 bg-blue-50 hover:bg-blue-100';
      default: return 'border-transparent hover:bg-slate-50';
    }
  }
}

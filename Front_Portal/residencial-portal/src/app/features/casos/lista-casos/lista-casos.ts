import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CasosService } from '../../../core/services/casos';
import { SessionService } from '../../../core/services/session';
import { NotificacionesService } from '../../../core/services/notificaciones';
import { CasoResponse, ESTADOS_CASO, ESTILO_ESTADO, EstadoCaso } from '../../../core/models/caso.models';
import { NovedadCaso } from '../../../core/models/notificacion.models';

/* LISTA DE CASOS */
type ModoLista = 'residente' | 'personal';

@Component({
  selector: 'app-lista-casos',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './lista-casos.html',
  styleUrl: './lista-casos.css',
})
export class ListaCasos implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly casosService = inject(CasosService);
  private readonly sessionService = inject(SessionService);
  private readonly notificacionesService = inject(NotificacionesService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly modo: ModoLista = this.route.snapshot.data['modo'] ?? 'residente';
  readonly rol = this.sessionService.obtenerSesion()?.rol ?? '';
  readonly estados = ESTADOS_CASO;
  readonly estiloEstado = ESTILO_ESTADO;

  casos: CasoResponse[] = [];
  cargando = true;
  errorCarga = '';

  terminoBusqueda = '';
  estadoSeleccionado: EstadoCaso | '' = '';

  ngOnInit(): void {
    /* FILTRO INICIAL */
    const estadoUrl = this.route.snapshot.queryParamMap.get('estado');

    if (estadoUrl && ESTADOS_CASO.some(e => e.valor === estadoUrl)) {
      this.estadoSeleccionado = estadoUrl as EstadoCaso;
    } else if (estadoUrl === 'todos') {
      this.estadoSeleccionado = '';
    } else if (this.rol === 'Supervisor') {
      this.estadoSeleccionado = 'PENDIENTE_AUTORIZACION';
    }

    this.cargarCasos();
  }

  get esPersonal(): boolean {
    return this.modo === 'personal';
  }

  get titulo(): string {
    if (!this.esPersonal) {
      return 'Mis casos';
    }

    return this.rol === 'Analista' ? 'Casos asignados' : 'Bandeja de casos';
  }

  get subtitulo(): string {
    if (!this.esPersonal) {
      return 'Consulta el estado y el seguimiento de los casos que has reportado.';
    }

    return this.rol === 'Analista'
      ? 'Casos que tienes asignados para análisis y ejecución.'
      : 'Todos los casos registrados en el residencial.';
  }

  get rutaBase(): string {
    return this.esPersonal ? '/admin/casos' : '/casos';
  }

  cargarCasos(): void {
    this.cargando = true;
    this.errorCarga = '';

    const peticion: Observable<CasoResponse[]> = this.esPersonal
      ? this.casosService.listarBandeja()
      : this.casosService.listarMisCasos();

    peticion.subscribe({
      next: (casos) => {
        this.casos = casos;
        this.cargando = false;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.errorCarga =
          error.error?.mensaje ??
          'No se pudieron cargar los casos.';
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  get casosFiltrados(): CasoResponse[] {
    const termino = this.terminoBusqueda.toLowerCase().trim();

    return this.casos.filter(caso => {

      const coincideBusqueda =
        !termino ||
        caso.numeroCaso.toLowerCase().includes(termino) ||
        caso.tipoIncidente.toLowerCase().includes(termino) ||
        caso.categoria.toLowerCase().includes(termino) ||
        (this.esPersonal && caso.nombreResidente.toLowerCase().includes(termino));

      const coincideEstado =
        !this.estadoSeleccionado ||
        caso.estado === this.estadoSeleccionado;

      return coincideBusqueda && coincideEstado;
    });
  }

  /* RESALTA LOS CASOS CON NOVEDADES */
  novedad(caso: CasoResponse): NovedadCaso | null {
    return this.notificacionesService.novedadDe(caso.id);
  }

  claseFila(caso: CasoResponse): string {
    switch (this.novedad(caso)) {
      case 'nuevo': return 'bg-amber-50 transition hover:bg-amber-100';
      case 'actualizado': return 'bg-blue-50 transition hover:bg-blue-100';
      default: return 'transition hover:bg-slate-50';
    }
  }

  claseMarca(caso: CasoResponse): string {
    switch (this.novedad(caso)) {
      case 'nuevo': return 'border-l-4 border-amber-400';
      case 'actualizado': return 'border-l-4 border-blue-500';
      default: return 'border-l-4 border-transparent';
    }
  }

  get totalConNovedad(): number {
    return this.casos.filter(caso => this.novedad(caso) !== null).length;
  }

  contarPorEstado(estado: EstadoCaso): number {
    return this.casos.filter(caso => caso.estado === estado).length;
  }

  seleccionarEstado(estado: EstadoCaso | ''): void {
    this.estadoSeleccionado = this.estadoSeleccionado === estado ? '' : estado;
  }
}

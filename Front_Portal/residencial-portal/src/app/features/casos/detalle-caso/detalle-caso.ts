import { ChangeDetectorRef, Component, DestroyRef, OnDestroy, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse, HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable, filter } from 'rxjs';
import { SelectorAdjuntos } from '../selector-adjuntos/selector-adjuntos';
import { CasosService } from '../../../core/services/casos';
import { SessionService } from '../../../core/services/session';
import { NotificacionesService } from '../../../core/services/notificaciones';
import {
  AnalistaResponse,
  CasoDetalleResponse,
  DocumentoResponse,
  ESTADOS_CASO,
  ESTILO_ESTADO,
  ETIQUETA_SEGUIMIENTO,
  EstadoCaso,
  ICONO_CATEGORIA,
  SeguimientoResponse,
  esEstadoFinal,
  formatearTamano,
} from '../../../core/models/caso.models';
import { LucideAngularModule, LucideIconData } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';

type Accion = 'informacion' | 'iniciar' | 'avance' | 'solicitar' | 'autorizar' | 'rechazar' | 'cerrar' | 'asignar';

interface ConfigAccion {
  titulo: string;
  descripcion: string;
  boton: string;
  exito: string;
  pideComentario: boolean;
  comentarioObligatorio: boolean;
  /* PERMITE ADJUNTAR ARCHIVOS */
  permiteAdjuntos: boolean;
  etiquetaComentario: string;
  placeholder: string;
  claseBoton: string;
}

const COMENTARIO_MAX = 1000;

/* ACCIONES DEL CASO */
const ACCIONES: Record<Accion, ConfigAccion> = {
  informacion: {
    titulo: 'Agregar información',
    descripcion: 'Comparte detalles o evidencia adicional. Quedará en el seguimiento y se avisará al analista.',
    boton: 'Agregar información',
    exito: 'La información fue agregada al caso.',
    pideComentario: true,
    comentarioObligatorio: true,
    permiteAdjuntos: true,
    etiquetaComentario: 'Información adicional',
    placeholder: 'Ej. El problema empeoró esta mañana; adjunto fotos de cómo se ve ahora.',
    claseBoton: 'bg-blue-600 hover:bg-blue-700',
  },
  iniciar: {
    titulo: 'Iniciar análisis',
    descripcion: 'El caso pasará a "En análisis" y el residente será notificado.',
    boton: 'Iniciar análisis',
    exito: 'El análisis del caso fue iniciado.',
    pideComentario: false,
    comentarioObligatorio: false,
    permiteAdjuntos: false,
    etiquetaComentario: '',
    placeholder: '',
    claseBoton: 'bg-blue-600 hover:bg-blue-700',
  },
  avance: {
    titulo: 'Registrar avance',
    descripcion: 'Deja constancia de lo realizado. El avance y sus adjuntos quedan en el historial del caso.',
    boton: 'Registrar avance',
    exito: 'El avance fue registrado.',
    pideComentario: true,
    comentarioObligatorio: true,
    permiteAdjuntos: true,
    etiquetaComentario: 'Detalle del avance',
    placeholder: 'Ej. Se inspeccionó el área y se identificó el origen de la fuga.',
    claseBoton: 'bg-blue-600 hover:bg-blue-700',
  },
  solicitar: {
    titulo: 'Solicitar autorización',
    descripcion: 'El caso se trasladará al Supervisor para que autorice o rechace la solución propuesta.',
    boton: 'Enviar a autorización',
    exito: 'El caso fue enviado al Supervisor para su autorización.',
    pideComentario: true,
    comentarioObligatorio: true,
    permiteAdjuntos: true,
    etiquetaComentario: 'Análisis y solución propuesta',
    placeholder: 'Describe el diagnóstico y la acción que propones ejecutar.',
    claseBoton: 'bg-orange-600 hover:bg-orange-700',
  },
  autorizar: {
    titulo: 'Autorizar caso',
    descripcion: 'El caso pasará a "En proceso" para que el analista ejecute la solución.',
    boton: 'Autorizar',
    exito: 'El caso fue autorizado.',
    pideComentario: true,
    comentarioObligatorio: false,
    permiteAdjuntos: false,
    etiquetaComentario: 'Comentario (opcional)',
    placeholder: 'Indicaciones para el analista.',
    claseBoton: 'bg-emerald-600 hover:bg-emerald-700',
  },
  rechazar: {
    titulo: 'Rechazar caso',
    descripcion: 'El caso quedará "Rechazado" de forma definitiva. Se notificará al analista y al residente.',
    boton: 'Rechazar',
    exito: 'El caso fue rechazado.',
    pideComentario: true,
    comentarioObligatorio: true,
    permiteAdjuntos: false,
    etiquetaComentario: 'Motivo del rechazo',
    placeholder: 'Explica por qué no se autoriza.',
    claseBoton: 'bg-red-600 hover:bg-red-700',
  },
  cerrar: {
    titulo: 'Cerrar caso',
    descripcion: 'El caso quedará "Finalizado" y el residente recibirá la notificación de cierre.',
    boton: 'Cerrar caso',
    exito: 'El caso fue finalizado.',
    pideComentario: true,
    comentarioObligatorio: true,
    permiteAdjuntos: true,
    etiquetaComentario: 'Resolución',
    placeholder: 'Describe la solución ejecutada.',
    claseBoton: 'bg-emerald-600 hover:bg-emerald-700',
  },
  asignar: {
    titulo: 'Asignar analista',
    descripcion: 'Selecciona el analista que atenderá el caso. Se muestran sus casos activos.',
    boton: 'Asignar',
    exito: 'El analista fue asignado.',
    pideComentario: false,
    comentarioObligatorio: false,
    permiteAdjuntos: false,
    etiquetaComentario: '',
    placeholder: '',
    claseBoton: 'bg-blue-600 hover:bg-blue-700',
  },
};

/* COLOR DEL MOVIMIENTO */
const COLOR_SEGUIMIENTO: Record<string, string> = {
  REGISTRO: 'bg-sky-500',
  ASIGNACION: 'bg-indigo-500',
  REASIGNACION: 'bg-indigo-500',
  INICIO_ANALISIS: 'bg-amber-500',
  AVANCE: 'bg-slate-400',
  INFORMACION_ADICIONAL: 'bg-sky-400',
  ADJUNTO: 'bg-slate-400',
  SOLICITUD_AUTORIZACION: 'bg-orange-500',
  AUTORIZACION: 'bg-emerald-500',
  RECHAZO: 'bg-red-500',
  CIERRE: 'bg-emerald-600',
};

/* DETALLE DEL CASO */
@Component({
  selector: 'app-detalle-caso',
  imports: [FormsModule, RouterLink, DatePipe, SelectorAdjuntos, LucideAngularModule],
  templateUrl: './detalle-caso.html',
  styleUrl: './detalle-caso.css',
})
export class DetalleCaso implements OnInit, OnDestroy {

  readonly icono = ICONOS;

  private readonly route = inject(ActivatedRoute);
  private readonly casosService = inject(CasosService);
  private readonly sessionService = inject(SessionService);
  private readonly notificacionesService = inject(NotificacionesService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  private readonly sesion = this.sessionService.obtenerSesion();
  private readonly idCaso = Number(this.route.snapshot.paramMap.get('id'));

  readonly esPersonal = this.route.snapshot.data['modo'] === 'personal';
  readonly rol = this.sesion?.rol ?? '';
  readonly estiloEstado = ESTILO_ESTADO;
  readonly etiquetaSeguimiento = ETIQUETA_SEGUIMIENTO;
  readonly comentarioMax = COMENTARIO_MAX;
  readonly formatearTamano = formatearTamano;

  detalle: CasoDetalleResponse | null = null;
  cargando = true;
  errorCarga = '';

  accionActiva: Accion | null = null;
  comentario = '';
  archivosAccion: File[] = [];
  progresoCarga: number | null = null;
  analistas: AnalistaResponse[] = [];
  cargandoAnalistas = false;
  idAnalistaSeleccionado: number | null = null;
  ejecutando = false;
  errorAccion = '';
  mensajeExito = '';

  descargandoId: number | null = null;
  errorDescarga = '';
  private readonly miniaturas = new Map<number, string>();

  ngOnInit(): void {
    this.cargarDetalle();

    /* SI LLEGA UNA ALERTA DEL CASO SE ACTUALIZA */
    this.notificacionesService.nuevas$
      .pipe(
        filter(notificacion => notificacion.idCaso === this.idCaso),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.recargarDetalle());
  }

  ngOnDestroy(): void {
    this.miniaturas.forEach(url => URL.revokeObjectURL(url));
    this.miniaturas.clear();
  }

  get rutaVolver(): string {
    return this.esPersonal ? '/admin/casos' : '/casos';
  }

  cargarDetalle(): void {
    if (!Number.isInteger(this.idCaso) || this.idCaso <= 0) {
      this.cargando = false;
      this.errorCarga = 'El identificador del caso no es válido.';
      return;
    }

    this.cargando = true;
    this.errorCarga = '';

    this.casosService.obtenerDetalle(this.idCaso).subscribe({
      next: (detalle) => this.mostrarDetalle(detalle),

      error: (error: HttpErrorResponse) => {
        this.errorCarga =
          error.error?.mensaje ??
          'No se pudo cargar el caso.';
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  /* ACTUALIZA EL CASO */
  private recargarDetalle(): void {
    this.casosService.obtenerDetalle(this.idCaso).subscribe({
      next: (detalle) => this.mostrarDetalle(detalle),
    });
  }

  private mostrarDetalle(detalle: CasoDetalleResponse): void {
    this.detalle = detalle;
    this.cargando = false;
    this.cargarMiniaturas();

    /* MARCA COMO LEÍDAS LAS ALERTAS DEL CASO */
    this.notificacionesService.marcarCasoLeido(detalle.caso.id);

    this.cdr.markForCheck();
  }

  /* ACCIONES DISPONIBLES */

  private get esAnalistaAsignado(): boolean {
    return this.rol === 'Analista' &&
      !!this.detalle?.caso.correoAnalista &&
      this.detalle.caso.correoAnalista === this.sesion?.usuario;
  }

  get accionesDisponibles(): Accion[] {
    if (!this.detalle) {
      return [];
    }

    const estado = this.detalle.caso.estado;

    /* EL RESIDENTE PUEDE AGREGAR INFORMACIÓN */
    if (!this.esPersonal) {
      return this.rol === 'Residente' && !esEstadoFinal(estado) ? ['informacion'] : [];
    }

    if (this.esAnalistaAsignado) {
      switch (estado) {
        case 'ASIGNADO': return ['iniciar'];
        case 'EN_ANALISIS': return ['avance', 'solicitar'];
        case 'EN_PROCESO': return ['avance', 'cerrar'];
        default: return [];
      }
    }

    if (this.rol === 'Supervisor') {
      const acciones: Accion[] = estado === 'PENDIENTE_AUTORIZACION' ? ['autorizar', 'rechazar'] : [];

      if (!esEstadoFinal(estado)) {
        acciones.push('asignar');
      }

      return acciones;
    }

    return [];
  }

  /* MENSAJE CUANDO NO HAY ACCIONES */
  get mensajeSinAcciones(): string {
    if (!this.detalle) {
      return '';
    }

    const estado = this.detalle.caso.estado;

    if (esEstadoFinal(estado)) {
      return 'El caso está cerrado. No hay acciones pendientes.';
    }

    if (this.rol === 'Analista' && !this.esAnalistaAsignado) {
      return 'Este caso no está asignado a ti.';
    }

    if (estado === 'PENDIENTE_AUTORIZACION') {
      return 'En espera de la decisión del Supervisor.';
    }

    if (this.rol === 'Administrador') {
      return 'Vista de solo lectura.';
    }

    return 'En espera de la gestión del analista.';
  }

  configDe(accion: Accion): ConfigAccion {
    return ACCIONES[accion];
  }

  claseBotonPanel(accion: Accion): string {
    const base = 'w-full rounded-xl px-4 py-3 text-sm font-semibold transition ';

    if (accion === 'rechazar') {
      return base + 'border border-red-200 bg-white text-red-600 hover:bg-red-50';
    }

    if (accion === 'avance' || accion === 'asignar') {
      return base + 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50';
    }

    return base + 'text-white ' + ACCIONES[accion].claseBoton;
  }

  get configActiva(): ConfigAccion | null {
    return this.accionActiva ? ACCIONES[this.accionActiva] : null;
  }

  abrirAccion(accion: Accion): void {
    this.accionActiva = accion;
    this.comentario = '';
    this.archivosAccion = [];
    this.progresoCarga = null;
    this.errorAccion = '';
    this.idAnalistaSeleccionado = null;

    if (accion === 'asignar') {
      this.cargarAnalistas();
    }
  }

  cerrarAccion(): void {
    if (!this.ejecutando) {
      this.accionActiva = null;
    }
  }

  private cargarAnalistas(): void {
    this.cargandoAnalistas = true;

    this.casosService.listarAnalistas().subscribe({
      next: (analistas) => {
        /* NO SE MUESTRA EL ANALISTA ACTUAL */
        this.analistas = analistas.filter(a => a.id !== this.detalle?.caso.idAnalista);
        this.cargandoAnalistas = false;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.errorAccion =
          error.error?.mensaje ??
          'No se pudo cargar la lista de analistas.';
        this.cargandoAnalistas = false;
        this.cdr.markForCheck();
      },
    });
  }

  get puedeConfirmar(): boolean {
    const config = this.configActiva;

    if (!config || this.ejecutando) {
      return false;
    }

    if (this.accionActiva === 'asignar') {
      return this.idAnalistaSeleccionado !== null;
    }

    return !config.comentarioObligatorio || this.comentario.trim().length > 0;
  }

  confirmarAccion(): void {
    if (!this.detalle || !this.accionActiva || !this.puedeConfirmar) {
      return;
    }

    const id = this.detalle.caso.id;
    const comentario = this.comentario.trim();
    const archivos = this.archivosAccion;
    const accion = this.accionActiva;

    /* PROGRESO DE LA CARGA */
    let peticion: Observable<CasoDetalleResponse | HttpEvent<CasoDetalleResponse>>;

    switch (accion) {
      case 'informacion': peticion = this.casosService.agregarInformacion(id, comentario, archivos); break;
      case 'iniciar': peticion = this.casosService.iniciarAnalisis(id); break;
      case 'avance': peticion = this.casosService.registrarAvance(id, comentario, archivos); break;
      case 'solicitar': peticion = this.casosService.solicitarAutorizacion(id, comentario, archivos); break;
      case 'autorizar': peticion = this.casosService.autorizar(id, comentario || null); break;
      case 'rechazar': peticion = this.casosService.rechazar(id, comentario); break;
      case 'cerrar': peticion = this.casosService.cerrar(id, comentario, archivos); break;
      case 'asignar': peticion = this.casosService.asignarAnalista(id, this.idAnalistaSeleccionado!); break;
    }

    this.ejecutando = true;
    this.errorAccion = '';
    this.progresoCarga = archivos.length > 0 ? 0 : null;

    peticion.subscribe({
      next: (respuesta) => {
        if (!esEvento(respuesta)) {
          this.accionCompletada(respuesta, accion);
          return;
        }

        if (respuesta.type === HttpEventType.UploadProgress && respuesta.total && this.progresoCarga !== null) {
          this.progresoCarga = Math.round((respuesta.loaded / respuesta.total) * 100);
          this.cdr.markForCheck();
        }

        if (respuesta.type === HttpEventType.Response && respuesta.body) {
          this.accionCompletada(respuesta.body, accion);
        }
      },

      error: (error: HttpErrorResponse) => {
        this.ejecutando = false;
        this.progresoCarga = null;
        this.errorAccion =
          error.error?.mensaje ??
          'No se pudo completar la acción.';
        this.cdr.markForCheck();
      },
    });
  }

  private accionCompletada(detalle: CasoDetalleResponse, accion: Accion): void {
    this.detalle = detalle;
    this.ejecutando = false;
    this.progresoCarga = null;
    this.accionActiva = null;
    this.archivosAccion = [];
    this.mensajeExito = ACCIONES[accion].exito;
    this.cargarMiniaturas();
    this.cdr.markForCheck();
  }

  cerrarMensajeExito(): void {
    this.mensajeExito = '';
  }

  /* ADJUNTOS */

  get documentos(): DocumentoResponse[] {
    return this.detalle?.seguimientos.flatMap(s => s.documentos) ?? [];
  }

  /* TOTAL ADJUNTADO AL CASO */
  get bytesAdjuntados(): number {
    return this.documentos.reduce((total, doc) => total + doc.tamanoBytes, 0);
  }

  descargar(documento: DocumentoResponse): void {
    if (!this.detalle || this.descargandoId !== null) {
      return;
    }

    this.descargandoId = documento.id;
    this.errorDescarga = '';

    this.casosService.descargarDocumento(this.detalle.caso.id, documento.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = documento.nombreOriginal;
        enlace.click();
        URL.revokeObjectURL(url);

        this.descargandoId = null;
        this.cdr.markForCheck();
      },

      error: () => {
        this.descargandoId = null;
        this.errorDescarga = `No se pudo descargar "${documento.nombreOriginal}".`;
        this.cdr.markForCheck();
      },
    });
  }

  miniatura(documento: DocumentoResponse): string | null {
    return this.miniaturas.get(documento.id) ?? null;
  }

  iconoDocumento(documento: DocumentoResponse): LucideIconData {
    return ICONO_CATEGORIA[documento.categoria] ?? ICONOS.Paperclip;
  }

  /* CARGA LA MINIATURA DE LA IMAGEN */
  private cargarMiniaturas(): void {
    if (!this.detalle) {
      return;
    }

    const idCaso = this.detalle.caso.id;

    this.documentos
      .filter(doc => doc.categoria === 'IMAGEN' && !this.miniaturas.has(doc.id))
      .forEach(doc => {
        this.casosService.obtenerVistaPrevia(idCaso, doc.id).subscribe({
          next: (blob) => {
            this.miniaturas.set(doc.id, URL.createObjectURL(blob));
            this.cdr.markForCheck();
          },
        });
      });
  }

  /* PRESENTACIÓN */

  etiquetaEstado(estado: EstadoCaso | null): string {
    return ESTADOS_CASO.find(e => e.valor === estado)?.etiqueta ?? '';
  }

  colorSeguimiento(seguimiento: SeguimientoResponse): string {
    return COLOR_SEGUIMIENTO[seguimiento.tipo] ?? 'bg-slate-400';
  }

  cambioDeEstado(seguimiento: SeguimientoResponse): boolean {
    return !!seguimiento.estadoAnterior &&
      !!seguimiento.estadoNuevo &&
      seguimiento.estadoAnterior !== seguimiento.estadoNuevo;
  }
}

function esEvento(respuesta: CasoDetalleResponse | HttpEvent<CasoDetalleResponse>): respuesta is HttpEvent<CasoDetalleResponse> {
  return 'type' in respuesta;
}

import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';
import { ContenidoService } from '../../../core/services/contenido';
import {
  AvisoRequest,
  AvisoResponse,
  CATEGORIAS_AVISO,
  ConfiguracionPortal,
  ESTILO_CATEGORIA_AVISO,
  ServicioRequest,
  ServicioResponse,
} from '../../../core/models/contenido.models';
import { fechaHoy } from '../../../core/validators/fechas';

type Pestana = 'datos' | 'avisos' | 'servicios';

/* CONTENIDO DEL PORTAL PÚBLICO */
@Component({
  selector: 'app-contenido',
  imports: [FormsModule, DatePipe, LucideAngularModule],
  templateUrl: './contenido.html',
})
export class Contenido implements OnInit {

  readonly icono = ICONOS;
  readonly categoriasAviso = CATEGORIAS_AVISO;
  readonly estiloCategoria = ESTILO_CATEGORIA_AVISO;

  private readonly contenidoService = inject(ContenidoService);
  private readonly cdr = inject(ChangeDetectorRef);

  pestana: Pestana = 'datos';

  /* DATOS DEL RESIDENCIAL */
  configuracion: ConfiguracionPortal | null = null;
  guardandoConfiguracion = false;
  mensajeConfiguracion = '';
  errorConfiguracion = '';

  /* AVISOS */
  avisos: AvisoResponse[] = [];
  aviso: (AvisoRequest & { id: number | null }) | null = null;

  /* SERVICIOS */
  servicios: ServicioResponse[] = [];
  servicio: (ServicioRequest & { id: number | null }) | null = null;

  guardando = false;
  errorModal = '';
  errorLista = '';

  ngOnInit(): void {
    this.contenidoService.obtenerConfiguracionAdmin().subscribe({
      next: (configuracion) => { this.configuracion = configuracion; this.cdr.markForCheck(); },
      error: () => {
        this.errorConfiguracion = 'No se pudo cargar la configuración. Verifica que contenido-service esté en ejecución.';
        this.cdr.markForCheck();
      },
    });

    this.cargarAvisos();
    this.cargarServicios();
  }

  /* DATOS DEL RESIDENCIAL */

  guardarConfiguracion(): void {
    if (!this.configuracion || this.guardandoConfiguracion) {
      return;
    }

    if (!this.configuracion.nombreResidencial?.trim() || !this.configuracion.tituloBienvenida?.trim()) {
      this.errorConfiguracion = 'El nombre del residencial y el título de bienvenida son obligatorios.';
      return;
    }

    this.guardandoConfiguracion = true;
    this.errorConfiguracion = '';
    this.mensajeConfiguracion = '';

    this.contenidoService.guardarConfiguracion(this.configuracion).subscribe({
      next: (guardada) => {
        this.configuracion = guardada;
        this.mensajeConfiguracion = 'Cambios guardados. Ya se muestran en el portal público.';
        this.guardandoConfiguracion = false;
        this.cdr.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.errorConfiguracion = error.error?.mensaje ?? 'No se pudieron guardar los cambios.';
        this.guardandoConfiguracion = false;
        this.cdr.markForCheck();
      },
    });
  }

  /* AVISOS */

  nuevoAviso(): void {
    this.errorModal = '';
    this.aviso = {
      id: null, titulo: '', contenido: '', categoria: 'GENERAL',
      publicado: true, fechaPublicacion: fechaHoy(), fechaVencimiento: null,
    };
  }

  editarAviso(aviso: AvisoResponse): void {
    this.errorModal = '';
    this.aviso = {
      id: aviso.id, titulo: aviso.titulo, contenido: aviso.contenido, categoria: aviso.categoria,
      publicado: aviso.publicado, fechaPublicacion: aviso.fechaPublicacion, fechaVencimiento: aviso.fechaVencimiento,
    };
  }

  guardarAviso(): void {
    const aviso = this.aviso;

    if (!aviso || this.guardando) {
      return;
    }

    if (!aviso.titulo.trim() || !aviso.contenido.trim() || !aviso.fechaPublicacion) {
      this.errorModal = 'Título, contenido y fecha de publicación son obligatorios.';
      return;
    }

    if (aviso.fechaVencimiento && aviso.fechaVencimiento < aviso.fechaPublicacion) {
      this.errorModal = 'El vencimiento no puede ser anterior a la publicación.';
      return;
    }

    const { id, ...request } = aviso;
    request.fechaVencimiento = request.fechaVencimiento || null;

    const peticion = id === null
      ? this.contenidoService.crearAviso(request)
      : this.contenidoService.actualizarAviso(id, request);

    this.iniciarGuardado();
    peticion.subscribe({
      next: () => { this.aviso = null; this.terminarGuardado(); this.cargarAvisos(); },
      error: (error: HttpErrorResponse) => this.fallarGuardado(error),
    });
  }

  eliminarAviso(aviso: AvisoResponse): void {
    if (!confirm(`¿Eliminar el aviso "${aviso.titulo}"? Esta acción no se puede deshacer.`)) {
      return;
    }

    this.contenidoService.eliminarAviso(aviso.id).subscribe({
      next: () => this.cargarAvisos(),
      error: () => { this.errorLista = 'No se pudo eliminar el aviso.'; this.cdr.markForCheck(); },
    });
  }

  /* SERVICIOS */

  nuevoServicio(): void {
    this.errorModal = '';
    const siguienteOrden = this.servicios.reduce((max, s) => Math.max(max, s.orden), 0) + 1;
    this.servicio = { id: null, nombre: '', descripcion: '', horario: '', orden: siguienteOrden, activo: true };
  }

  editarServicio(servicio: ServicioResponse): void {
    this.errorModal = '';
    this.servicio = { ...servicio, horario: servicio.horario ?? '' };
  }

  guardarServicio(): void {
    const servicio = this.servicio;

    if (!servicio || this.guardando) {
      return;
    }

    if (!servicio.nombre.trim() || !servicio.descripcion.trim()) {
      this.errorModal = 'Nombre y descripción son obligatorios.';
      return;
    }

    const { id, ...request } = servicio;
    request.horario = request.horario?.trim() || null;

    const peticion = id === null
      ? this.contenidoService.crearServicio(request)
      : this.contenidoService.actualizarServicio(id, request);

    this.iniciarGuardado();
    peticion.subscribe({
      next: () => { this.servicio = null; this.terminarGuardado(); this.cargarServicios(); },
      error: (error: HttpErrorResponse) => this.fallarGuardado(error),
    });
  }

  eliminarServicio(servicio: ServicioResponse): void {
    if (!confirm(`¿Eliminar el servicio "${servicio.nombre}"? Si solo quieres ocultarlo, desactívalo.`)) {
      return;
    }

    this.contenidoService.eliminarServicio(servicio.id).subscribe({
      next: () => this.cargarServicios(),
      error: () => { this.errorLista = 'No se pudo eliminar el servicio.'; this.cdr.markForCheck(); },
    });
  }

  cerrarModal(): void {
    if (!this.guardando) {
      this.aviso = null;
      this.servicio = null;
    }
  }

  /* UTILIDADES */

  private cargarAvisos(): void {
    this.contenidoService.listarAvisos().subscribe({
      next: (avisos) => { this.avisos = avisos; this.cdr.markForCheck(); },
      error: () => { this.errorLista = 'No se pudieron cargar los avisos.'; this.cdr.markForCheck(); },
    });
  }

  private cargarServicios(): void {
    this.contenidoService.listarServicios().subscribe({
      next: (servicios) => { this.servicios = servicios; this.cdr.markForCheck(); },
      error: () => { this.errorLista = 'No se pudieron cargar los servicios.'; this.cdr.markForCheck(); },
    });
  }

  private iniciarGuardado(): void {
    this.guardando = true;
    this.errorModal = '';
    this.errorLista = '';
  }

  private terminarGuardado(): void {
    this.guardando = false;
    this.cdr.markForCheck();
  }

  private fallarGuardado(error: HttpErrorResponse): void {
    this.errorModal = error.error?.mensaje ?? 'No se pudo guardar.';
    this.guardando = false;
    this.cdr.markForCheck();
  }
}

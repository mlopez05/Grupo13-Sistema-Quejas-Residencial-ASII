import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../../core/iconos';
import { ReportesService, descargarArchivo } from '../../../../core/services/reportes';
import {
  FiltroAuditoria,
  FormatoExportacion,
  PaginaResponse,
  RegistroAuditoria,
  TipoAuditoria,
} from '../../../../core/models/reporte.models';
import { fechaHoy } from '../../../../core/validators/fechas';

/* REPORTE DE AUDITORÍA */
@Component({
  selector: 'app-reporte-auditoria',
  imports: [FormsModule, LucideAngularModule],
  templateUrl: './reporte-auditoria.html',
})
export class ReporteAuditoria implements OnInit {

  readonly icono = ICONOS;
  readonly hoy = fechaHoy();

  readonly tipos: { valor: TipoAuditoria; etiqueta: string }[] = [
    { valor: 'sesiones', etiqueta: 'Sesiones' },
    { valor: 'usuarios', etiqueta: 'Usuarios' },
    { valor: 'casos', etiqueta: 'Casos' },
    { valor: 'documentos', etiqueta: 'Documentos' },
  ];

  private readonly reportesService = inject(ReportesService);
  private readonly cdr = inject(ChangeDetectorRef);

  tipo: TipoAuditoria = 'sesiones';

  filtro: FiltroAuditoria = {
    desde: haceDias(30),
    hasta: fechaHoy(),
    usuario: '',
    evento: '',
    pagina: 0,
    tamano: 20,
  };

  eventos: string[] = [];
  resultado: PaginaResponse<RegistroAuditoria> | null = null;
  cargando = false;
  exportando: FormatoExportacion | null = null;
  error = '';

  ngOnInit(): void {
    this.cambiarTipo(this.tipo);
  }

  cambiarTipo(tipo: TipoAuditoria): void {
    this.tipo = tipo;
    this.filtro.evento = '';
    this.eventos = [];
    this.resultado = null;

    this.reportesService.eventosAuditoria(tipo).subscribe({
      next: (eventos) => { this.eventos = eventos; this.cdr.markForCheck(); },
    });

    this.buscar();
  }

  buscar(): void {
    this.filtro.pagina = 0;
    this.consultar();
  }

  irAPagina(pagina: number): void {
    if (!this.resultado || pagina < 0 || pagina >= this.resultado.totalPaginas) {
      return;
    }

    this.filtro.pagina = pagina;
    this.consultar();
  }

  limpiar(): void {
    this.filtro = { ...this.filtro, desde: haceDias(30), hasta: fechaHoy(), usuario: '', evento: '' };
    this.buscar();
  }

  exportar(formato: FormatoExportacion): void {
    if (!this.filtroValido() || this.exportando) {
      return;
    }

    this.exportando = formato;
    this.error = '';

    this.reportesService.exportarAuditoria(this.tipo, this.filtro, formato).subscribe({
      next: (archivo) => {
        const extension = formato === 'excel' ? 'xlsx' : 'pdf';
        descargarArchivo(archivo, `auditoria-${this.tipo}-${this.filtro.desde ?? 'inicio'}-a-${this.filtro.hasta ?? this.hoy}.${extension}`);
        this.exportando = null;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'No se pudo exportar la bitácora.';
        this.exportando = null;
        this.cdr.markForCheck();
      },
    });
  }

  /* CONVIERTE EL CÓDIGO DEL EVENTO EN TEXTO */
  etiquetaEvento(evento: string): string {
    const texto = evento.replaceAll('_', ' ').toLowerCase();
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  }

  get primerRegistro(): number {
    return this.resultado && this.resultado.totalElementos > 0
      ? this.resultado.pagina * this.resultado.tamano + 1
      : 0;
  }

  get ultimoRegistro(): number {
    return this.resultado ? this.resultado.pagina * this.resultado.tamano + this.resultado.contenido.length : 0;
  }

  private consultar(): void {
    if (!this.filtroValido()) {
      return;
    }

    this.cargando = true;
    this.error = '';

    this.reportesService.auditoria(this.tipo, this.filtro).subscribe({
      next: (resultado) => {
        this.resultado = resultado;
        this.cargando = false;
        this.cdr.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.error = error.error?.mensaje ?? 'No se pudo consultar la bitácora.';
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  private filtroValido(): boolean {
    if (this.filtro.desde && this.filtro.hasta && this.filtro.hasta < this.filtro.desde) {
      this.error = 'La fecha final no puede ser anterior a la inicial.';
      return false;
    }

    return true;
  }
}

function haceDias(dias: number): string {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() - dias);
  return `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}-${String(fecha.getDate()).padStart(2, '0')}`;
}

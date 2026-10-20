import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../../core/iconos';
import { CasosService } from '../../../../core/services/casos';
import { ReportesService, descargarArchivo } from '../../../../core/services/reportes';
import { AnalistaResponse, CategoriaResponse, ESTADOS_CASO, ESTILO_ESTADO } from '../../../../core/models/caso.models';
import {
  ConteoReporte,
  FiltroReporteCasos,
  FormatoExportacion,
  ReporteCasosResponse,
  formatearHoras,
} from '../../../../core/models/reporte.models';
import { fechaHoy } from '../../../../core/validators/fechas';

type Tendencia = ReporteCasosResponse['tendenciaMensual'][number];

/* REPORTE DE CASOS */
@Component({
  selector: 'app-reporte-casos',
  imports: [FormsModule, RouterLink, DatePipe, LucideAngularModule],
  templateUrl: './reporte-casos.html',
  styleUrl: './reporte-casos.css',
})
export class ReporteCasos implements OnInit {

  readonly icono = ICONOS;
  readonly estados = ESTADOS_CASO;
  readonly estiloEstado = ESTILO_ESTADO;
  readonly formatearHoras = formatearHoras;
  readonly hoy = fechaHoy();

  private readonly casosService = inject(CasosService);
  private readonly reportesService = inject(ReportesService);
  private readonly cdr = inject(ChangeDetectorRef);

  filtro: FiltroReporteCasos = {
    desde: `${new Date().getFullYear()}-01-01`,
    hasta: fechaHoy(),
    estado: '',
    idCategoria: undefined,
    idAnalista: undefined,
    diasEstancado: 7,
  };

  categorias: CategoriaResponse[] = [];
  analistas: AnalistaResponse[] = [];

  reporte: ReporteCasosResponse | null = null;
  cargando = false;
  exportando: FormatoExportacion | null = null;
  error = '';

  /* MES SELECCIONADO EN LA GRÁFICA */
  mesActivo: Tendencia | null = null;
  verTablaTendencia = false;

  ngOnInit(): void {
    this.casosService.obtenerCatalogo().subscribe({
      next: (categorias) => { this.categorias = categorias; this.cdr.markForCheck(); },
    });
    this.casosService.listarAnalistas().subscribe({
      next: (analistas) => { this.analistas = analistas; this.cdr.markForCheck(); },
    });

    this.generar();
  }

  generar(): void {
    if (!this.filtroValido()) {
      return;
    }

    this.cargando = true;
    this.error = '';

    this.reportesService.reporteCasos(this.filtro).subscribe({
      next: (reporte) => {
        this.reporte = reporte;
        this.cargando = false;
        this.cdr.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.error = error.error?.mensaje ?? 'No se pudo generar el reporte.';
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  exportar(formato: FormatoExportacion): void {
    if (!this.filtroValido() || this.exportando) {
      return;
    }

    this.exportando = formato;
    this.error = '';

    this.reportesService.exportarCasos(this.filtro, formato).subscribe({
      next: (archivo) => {
        const extension = formato === 'excel' ? 'xlsx' : 'pdf';
        descargarArchivo(archivo, `reporte-casos-${this.filtro.desde}-a-${this.filtro.hasta}.${extension}`);
        this.exportando = null;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'No se pudo exportar el reporte.';
        this.exportando = null;
        this.cdr.markForCheck();
      },
    });
  }

  private filtroValido(): boolean {
    if (!this.filtro.desde || !this.filtro.hasta) {
      this.error = 'Selecciona el período del reporte.';
      return false;
    }

    if (this.filtro.hasta < this.filtro.desde) {
      this.error = 'La fecha final no puede ser anterior a la inicial.';
      return false;
    }

    return true;
  }

  /* GRÁFICAS */

  maximo(conteos: ConteoReporte[]): number {
    return Math.max(1, ...conteos.map(c => c.cantidad));
  }

  /* ANCHO DE LA BARRA */
  ancho(cantidad: number, maximo: number): number {
    return cantidad === 0 ? 0 : Math.max(2, (cantidad / maximo) * 100);
  }

  get maximoTendencia(): number {
    return Math.max(1, ...(this.reporte?.tendenciaMensual ?? []).flatMap(t => [t.registrados, t.cerrados]));
  }

  alto(cantidad: number): number {
    return cantidad === 0 ? 0 : Math.max(2, (cantidad / this.maximoTendencia) * 100);
  }

  /* NOMBRE CORTO DEL MES */
  etiquetaMes(mes: string): string {
    const [anio, numero] = mes.split('-').map(Number);
    const nombre = new Date(anio, numero - 1, 1).toLocaleDateString('es', { month: 'short' }).replace('.', '');
    return `${nombre} ${String(anio).slice(2)}`;
  }

  get estadosConCasos(): ConteoReporte[] {
    return this.reporte?.porEstado ?? [];
  }
}

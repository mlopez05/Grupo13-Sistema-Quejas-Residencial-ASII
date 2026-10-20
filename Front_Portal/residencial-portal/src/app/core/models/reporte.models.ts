/* MODELOS DE REPORTES */

import { EstadoCaso } from './caso.models';

/* REPORTE DE CASOS */

export interface FiltroReporteCasos {
  desde: string;          /* FECHA */
  hasta: string;
  estado?: string;
  idCategoria?: number;
  idAnalista?: number;
  diasEstancado?: number;
}

export interface ConteoReporte {
  clave: string;
  etiqueta: string;
  cantidad: number;
}

export interface ReporteCasosResponse {
  resumen: {
    total: number;
    abiertos: number;
    finalizados: number;
    rechazados: number;
    sinAsignar: number;
    porcentajeRechazo: number | null;
  };
  porEstado: ConteoReporte[];
  porCategoria: ConteoReporte[];
  porTipoIncidente: ConteoReporte[];
  porAnalista: {
    idAnalista: number;
    nombreAnalista: string;
    asignados: number;
    enCurso: number;
    finalizados: number;
    rechazados: number;
    promedioHorasResolucion: number | null;
  }[];
  /* TIEMPOS PROMEDIO EN HORAS */
  tiempos: {
    registroAAsignacion: number | null;
    asignacionASolicitud: number | null;
    solicitudADecision: number | null;
    registroACierre: number | null;
  };
  tendenciaMensual: { mes: string; registrados: number; cerrados: number }[];
  estancados: {
    id: number;
    numeroCaso: string;
    tipoIncidente: string;
    estado: EstadoCaso;
    estadoDescripcion: string;
    nombreAnalista: string | null;
    ultimaActualizacion: string;
    diasSinMovimiento: number;
  }[];
}

/* REPORTE DE AUDITORÍA */

export type TipoAuditoria = 'sesiones' | 'usuarios' | 'casos' | 'documentos';

export interface FiltroAuditoria {
  desde?: string;
  hasta?: string;
  usuario?: string;
  evento?: string;
  pagina?: number;
  tamano?: number;
}

export interface RegistroAuditoria {
  id: number;
  fecha: string;
  hora: string;
  usuario: string;
  rol: string | null;
  evento: string;
  detalle: string;
  ip: string | null;
}

export interface PaginaResponse<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export type FormatoExportacion = 'excel' | 'pdf';

/* CONVIERTE HORAS A TEXTO */
export function formatearHoras(horas: number | null): string {
  if (horas === null || horas === undefined) {
    return '—';
  }

  return horas < 48 ? `${horas.toFixed(1)} h` : `${(horas / 24).toFixed(1)} días`;
}

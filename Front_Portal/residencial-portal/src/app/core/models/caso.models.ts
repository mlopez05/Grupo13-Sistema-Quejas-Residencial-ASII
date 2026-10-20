import { FileText, Image, LucideIconData, Music, Video } from 'lucide-angular';

/* MODELOS DE CASOS */

export type EstadoCaso =
  | 'NUEVO'
  | 'ASIGNADO'
  | 'EN_ANALISIS'
  | 'PENDIENTE_AUTORIZACION'
  | 'EN_PROCESO'
  | 'RECHAZADO'
  | 'FINALIZADO';

export type TipoSeguimiento =
  | 'REGISTRO'
  | 'ASIGNACION'
  | 'REASIGNACION'
  | 'INICIO_ANALISIS'
  | 'AVANCE'
  | 'INFORMACION_ADICIONAL'
  | 'ADJUNTO'   /* YA NO SE USA */
  | 'SOLICITUD_AUTORIZACION'
  | 'AUTORIZACION'
  | 'RECHAZO'
  | 'CIERRE';

export interface TipoIncidenteResponse {
  id: number;
  nombre: string;
}

export interface CategoriaResponse {
  id: number;
  nombre: string;
  tipos: TipoIncidenteResponse[];
}

/* DATOS PARA REGISTRAR UN CASO */
export interface RegistrarCasoRequest {
  idCategoria: number;
  idTipoIncidente: number;
  descripcion: string;
}

export interface AutorizarRequest {
  comentario: string | null;
}

export interface AsignarAnalistaRequest {
  idAnalista: number;
}

export interface CasoResponse {
  id: number;
  numeroCaso: string;
  categoria: string;
  tipoIncidente: string;
  descripcion: string;
  estado: EstadoCaso;
  estadoDescripcion: string;
  nombreResidente: string;
  correoResidente: string;
  direccionResidente: string | null;
  idAnalista: number | null;
  nombreAnalista: string | null;
  correoAnalista: string | null;
  fechaRegistro: string;
  fechaActualizacion: string;
  fechaCierre: string | null;
}

export interface SeguimientoResponse {
  id: number;
  tipo: TipoSeguimiento;
  estadoAnterior: EstadoCaso | null;
  estadoNuevo: EstadoCaso | null;
  comentario: string | null;
  nombreUsuario: string;
  rolUsuario: string;
  fecha: string;
  documentos: DocumentoResponse[];   /* ADJUNTOS DE ESTE MOVIMIENTO */
}

export type CategoriaArchivo = 'DOCUMENTO' | 'IMAGEN' | 'AUDIO' | 'VIDEO';

export interface DocumentoResponse {
  id: number;
  nombreOriginal: string;
  extension: string;
  tipoContenido: string;
  categoria: CategoriaArchivo;
  tamanoBytes: number;
  nombreUsuario: string;
  rolUsuario: string;
  fechaCarga: string;
}

export interface CasoDetalleResponse {
  caso: CasoResponse;
  seguimientos: SeguimientoResponse[];
}

export interface AnalistaResponse {
  id: number;
  nombre: string;
  apellido: string;
  correo: string;
  casosActivos: number;
}

/* PRESENTACIÓN */

export const ESTADOS_CASO: { valor: EstadoCaso; etiqueta: string }[] = [
  { valor: 'NUEVO', etiqueta: 'Nuevo' },
  { valor: 'ASIGNADO', etiqueta: 'Asignado' },
  { valor: 'EN_ANALISIS', etiqueta: 'En análisis' },
  { valor: 'PENDIENTE_AUTORIZACION', etiqueta: 'Pendiente de autorización' },
  { valor: 'EN_PROCESO', etiqueta: 'En proceso' },
  { valor: 'RECHAZADO', etiqueta: 'Rechazado' },
  { valor: 'FINALIZADO', etiqueta: 'Finalizado' },
];

/* COLORES DE CADA ESTADO */
export const ESTILO_ESTADO: Record<EstadoCaso, string> = {
  NUEVO: 'bg-sky-50 text-sky-700',
  ASIGNADO: 'bg-indigo-50 text-indigo-700',
  EN_ANALISIS: 'bg-amber-50 text-amber-700',
  PENDIENTE_AUTORIZACION: 'bg-orange-50 text-orange-700',
  EN_PROCESO: 'bg-blue-50 text-blue-700',
  RECHAZADO: 'bg-red-50 text-red-700',
  FINALIZADO: 'bg-emerald-50 text-emerald-700',
};

export const ETIQUETA_SEGUIMIENTO: Record<TipoSeguimiento, string> = {
  REGISTRO: 'Caso registrado',
  ASIGNACION: 'Caso asignado',
  REASIGNACION: 'Caso reasignado',
  INICIO_ANALISIS: 'Análisis iniciado',
  AVANCE: 'Avance registrado',
  INFORMACION_ADICIONAL: 'Información agregada por el residente',
  ADJUNTO: 'Documentos adjuntados',
  SOLICITUD_AUTORIZACION: 'Autorización solicitada',
  AUTORIZACION: 'Caso autorizado',
  RECHAZO: 'Caso rechazado',
  CIERRE: 'Caso finalizado',
};

export function esEstadoFinal(estado: EstadoCaso): boolean {
  return estado === 'RECHAZADO' || estado === 'FINALIZADO';
}

/* REGLAS DE LOS ADJUNTOS */

const MB = 1024 * 1024;

export const REGLAS_ADJUNTOS: Record<string, { categoria: CategoriaArchivo; maximoBytes: number }> = {
  pdf: { categoria: 'DOCUMENTO', maximoBytes: 10 * MB },
  docx: { categoria: 'DOCUMENTO', maximoBytes: 10 * MB },
  jpg: { categoria: 'IMAGEN', maximoBytes: 8 * MB },
  jpeg: { categoria: 'IMAGEN', maximoBytes: 8 * MB },
  png: { categoria: 'IMAGEN', maximoBytes: 8 * MB },
  mp3: { categoria: 'AUDIO', maximoBytes: 15 * MB },
  m4a: { categoria: 'AUDIO', maximoBytes: 15 * MB },
  mp4: { categoria: 'VIDEO', maximoBytes: 50 * MB },
};

export const MAXIMO_TOTAL_ADJUNTOS = 150 * MB;
export const MAXIMO_ARCHIVOS_POR_CARGA = 10;
export const EXTENSIONES_ACEPTADAS = Object.keys(REGLAS_ADJUNTOS).map(ext => '.' + ext).join(',');

/* ÍCONOS POR TIPO DE ADJUNTO */
export const ICONO_CATEGORIA: Record<CategoriaArchivo, LucideIconData> = {
  DOCUMENTO: FileText,
  IMAGEN: Image,
  AUDIO: Music,
  VIDEO: Video,
};

export function extensionDe(nombre: string): string {
  const punto = nombre.lastIndexOf('.');
  return punto >= 0 ? nombre.substring(punto + 1).toLowerCase() : '';
}

export function formatearTamano(bytes: number): string {
  if (bytes < 1024) {
    return `${bytes} B`;
  }

  if (bytes < MB) {
    return `${(bytes / 1024).toFixed(0)} KB`;
  }

  return `${(bytes / MB).toFixed(1)} MB`;
}

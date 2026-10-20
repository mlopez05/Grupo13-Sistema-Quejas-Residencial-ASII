/* MODELOS DEL CONTENIDO DEL PORTAL */

export interface ConfiguracionPortal {
  nombreResidencial: string;
  lema: string | null;

  razonSocial: string | null;
  nit: string | null;
  direccion: string | null;
  telefono: string | null;
  correoContacto: string | null;
  horarioAtencion: string | null;

  tituloBienvenida: string;
  mensajeBienvenida: string | null;
  descripcionComunidad: string | null;
  mision: string | null;
  vision: string | null;
}

export type CategoriaAviso = 'GENERAL' | 'IMPORTANTE' | 'MANTENIMIENTO' | 'ACTIVIDAD' | 'COMUNIDAD';

export interface AvisoResponse {
  id: number;
  titulo: string;
  contenido: string;
  categoria: CategoriaAviso;
  categoriaEtiqueta: string;
  publicado: boolean;
  fechaPublicacion: string;
  fechaVencimiento: string | null;
  vigente: boolean;
  fechaActualizacion: string;
  actualizadoPor: string | null;
}

export interface AvisoRequest {
  titulo: string;
  contenido: string;
  categoria: CategoriaAviso;
  publicado: boolean;
  fechaPublicacion: string;
  fechaVencimiento: string | null;
}

export interface ServicioResponse {
  id: number;
  nombre: string;
  descripcion: string;
  horario: string | null;
  orden: number;
  activo: boolean;
}

export interface ServicioRequest {
  nombre: string;
  descripcion: string;
  horario: string | null;
  orden: number;
  activo: boolean;
}

export const CATEGORIAS_AVISO: { valor: CategoriaAviso; etiqueta: string }[] = [
  { valor: 'GENERAL', etiqueta: 'General' },
  { valor: 'IMPORTANTE', etiqueta: 'Importante' },
  { valor: 'MANTENIMIENTO', etiqueta: 'Mantenimiento' },
  { valor: 'ACTIVIDAD', etiqueta: 'Actividad' },
  { valor: 'COMUNIDAD', etiqueta: 'Comunidad' },
];

export const ESTILO_CATEGORIA_AVISO: Record<CategoriaAviso, string> = {
  GENERAL: 'bg-slate-100 text-slate-700',
  IMPORTANTE: 'bg-red-50 text-red-700',
  MANTENIMIENTO: 'bg-amber-50 text-amber-700',
  ACTIVIDAD: 'bg-violet-50 text-violet-700',
  COMUNIDAD: 'bg-emerald-50 text-emerald-700',
};

/* CATÁLOGO DE CATEGORÍAS Y TIPOS */
export interface CategoriaAdmin {
  id: number;
  nombre: string;
  activo: boolean;
  casos: number;
  tipos: { id: number; nombre: string; activo: boolean; casos: number }[];
}

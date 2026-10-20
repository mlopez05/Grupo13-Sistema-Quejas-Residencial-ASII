/* ALERTA INTERNA */
export interface NotificacionResponse {
  id: number;
  tipo: string;
  titulo: string;
  mensaje: string;
  idCaso: number | null;
  numeroCaso: string | null;
  leida: boolean;
  fecha: string;
}

/* TIPO DE RESALTADO DE UN CASO */
export type NovedadCaso = 'nuevo' | 'actualizado';

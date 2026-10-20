export type EstadoUsuario = 'Activo' | 'Inactivo';

/* DATOS DEL USUARIO */
export interface UsuarioResponse {
  id: number;
  nombre: string;
  apellido: string;
  correo: string;
  fechaNacimiento: string | null;
  dpi: string;
  telefono: string | null;
  direccion: string | null;
  rol: string;
  estado: EstadoUsuario;
}

/* DATOS PARA ACTUALIZAR UN USUARIO */
export interface ActualizarUsuarioRequest {
  nombreUsuario?: string;
  apellidoUsuario?: string;
  fechaNacimiento?: string;
  dpi?: string;
  direccion?: string;
  telefono?: string;
  estado?: EstadoUsuario;
}

/* DATOS PARA ACTUALIZAR EL PERFIL */
export interface ActualizarPerfilRequest {
  nombreUsuario?: string;
  apellidoUsuario?: string;
  fechaNacimiento?: string;
  direccion?: string;
  telefono?: string;
}

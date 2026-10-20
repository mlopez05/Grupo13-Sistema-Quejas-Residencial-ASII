export interface RegistroRequest {
  nombre: string;
  apellido: string;
  correo: string;
  clave: string;
  fecha_nacimiento: string;
  dpi: string;
  telefono: string;
  direccion: string;
}

export interface RegistroRequestPortal {
  nombre: string;
  apellido: string;
  correo: string;
  clave: string;
  fecha_nacimiento: string;
  dpi: string;
  telefono: string;
  id_rol: number;
  direccion: string;
}

export interface LoginRequest {
  usuario: string;
  clave: string;
}

export interface LoginResponse {
  token: string;
  usuario: string;
  rol: string;
  debeCambiarClave: boolean;   /* CONTRASEÑA TEMPORAL */
}

export interface RegistroResponse {
  nombre: string;
  apellido: string;
  usuario: string;
}

export interface ErrorResponse {
  status: number;
  mensaje: string;
  timestamp: string;
}

export interface AuthSession {
  token: string;
  usuario: string;
  rol: string;
  /* INDICA SI DEBE CAMBIAR SU CONTRASEÑA TEMPORAL */
  debeCambiarClave?: boolean;
}

export interface RecuperacionRequest{
  usuario: string;
}


/* DATOS PARA CAMBIAR LA CONTRASEÑA */
export interface CambiarClaveRequest {
  claveActual: string;
  claveNueva: string;
}

export interface ResetPasswordRequest {
  token: string;
  nuevaPassword: string;
}

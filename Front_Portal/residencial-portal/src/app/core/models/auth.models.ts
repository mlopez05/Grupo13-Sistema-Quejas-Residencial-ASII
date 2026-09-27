export interface RegistroRequest {
  nombre: string;
  apellido: string;
  correo: string;
  clave: string;
  fecha_nacimiento: string;
  dpi: string;
  direccion: string;
}

export interface RegistroRequestPortal {
  nombre: string;
  apellido: string;
  correo: string;
  clave: string;
  fecha_nacimiento: string;
  dpi: string;
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
}
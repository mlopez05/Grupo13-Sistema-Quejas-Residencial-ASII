import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ActualizarPerfilRequest, ActualizarUsuarioRequest, UsuarioResponse } from '../models/usuario.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.authUrl}/api/registro`;

  listarUsuarios(): Observable<UsuarioResponse[]> {
    return this.http.get<UsuarioResponse[]>(`${this.apiUrl}/usuarios`);
  }

  obtenerUsuario(id: number): Observable<UsuarioResponse> {
    return this.http.get<UsuarioResponse>(`${this.apiUrl}/usuarios/${id}`);
  }

  actualizarUsuario(
    id: number,
    request: ActualizarUsuarioRequest
  ): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/actualizar/${id}`, request);
  }

  /* PERFIL DEL USUARIO */
  obtenerPerfil(): Observable<UsuarioResponse> {
    return this.http.get<UsuarioResponse>(`${this.apiUrl}/perfil`);
  }

  actualizarPerfil(request: ActualizarPerfilRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/perfil`, request);
  }
}

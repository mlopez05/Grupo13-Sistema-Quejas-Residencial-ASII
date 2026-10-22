import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  AvisoRequest,
  AvisoResponse,
  CategoriaAdmin,
  ConfiguracionPortal,
  ServicioRequest,
  ServicioResponse,
} from '../models/contenido.models';
import { environment } from '../../../environments/environment';

/* CONTENIDO DEL PORTAL Y CATÁLOGO */
@Injectable({
  providedIn: 'root',
})
export class ContenidoService {
  private readonly http = inject(HttpClient);

  private readonly publicoUrl = `${environment.contenidoUrl}/api/portal/publico`;
  private readonly adminUrl = `${environment.contenidoUrl}/api/portal/admin`;
  private readonly catalogoUrl = `${environment.casosUrl}/api/casos/catalogo/admin`;

  /* DATOS DEL RESIDENCIAL */
  readonly configuracion = signal<ConfiguracionPortal | null>(null);
  private cargada = false;

  cargarConfiguracion(): void {
    if (this.cargada) {
      return;
    }

    this.cargada = true;
    this.http.get<ConfiguracionPortal>(`${this.publicoUrl}/configuracion`).subscribe({
      next: (configuracion) => this.configuracion.set(configuracion),
      error: () => {
        this.cargada = false; /* PERMITE VOLVER A INTENTAR */
      },
    });
  }

  /* PORTAL PÚBLICO */

  avisosVigentes(limite?: number): Observable<AvisoResponse[]> {
    const params: Record<string, string> = limite ? { limite: String(limite) } : {};
    return this.http.get<AvisoResponse[]>(`${this.publicoUrl}/avisos`, { params });
  }

  serviciosActivos(): Observable<ServicioResponse[]> {
    return this.http.get<ServicioResponse[]>(`${this.publicoUrl}/servicios`);
  }

  /* DATOS DEL RESIDENCIAL */

  obtenerConfiguracionAdmin(): Observable<ConfiguracionPortal> {
    return this.http.get<ConfiguracionPortal>(`${this.adminUrl}/configuracion`);
  }

  guardarConfiguracion(configuracion: ConfiguracionPortal): Observable<ConfiguracionPortal> {
    return this.http.put<ConfiguracionPortal>(`${this.adminUrl}/configuracion`, configuracion).pipe(
      tap((guardada) => this.configuracion.set(guardada)),
    );
  }

  /* AVISOS */

  listarAvisos(): Observable<AvisoResponse[]> {
    return this.http.get<AvisoResponse[]>(`${this.adminUrl}/avisos`);
  }

  crearAviso(request: AvisoRequest): Observable<AvisoResponse> {
    return this.http.post<AvisoResponse>(`${this.adminUrl}/avisos`, request);
  }

  actualizarAviso(id: number, request: AvisoRequest): Observable<AvisoResponse> {
    return this.http.put<AvisoResponse>(`${this.adminUrl}/avisos/${id}`, request);
  }

  eliminarAviso(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/avisos/${id}`);
  }

  /* SERVICIOS */

  listarServicios(): Observable<ServicioResponse[]> {
    return this.http.get<ServicioResponse[]>(`${this.adminUrl}/servicios`);
  }

  crearServicio(request: ServicioRequest): Observable<ServicioResponse> {
    return this.http.post<ServicioResponse>(`${this.adminUrl}/servicios`, request);
  }

  actualizarServicio(id: number, request: ServicioRequest): Observable<ServicioResponse> {
    return this.http.put<ServicioResponse>(`${this.adminUrl}/servicios/${id}`, request);
  }

  eliminarServicio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminUrl}/servicios/${id}`);
  }

  /* CATÁLOGO DE CATEGORÍAS Y TIPOS */

  listarCatalogo(): Observable<CategoriaAdmin[]> {
    return this.http.get<CategoriaAdmin[]>(this.catalogoUrl);
  }

  crearCategoria(nombre: string, activo: boolean): Observable<CategoriaAdmin[]> {
    return this.http.post<CategoriaAdmin[]>(`${this.catalogoUrl}/categorias`, { nombre, activo });
  }

  actualizarCategoria(id: number, nombre: string, activo: boolean): Observable<CategoriaAdmin[]> {
    return this.http.put<CategoriaAdmin[]>(`${this.catalogoUrl}/categorias/${id}`, { nombre, activo });
  }

  crearTipo(idCategoria: number, nombre: string, activo: boolean): Observable<CategoriaAdmin[]> {
    return this.http.post<CategoriaAdmin[]>(`${this.catalogoUrl}/tipos`, { idCategoria, nombre, activo });
  }

  actualizarTipo(id: number, idCategoria: number, nombre: string, activo: boolean): Observable<CategoriaAdmin[]> {
    return this.http.put<CategoriaAdmin[]>(`${this.catalogoUrl}/tipos/${id}`, { idCategoria, nombre, activo });
  }

  eliminarCategoria(id: number): Observable<CategoriaAdmin[]> {
    return this.http.delete<CategoriaAdmin[]>(`${this.catalogoUrl}/categorias/${id}`);
  }

  eliminarTipo(id: number): Observable<CategoriaAdmin[]> {
    return this.http.delete<CategoriaAdmin[]>(`${this.catalogoUrl}/tipos/${id}`);
  }
}

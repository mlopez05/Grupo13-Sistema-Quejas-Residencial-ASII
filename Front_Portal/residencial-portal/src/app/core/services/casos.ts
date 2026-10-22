import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AnalistaResponse,
  CasoDetalleResponse,
  CasoResponse,
  CategoriaResponse,
  RegistrarCasoRequest,
} from '../models/caso.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class CasosService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.casosUrl}/api/casos`;

  obtenerCatalogo(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>(`${this.apiUrl}/catalogo`);
  }

  /* RESIDENTE */

  /* REGISTRA EL CASO CON SUS ADJUNTOS */
  registrarCaso(request: RegistrarCasoRequest, archivos: File[]): Observable<HttpEvent<CasoResponse>> {
    const formData = new FormData();
    formData.append('idCategoria', String(request.idCategoria));
    formData.append('idTipoIncidente', String(request.idTipoIncidente));
    formData.append('descripcion', request.descripcion);
    archivos.forEach(archivo => formData.append('archivos', archivo, archivo.name));

    return this.http.post<CasoResponse>(this.apiUrl, formData, {
      reportProgress: true,
      observe: 'events',
    });
  }

  /* EL RESIDENTE AGREGA INFORMACIÓN */
  agregarInformacion(id: number, comentario: string, archivos: File[]): Observable<HttpEvent<CasoDetalleResponse>> {
    return this.http.post<CasoDetalleResponse>(`${this.apiUrl}/${id}/informacion`,
      this.formularioAccion(comentario, archivos), { reportProgress: true, observe: 'events' });
  }

  listarMisCasos(): Observable<CasoResponse[]> {
    return this.http.get<CasoResponse[]>(`${this.apiUrl}/mis-casos`);
  }

  /* PERSONAL */

  listarBandeja(): Observable<CasoResponse[]> {
    return this.http.get<CasoResponse[]>(`${this.apiUrl}/bandeja`);
  }

  listarAnalistas(): Observable<AnalistaResponse[]> {
    return this.http.get<AnalistaResponse[]>(`${this.apiUrl}/analistas`);
  }

  obtenerDetalle(id: number): Observable<CasoDetalleResponse> {
    return this.http.get<CasoDetalleResponse>(`${this.apiUrl}/${id}`);
  }

  iniciarAnalisis(id: number): Observable<CasoDetalleResponse> {
    return this.http.put<CasoDetalleResponse>(`${this.apiUrl}/${id}/iniciar-analisis`, {});
  }

  /* ACCIONES DEL ANALISTA */

  registrarAvance(id: number, comentario: string, archivos: File[]): Observable<HttpEvent<CasoDetalleResponse>> {
    return this.http.post<CasoDetalleResponse>(`${this.apiUrl}/${id}/avances`,
      this.formularioAccion(comentario, archivos), { reportProgress: true, observe: 'events' });
  }

  solicitarAutorizacion(id: number, comentario: string, archivos: File[]): Observable<HttpEvent<CasoDetalleResponse>> {
    return this.http.put<CasoDetalleResponse>(`${this.apiUrl}/${id}/solicitar-autorizacion`,
      this.formularioAccion(comentario, archivos), { reportProgress: true, observe: 'events' });
  }

  cerrar(id: number, comentario: string, archivos: File[]): Observable<HttpEvent<CasoDetalleResponse>> {
    return this.http.put<CasoDetalleResponse>(`${this.apiUrl}/${id}/cerrar`,
      this.formularioAccion(comentario, archivos), { reportProgress: true, observe: 'events' });
  }

  /* ACCIONES DEL SUPERVISOR */

  autorizar(id: number, comentario: string | null): Observable<CasoDetalleResponse> {
    return this.http.put<CasoDetalleResponse>(`${this.apiUrl}/${id}/autorizar`, { comentario });
  }

  rechazar(id: number, comentario: string): Observable<CasoDetalleResponse> {
    return this.http.put<CasoDetalleResponse>(`${this.apiUrl}/${id}/rechazar`, { comentario });
  }

  asignarAnalista(id: number, idAnalista: number): Observable<CasoDetalleResponse> {
    return this.http.put<CasoDetalleResponse>(`${this.apiUrl}/${id}/asignar`, { idAnalista });
  }

  /* ADJUNTOS */

  /* DESCARGA UN ADJUNTO */
  descargarDocumento(idCaso: number, idDocumento: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${idCaso}/documentos/${idDocumento}/descarga`, {
      responseType: 'blob',
    });
  }

  /* MINIATURA DE UNA IMAGEN */
  obtenerVistaPrevia(idCaso: number, idDocumento: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${idCaso}/documentos/${idDocumento}/vista`, {
      responseType: 'blob',
    });
  }

  private formularioAccion(comentario: string, archivos: File[]): FormData {
    const formData = new FormData();
    formData.append('comentario', comentario);
    archivos.forEach(archivo => formData.append('archivos', archivo, archivo.name));
    return formData;
  }
}

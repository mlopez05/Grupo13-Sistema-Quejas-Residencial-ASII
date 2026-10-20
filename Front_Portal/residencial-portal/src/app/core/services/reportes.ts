import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  FiltroAuditoria,
  FiltroReporteCasos,
  FormatoExportacion,
  PaginaResponse,
  RegistroAuditoria,
  ReporteCasosResponse,
  TipoAuditoria,
} from '../models/reporte.models';

@Injectable({
  providedIn: 'root',
})
export class ReportesService {
  private readonly http = inject(HttpClient);

  private readonly casosUrl = 'http://localhost:8081/api/casos/reportes';
  private readonly bitacoraUrl = 'http://localhost:8085/bitacora/reportes';

  /* REPORTE DE CASOS */

  reporteCasos(filtro: FiltroReporteCasos): Observable<ReporteCasosResponse> {
    return this.http.get<ReporteCasosResponse>(this.casosUrl, { params: parametros(filtro) });
  }

  exportarCasos(filtro: FiltroReporteCasos, formato: FormatoExportacion): Observable<Blob> {
    return this.http.get(`${this.casosUrl}/exportar`, {
      params: parametros({ ...filtro, formato }),
      responseType: 'blob',
    });
  }

  /* REPORTE DE AUDITORÍA */

  auditoria(tipo: TipoAuditoria, filtro: FiltroAuditoria): Observable<PaginaResponse<RegistroAuditoria>> {
    return this.http.get<PaginaResponse<RegistroAuditoria>>(`${this.bitacoraUrl}/${tipo}`, {
      params: parametros(filtro),
    });
  }

  eventosAuditoria(tipo: TipoAuditoria): Observable<string[]> {
    return this.http.get<string[]>(`${this.bitacoraUrl}/${tipo}/eventos`);
  }

  exportarAuditoria(tipo: TipoAuditoria, filtro: FiltroAuditoria, formato: FormatoExportacion): Observable<Blob> {
    return this.http.get(`${this.bitacoraUrl}/${tipo}/exportar`, {
      params: parametros({ ...filtro, formato }),
      responseType: 'blob',
    });
  }
}

/* ENVÍA SOLO LOS FILTROS CON VALOR */
function parametros(filtro: object): HttpParams {
  let params = new HttpParams();

  for (const [clave, valor] of Object.entries(filtro)) {
    if (valor !== null && valor !== undefined && valor !== '') {
      params = params.set(clave, String(valor));
    }
  }

  return params;
}

/* DESCARGA UN ARCHIVO */
export function descargarArchivo(blob: Blob, nombre: string): void {
  const url = URL.createObjectURL(blob);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombre;
  enlace.click();
  URL.revokeObjectURL(url);
}

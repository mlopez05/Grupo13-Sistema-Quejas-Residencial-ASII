import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subject, forkJoin } from 'rxjs';
import { Client } from '@stomp/stompjs';
import { SessionService } from './session';
import { NotificacionResponse, NovedadCaso } from '../models/notificacion.models';

/* ALERTAS INTERNAS DEL PORTAL */
@Injectable({
  providedIn: 'root',
})
export class NotificacionesService {
  private readonly http = inject(HttpClient);
  private readonly sessionService = inject(SessionService);

  private readonly apiUrl = 'http://localhost:8084/notificaciones';
  private readonly wsUrl = 'ws://localhost:8084/ws';

  private cliente: Client | null = null;

  readonly recientes = signal<NotificacionResponse[]>([]);
  readonly noLeidas = signal<NotificacionResponse[]>([]);
  readonly totalNoLeidas = computed(() => this.noLeidas().length);

  /* CASOS CON NOVEDADES */
  readonly novedadesPorCaso = computed(() => {
    const novedades = new Map<number, NovedadCaso>();

    for (const notificacion of this.noLeidas()) {
      if (notificacion.idCaso === null) {
        continue;
      }

      const novedad: NovedadCaso = notificacion.tipo === 'CASO_ASIGNADO' ? 'nuevo' : 'actualizado';

      if (novedades.get(notificacion.idCaso) !== 'nuevo') {
        novedades.set(notificacion.idCaso, novedad);
      }
    }

    return novedades;
  });

  /* AVISA CUANDO LLEGA UNA ALERTA */
  private readonly llegadas = new Subject<NotificacionResponse>();
  readonly nuevas$ = this.llegadas.asObservable();

  iniciar(): void {
    const sesion = this.sessionService.obtenerSesion();

    if (this.cliente || !sesion) {
      return;
    }

    this.cliente = new Client({
      brokerURL: this.wsUrl,
      connectHeaders: { Authorization: `Bearer ${sesion.token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        this.cliente?.subscribe('/user/queue/notificaciones', (mensaje) => {
          this.recibir(JSON.parse(mensaje.body) as NotificacionResponse);
        });

        /* CARGA LAS ALERTAS AL CONECTARSE */
        this.cargar();
      },
    });

    this.cliente.activate();
  }

  detener(): void {
    this.cliente?.deactivate();
    this.cliente = null;
    this.recientes.set([]);
    this.noLeidas.set([]);
  }

  novedadDe(idCaso: number): NovedadCaso | null {
    return this.novedadesPorCaso().get(idCaso) ?? null;
  }

  /* MARCA COMO LEÍDAS LAS ALERTAS DEL CASO */
  marcarCasoLeido(idCaso: number): void {
    if (!this.noLeidas().some(n => n.idCaso === idCaso)) {
      return;
    }

    this.noLeidas.update(lista => lista.filter(n => n.idCaso !== idCaso));
    this.recientes.update(lista => lista.map(n => n.idCaso === idCaso ? { ...n, leida: true } : n));

    this.http.put<void>(`${this.apiUrl}/caso/${idCaso}/leidas`, {}).subscribe();
  }

  marcarLeida(notificacion: NotificacionResponse): void {
    if (notificacion.leida) {
      return;
    }

    this.noLeidas.update(lista => lista.filter(n => n.id !== notificacion.id));
    this.recientes.update(lista => lista.map(n => n.id === notificacion.id ? { ...n, leida: true } : n));

    this.http.put<void>(`${this.apiUrl}/${notificacion.id}/leida`, {}).subscribe();
  }

  marcarTodasLeidas(): void {
    if (this.totalNoLeidas() === 0) {
      return;
    }

    this.noLeidas.set([]);
    this.recientes.update(lista => lista.map(n => ({ ...n, leida: true })));

    this.http.put<void>(`${this.apiUrl}/leidas`, {}).subscribe();
  }

  private cargar(): void {
    forkJoin({
      recientes: this.http.get<NotificacionResponse[]>(this.apiUrl),
      noLeidas: this.http.get<NotificacionResponse[]>(`${this.apiUrl}/no-leidas`),
    }).subscribe({
      next: ({ recientes, noLeidas }) => {
        this.recientes.set(recientes);
        this.noLeidas.set(noLeidas);
      },
      error: () => {
        /* SI EL SERVICIO NO RESPONDE, EL PORTAL SIGUE FUNCIONANDO */
      },
    });
  }

  private recibir(notificacion: NotificacionResponse): void {
    this.recientes.update(lista => [notificacion, ...lista].slice(0, 30));
    this.noLeidas.update(lista => [notificacion, ...lista]);
    this.llegadas.next(notificacion);
  }
}

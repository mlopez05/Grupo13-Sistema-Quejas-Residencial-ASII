import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { UsuarioService } from '../../../../core/services/usuario';
import { SessionService } from '../../../../core/services/session';
import { EstadoUsuario, UsuarioResponse } from '../../../../core/models/usuario.models';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../../core/iconos';

@Component({
  selector: 'app-usuarios',
  imports: [FormsModule, RouterLink, LucideAngularModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {

  readonly icono = ICONOS;

  private readonly usuarioService = inject(UsuarioService);
  private readonly sessionService = inject(SessionService);
  private readonly cdr = inject(ChangeDetectorRef);

  private readonly correoSesion =
    this.sessionService.obtenerSesion()?.usuario ?? '';

  readonly roles = [
    'Analista',
    'Supervisor',
    'Administrador',
    'Residente'
  ];

  usuarios: UsuarioResponse[] = [];
  cargando = true;
  errorCarga = '';

  terminoBusqueda = '';
  rolSeleccionado = '';
  estadoSeleccionado = '';

  usuarioPorCambiarEstado: UsuarioResponse | null = null;
  cambiandoEstado = false;
  mensajeExito = '';
  mensajeError = '';

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.cargando = true;
    this.errorCarga = '';

    this.usuarioService.listarUsuarios().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        this.cargando = false;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.errorCarga =
          error.error?.mensaje ??
          'No se pudo cargar la lista de usuarios.';
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  get usuariosFiltrados(): UsuarioResponse[] {
    const termino = this.terminoBusqueda.toLowerCase().trim();

    return this.usuarios.filter(usuario => {

      const coincideBusqueda =
        !termino ||
        this.nombreCompleto(usuario).toLowerCase().includes(termino) ||
        usuario.correo.toLowerCase().includes(termino);

      const coincideRol =
        !this.rolSeleccionado ||
        usuario.rol === this.rolSeleccionado;

      const coincideEstado =
        !this.estadoSeleccionado ||
        usuario.estado === this.estadoSeleccionado;

      return coincideBusqueda && coincideRol && coincideEstado;
    });
  }

  nombreCompleto(usuario: UsuarioResponse): string {
    return `${usuario.nombre ?? ''} ${usuario.apellido ?? ''}`.trim();
  }

  inicial(usuario: UsuarioResponse): string {
    return this.nombreCompleto(usuario).charAt(0).toUpperCase() || '?';
  }

  estaActivo(usuario: UsuarioResponse): boolean {
    return usuario.estado === 'Activo';
  }

  /* EL ADMINISTRADOR NO PUEDE INACTIVARSE A SÍ MISMO */
  esUsuarioActual(usuario: UsuarioResponse): boolean {
    return usuario.correo === this.correoSesion;
  }

  solicitarCambioEstado(usuario: UsuarioResponse): void {
    this.usuarioPorCambiarEstado = usuario;
  }

  cancelarCambioEstado(): void {
    if (this.cambiandoEstado) {
      return;
    }

    this.usuarioPorCambiarEstado = null;
  }

  confirmarCambioEstado(): void {
    const usuario = this.usuarioPorCambiarEstado;

    if (!usuario || this.cambiandoEstado) {
      return;
    }

    const nuevoEstado: EstadoUsuario =
      this.estaActivo(usuario) ? 'Inactivo' : 'Activo';

    this.cambiandoEstado = true;

    this.usuarioService
      .actualizarUsuario(usuario.id, { estado: nuevoEstado })
      .subscribe({
        next: () => {
          usuario.estado = nuevoEstado;
          this.cambiandoEstado = false;
          this.usuarioPorCambiarEstado = null;
          this.mensajeExito =
            `El usuario ${this.nombreCompleto(usuario)} fue ` +
            `${nuevoEstado === 'Activo' ? 'activado' : 'inactivado'} correctamente.`;
          this.cdr.markForCheck();
        },

        error: (error: HttpErrorResponse) => {
          this.cambiandoEstado = false;
          this.usuarioPorCambiarEstado = null;
          this.mensajeError =
            error.error?.mensaje ??
            'Ocurrió un error al cambiar el estado del usuario.';
          this.cdr.markForCheck();
        },
      });
  }

  cerrarModalExito(): void {
    this.mensajeExito = '';
  }

  cerrarModalError(): void {
    this.mensajeError = '';
  }
}

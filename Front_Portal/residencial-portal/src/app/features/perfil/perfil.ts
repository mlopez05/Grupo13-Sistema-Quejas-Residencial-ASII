import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { LucideAngularModule } from 'lucide-angular';
import { UsuarioService } from '../../core/services/usuario';
import { AuthService } from '../../core/services/auth';
import { ActualizarPerfilRequest, UsuarioResponse } from '../../core/models/usuario.models';
import { ICONOS } from '../../core/iconos';
import { LONGITUD_MINIMA_CLAVE, PATRON_CLAVE, validarCambioClave } from '../../core/validators/claves';
import { fechaMaximaNacimiento, fechaNoFuturaValidator, mayorDeEdadValidator } from '../../core/validators/fechas';

/* NO PERMITE TEXTO VACÍO */
const NO_SOLO_ESPACIOS = /\S/;

const SEGUNDOS_CIERRE_SESION = 5;

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, LucideAngularModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil implements OnInit, OnDestroy {

  private readonly formBuilder = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  usuario: UsuarioResponse | null = null;
  cargando = true;
  errorCarga = '';

  submitted = false;
  guardando = false;
  sinCambios = false;
  actualizacionExitosa = false;
  mensajeError = '';

  readonly icono = ICONOS;

  readonly fechaMaxima = fechaMaximaNacimiento();

  cambiandoClave = false;
  guardandoClave = false;
  claveSubmitted = false;
  errorClave = '';
  claveCambiada = false;
  segundosParaCerrar = SEGUNDOS_CIERRE_SESION;
  mostrarClaveActual = false;
  mostrarClaveNueva = false;
  mostrarConfirmacion = false;
  private temporizador: ReturnType<typeof setInterval> | null = null;

  readonly claveForm = this.formBuilder.nonNullable.group({
    claveActual: ['', [Validators.required]],
    claveNueva: ['', [
      Validators.required,
      Validators.minLength(LONGITUD_MINIMA_CLAVE),
      Validators.pattern(PATRON_CLAVE),
    ]],
    confirmarClave: ['', [Validators.required]],
  }, { validators: validarCambioClave });

  readonly perfilForm = this.formBuilder.nonNullable.group({
    nombre: ['', [Validators.required, Validators.pattern(NO_SOLO_ESPACIOS)]],
    apellido: ['', [Validators.required, Validators.pattern(NO_SOLO_ESPACIOS)]],
    fechaNacimiento: ['', [mayorDeEdadValidator, fechaNoFuturaValidator]],
    telefono: ['', [Validators.pattern(/^[0-9]{8}$/)]],
    direccion: ['', [Validators.pattern(NO_SOLO_ESPACIOS)]],
  });

  ngOnInit(): void {
    this.cargarPerfil();
  }

  cargarPerfil(): void {
    this.cargando = true;
    this.errorCarga = '';

    this.usuarioService.obtenerPerfil().subscribe({
      next: (usuario) => {
        this.usuario = usuario;
        this.cargarFormulario(usuario);
        this.cargando = false;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.errorCarga =
          error.error?.mensaje ??
          'No se pudo cargar la información de tu perfil.';
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  onSubmit(): void {
    this.submitted = true;
    this.sinCambios = false;
    this.mensajeError = '';

    if (!this.usuario || this.guardando) {
      return;
    }

    if (this.perfilForm.invalid) {
      this.perfilForm.markAllAsTouched();
      return;
    }

    const cambios = this.construirCambios(this.usuario);

    if (Object.keys(cambios).length === 0) {
      this.sinCambios = true;
      return;
    }

    this.guardando = true;

    this.usuarioService.actualizarPerfil(cambios).subscribe({
      next: () => {
        this.guardando = false;
        this.submitted = false;
        this.actualizacionExitosa = true;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.guardando = false;
        this.mensajeError =
          error.error?.mensaje ??
          'Ocurrió un error al actualizar tu perfil.';
        this.cdr.markForCheck();
      },
    });
  }

  /* DESCARTA LOS CAMBIOS */
  descartarCambios(): void {
    if (this.usuario) {
      this.submitted = false;
      this.sinCambios = false;
      this.cargarFormulario(this.usuario);
    }
  }

  /* CAMBIO DE CONTRASEÑA */

  abrirCambioClave(): void {
    this.claveForm.reset();
    this.claveSubmitted = false;
    this.errorClave = '';
    this.mostrarClaveActual = false;
    this.mostrarClaveNueva = false;
    this.mostrarConfirmacion = false;
    this.cambiandoClave = true;
  }

  cancelarCambioClave(): void {
    if (!this.guardandoClave) {
      this.cambiandoClave = false;
    }
  }

  guardarClave(): void {
    this.claveSubmitted = true;
    this.errorClave = '';

    if (this.claveForm.invalid || this.guardandoClave) {
      this.claveForm.markAllAsTouched();
      return;
    }

    const { claveActual, claveNueva } = this.claveForm.getRawValue();

    this.guardandoClave = true;

    this.authService.cambiarClave({ claveActual, claveNueva }).subscribe({
      next: () => {
        this.guardandoClave = false;
        this.cambiandoClave = false;
        this.claveForm.reset();
        this.claveCambiada = true;
        this.iniciarCuentaRegresiva();
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.guardandoClave = false;
        this.errorClave =
          error.error?.mensaje ??
          'No se pudo cambiar la contraseña.';
        this.cdr.markForCheck();
      },
    });
  }

  /* CIERRA LA SESIÓN DESPUÉS DE CAMBIAR LA CONTRASEÑA */
  cerrarSesionAhora(): void {
    if (this.temporizador) {
      clearInterval(this.temporizador);
      this.temporizador = null;
    }

    this.authService.cerrarSesion();
  }

  isClaveInvalida(controlName: keyof typeof this.claveForm.controls): boolean {
    const control = this.claveForm.controls[controlName];

    return control.invalid && (control.touched || this.claveSubmitted);
  }

  private iniciarCuentaRegresiva(): void {
    this.segundosParaCerrar = SEGUNDOS_CIERRE_SESION;

    this.temporizador = setInterval(() => {
      this.segundosParaCerrar--;
      this.cdr.markForCheck();

      if (this.segundosParaCerrar <= 0) {
        this.cerrarSesionAhora();
      }
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.temporizador) {
      clearInterval(this.temporizador);
    }
  }

  onTelefonoInput(event: Event): void {
    const input = event.target as HTMLInputElement;

    const numericValue = input.value
      .replace(/\D/g, '')
      .slice(0, 8);

    this.perfilForm.controls.telefono.setValue(numericValue);
  }

  isInvalid(controlName: keyof typeof this.perfilForm.controls): boolean {
    const control = this.perfilForm.controls[controlName];

    return control.invalid && (control.touched || this.submitted);
  }

  cerrarModalExito(): void {
    this.actualizacionExitosa = false;
    this.cargarPerfil();
  }

  cerrarModalError(): void {
    this.mensajeError = '';
  }

  private cargarFormulario(usuario: UsuarioResponse): void {
    this.perfilForm.reset({
      nombre: usuario.nombre ?? '',
      apellido: usuario.apellido ?? '',
      fechaNacimiento: usuario.fechaNacimiento ?? '',
      telefono: usuario.telefono ?? '',
      direccion: usuario.direccion ?? '',
    });

    /* NO PERMITE BORRAR UN DATO QUE YA EXISTÍA */
    const opcionalesSiVacios = ['telefono', 'direccion'] as const;

    for (const campo of opcionalesSiVacios) {
      const control = this.perfilForm.controls[campo];

      if (usuario[campo]) {
        control.addValidators(Validators.required);
      } else {
        control.removeValidators(Validators.required);
      }

      control.updateValueAndValidity();
    }
  }

  /* ENVÍA SOLO LOS CAMPOS QUE CAMBIARON */
  private construirCambios(usuario: UsuarioResponse): ActualizarPerfilRequest {
    const formValue = this.perfilForm.getRawValue();
    const cambios: ActualizarPerfilRequest = {};

    const nombre = formValue.nombre.trim();
    const apellido = formValue.apellido.trim();
    const telefono = formValue.telefono.trim();
    const direccion = formValue.direccion.trim();

    if (nombre !== (usuario.nombre ?? '')) {
      cambios.nombreUsuario = nombre;
    }

    if (apellido !== (usuario.apellido ?? '')) {
      cambios.apellidoUsuario = apellido;
    }

    if (formValue.fechaNacimiento &&
        formValue.fechaNacimiento !== (usuario.fechaNacimiento ?? '')) {
      cambios.fechaNacimiento = formValue.fechaNacimiento;
    }

    if (telefono && telefono !== (usuario.telefono ?? '')) {
      cambios.telefono = telefono;
    }

    if (direccion && direccion !== (usuario.direccion ?? '')) {
      cambios.direccion = direccion;
    }

    return cambios;
  }
}

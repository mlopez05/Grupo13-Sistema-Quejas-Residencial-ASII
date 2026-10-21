import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { UsuarioService } from '../../../../core/services/usuario';
import { ActualizarUsuarioRequest, UsuarioResponse } from '../../../../core/models/usuario.models';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../../core/iconos';
import { fechaMaximaNacimiento, fechaNoFuturaValidator, mayorDeEdadValidator } from '../../../../core/validators/fechas';

/* NO PERMITE TEXTO VACÍO */
const NO_SOLO_ESPACIOS = /\S/;

@Component({
  selector: 'app-editar-usuario',
  imports: [ReactiveFormsModule, RouterLink, LucideAngularModule],
  templateUrl: './editar-usuario.html',
  styleUrl: './editar-usuario.css',
})
export class EditarUsuario implements OnInit {

  readonly icono = ICONOS;

  readonly fechaMaxima = fechaMaximaNacimiento();

  private readonly formBuilder = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly usuarioService = inject(UsuarioService);
  private readonly cdr = inject(ChangeDetectorRef);

  usuario: UsuarioResponse | null = null;
  cargando = true;
  errorCarga = '';

  submitted = false;
  guardando = false;
  sinCambios = false;
  actualizacionExitosa = false;
  mensajeError = '';

  readonly usuarioForm = this.formBuilder.nonNullable.group({
    nombre: ['', [Validators.required, Validators.pattern(NO_SOLO_ESPACIOS)]],
    apellido: ['', [Validators.required, Validators.pattern(NO_SOLO_ESPACIOS)]],
    fechaNacimiento: ['', [mayorDeEdadValidator, fechaNoFuturaValidator]],
    dpi: ['', [Validators.pattern(/^[0-9]{13}$/)]],
    telefono: ['', [Validators.pattern(/^[0-9]{8}$/)]],
    direccion: ['', [Validators.pattern(NO_SOLO_ESPACIOS)]],
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isInteger(id) || id <= 0) {
      this.cargando = false;
      this.errorCarga = 'El identificador del usuario no es válido.';
      return;
    }

    this.usuarioService.obtenerUsuario(id).subscribe({
      next: (usuario) => {
        this.usuario = usuario;
        this.cargarFormulario(usuario);
        this.cargando = false;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.errorCarga =
          error.error?.mensaje ??
          'No se pudo cargar la información del usuario.';
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

    if (this.usuarioForm.invalid) {
      this.usuarioForm.markAllAsTouched();
      return;
    }

    const cambios = this.construirCambios(this.usuario);

    if (Object.keys(cambios).length === 0) {
      this.sinCambios = true;
      return;
    }

    this.guardando = true;

    this.usuarioService.actualizarUsuario(this.usuario.id, cambios).subscribe({
      next: () => {
        this.guardando = false;
        this.actualizacionExitosa = true;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.guardando = false;
        this.mensajeError =
          error.error?.mensaje ??
          'Ocurrió un error al actualizar el usuario.';
        this.cdr.markForCheck();
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/admin/usuarios']);
  }

  onDpiInput(event: Event): void {
    const input = event.target as HTMLInputElement;

    const numericValue = input.value
      .replace(/\D/g, '')
      .slice(0, 13);

    this.usuarioForm.controls.dpi.setValue(numericValue);
  }

  onTelefonoInput(event: Event): void {
    const input = event.target as HTMLInputElement;

    const numericValue = input.value
      .replace(/\D/g, '')
      .slice(0, 8);

    this.usuarioForm.controls.telefono.setValue(numericValue);
  }

  isInvalid(controlName: keyof typeof this.usuarioForm.controls): boolean {
    const control = this.usuarioForm.controls[controlName];

    return control.invalid && (control.touched || this.submitted);
  }

  cerrarModalExito(): void {
    this.actualizacionExitosa = false;
    this.router.navigate(['/admin/usuarios']);
  }

  cerrarModalError(): void {
    this.mensajeError = '';
  }

  private cargarFormulario(usuario: UsuarioResponse): void {
    this.usuarioForm.reset({
      nombre: usuario.nombre ?? '',
      apellido: usuario.apellido ?? '',
      fechaNacimiento: usuario.fechaNacimiento ?? '',
      dpi: usuario.dpi ?? '',
      telefono: usuario.telefono ?? '',
      direccion: usuario.direccion ?? '',
    });

    /* NO PERMITE BORRAR UN DATO QUE YA EXISTÍA */
    const opcionalesSiVacios = ['dpi', 'telefono', 'direccion'] as const;

    for (const campo of opcionalesSiVacios) {
      if (usuario[campo]) {
        this.usuarioForm.controls[campo].addValidators(Validators.required);
        this.usuarioForm.controls[campo].updateValueAndValidity();
      }
    }
  }

  /* ENVÍA SOLO LOS CAMPOS QUE CAMBIARON */
  private construirCambios(usuario: UsuarioResponse): ActualizarUsuarioRequest {
    const formValue = this.usuarioForm.getRawValue();
    const cambios: ActualizarUsuarioRequest = {};

    const nombre = formValue.nombre.trim();
    const apellido = formValue.apellido.trim();
    const dpi = formValue.dpi.trim();
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

    if (dpi && dpi !== (usuario.dpi ?? '')) {
      cambios.dpi = dpi;
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

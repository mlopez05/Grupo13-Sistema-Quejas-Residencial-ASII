import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { switchMap } from 'rxjs';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth';
import { SessionService } from '../../../core/services/session';
import { ICONOS } from '../../../core/iconos';
import { LONGITUD_MINIMA_CLAVE, PATRON_CLAVE, validarCambioClave } from '../../../core/validators/claves';

/* PRIMER INGRESO: CAMBIO DE CONTRASEÑA TEMPORAL */
@Component({
  selector: 'app-cambiar-clave-inicial',
  imports: [ReactiveFormsModule, LucideAngularModule],
  templateUrl: './cambiar-clave-inicial.html',
  styleUrl: './cambiar-clave-inicial.css',
})
export class CambiarClaveInicial {

  readonly icono = ICONOS;

  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly sessionService = inject(SessionService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly correo = this.sessionService.obtenerSesion()?.usuario ?? '';

  submitted = false;
  guardando = false;
  mensajeError = '';
  mostrarClaveActual = false;
  mostrarClaveNueva = false;
  mostrarConfirmacion = false;

  readonly claveForm = this.formBuilder.nonNullable.group({
    claveActual: ['', [Validators.required]],
    claveNueva: ['', [
      Validators.required,
      Validators.minLength(LONGITUD_MINIMA_CLAVE),
      Validators.pattern(PATRON_CLAVE),
    ]],
    confirmarClave: ['', [Validators.required]],
  }, { validators: validarCambioClave });

  guardar(): void {
    this.submitted = true;
    this.mensajeError = '';

    if (this.claveForm.invalid || this.guardando) {
      this.claveForm.markAllAsTouched();
      return;
    }

    const { claveActual, claveNueva } = this.claveForm.getRawValue();

    this.guardando = true;

    this.authService.cambiarClave({ claveActual, claveNueva })
      .pipe(switchMap(() => this.authService.login({ usuario: this.correo, clave: claveNueva })))
      .subscribe({
        next: (respuesta) => {
          this.sessionService.guardarSesion({
            token: respuesta.token,
            usuario: respuesta.usuario,
            rol: respuesta.rol,
            debeCambiarClave: respuesta.debeCambiarClave,
          });

          this.router.navigate([this.sessionService.rutaInicio()]);
        },

        error: (error: HttpErrorResponse) => {
          this.guardando = false;
          this.mensajeError =
            error.error?.mensaje ??
            'No se pudo guardar la nueva contraseña.';
          this.cdr.markForCheck();
        },
      });
  }

  cerrarSesion(): void {
    this.authService.cerrarSesion();
  }

  isInvalid(controlName: keyof typeof this.claveForm.controls): boolean {
    const control = this.claveForm.controls[controlName];

    return control.invalid && (control.touched || this.submitted);
  }
}

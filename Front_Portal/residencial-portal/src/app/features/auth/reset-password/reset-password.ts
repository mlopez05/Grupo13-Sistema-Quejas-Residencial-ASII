import { Component, ChangeDetectorRef, inject } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth';
import { ResetPasswordRequest } from '../../../core/models/auth.models';

function passwordsMatchValidator(
  control: AbstractControl
): ValidationErrors | null {
  const nuevaPassword = control.get('nuevaPassword')?.value;
  const confirmarPassword = control.get('confirmarPassword')?.value;

  if (!nuevaPassword || !confirmarPassword) {
    return null;
  }

  return nuevaPassword === confirmarPassword
    ? null
    : { passwordsMismatch: true };
}

@Component({
  selector: 'app-reset-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword {

  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly router = inject(Router);


  private temporizadorMensaje?: ReturnType<typeof setTimeout>;

  cargando = false;
  restablecimientoExitoso = false;
  mensajeError = '';
  mostrarNuevaPassword = false;
  mostrarConfirmarPassword = false;

  private readonly token = this.activatedRoute.snapshot.queryParamMap.get('token');

  readonly resetPasswordForm = this.formBuilder.nonNullable.group(
    {
      nuevaPassword: [
        '',
        [
          Validators.required,
          Validators.minLength(8),
          Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/),
        ],
      ],
      confirmarPassword: ['', Validators.required],
    },
    {
      validators: passwordsMatchValidator,
    }
  );

  onSubmit(): void {
    this.restablecimientoExitoso = false;
    this.mensajeError = '';

    if (this.resetPasswordForm.invalid) {
      this.resetPasswordForm.markAllAsTouched();
      return;
    }

    if (!this.token) {
      this.mensajeError =
        'El enlace de recuperación no es válido. Solicita uno nuevo.';
      return;
    }

    const request: ResetPasswordRequest = {
      token: this.token,
      nuevaPassword: this.resetPasswordForm.controls.nuevaPassword.value,
    };

    this.cargando = true;

    this.authService.restablecerClave(request).subscribe({
      next: () => {
        this.cargando = false;
        this.restablecimientoExitoso = true;
        this.mensajeError = '';
        this.resetPasswordForm.reset();

        this.changeDetectorRef.detectChanges();

        this.temporizadorMensaje = setTimeout(() => {
          this.router.navigate(['/login']);
        }, 5000);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error al restablecer contraseña:', error);

        this.cargando = false;
        this.restablecimientoExitoso = false;
        this.mensajeError =
          error.error?.mensaje ??
          'No fue posible restablecer la contraseña. Verifica el enlace e inténtalo nuevamente.';

        this.changeDetectorRef.detectChanges();
        this.ocultarMensajeDespuesDeTiempo();
      },
    });
  }

  private ocultarMensajeDespuesDeTiempo(): void {
    clearTimeout(this.temporizadorMensaje);

    this.temporizadorMensaje = setTimeout(() => {
      this.restablecimientoExitoso = false;
      this.mensajeError = '';
      this.changeDetectorRef.detectChanges();
    }, 5000);
  }
}

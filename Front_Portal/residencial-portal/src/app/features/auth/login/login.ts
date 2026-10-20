import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { AuthService } from '../../../core/services/auth';
import { LoginRequest, LoginResponse } from '../../../core/models/auth.models';
import { HttpErrorResponse } from '@angular/common/http';
import { SessionService } from '../../../core/services/session';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, LucideAngularModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {

  readonly icono = ICONOS;

  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly sessionService = inject(SessionService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  submitted = false;
  showPassword = false;
  loginExitoso = false;
  mensajeError = '';


  readonly loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    rememberMe: [false],
  });

  onSubmit(): void {

    this.loginExitoso = false;
    this.mensajeError = '';
    this.submitted = true;

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const formValue = this.loginForm.getRawValue();

    const request: LoginRequest = {
      usuario: formValue.email,
      clave: formValue.password
    };

    this.authService.login(request).subscribe({
      next: (response: LoginResponse) => {
        console.log('Login exitoso', response);

        this.sessionService.guardarSesion({
          token: response.token,
          usuario: response.usuario,
          rol: response.rol,
          debeCambiarClave: response.debeCambiarClave,
        });

        /* CON CONTRASEÑA TEMPORAL PRIMERO DEBE CAMBIARLA */
        if (response.debeCambiarClave) {
          this.router.navigate(['/cambiar-clave-inicial']);
          return;
        }

        this.router.navigate([this.sessionService.rutaInicio()]);
      },

      error: (error: HttpErrorResponse) => {
        console.log('Error en login', error);

        this.loginExitoso = false;

        if (error.status === 401 || error.status === 403) {
          this.mensajeError =
            error.error?.mensaje ??
            'Correo electrónico o contraseña incorrectos.';

        } else {
          this.mensajeError =
            error.error?.mensaje ??
            'No fue posible iniciar sesión. Intenta nuevamente.';
        }

        this.cdr.markForCheck();
      }
    });

    console.log('Formulario válido:', this.loginForm.getRawValue());
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  cerrarModalError(): void {
    this.mensajeError = '';
  }
}
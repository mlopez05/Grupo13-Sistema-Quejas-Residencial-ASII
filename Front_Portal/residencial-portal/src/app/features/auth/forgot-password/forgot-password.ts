import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { RecuperacionRequest } from '../../../core/models/auth.models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-forgot-password',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {

  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  private temporizadorMensaje?: ReturnType<typeof setTimeout>;

  cargando = false;
  solicitudEnviada = false;
  mensajeError = '';

  readonly recuperarForm = this.formBuilder.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
  });

  onSubmit(): void {
    this.solicitudEnviada = false;
    this.mensajeError = '';

    if (this.recuperarForm.invalid) {
      this.recuperarForm.markAllAsTouched();
      return;
    }

    const request: RecuperacionRequest = {
      usuario: this.recuperarForm.controls.correo.value,
    };

    this.cargando = true;

    this.authService.solicitarRecuperacion(request).subscribe({
      next: () => {
        this.solicitudEnviada = true;
        this.mensajeError = '';
        this.cargando = false;
        this.recuperarForm.reset();

        this.changeDetectorRef.detectChanges();

        this.ocultarMensajeDespuesDeTiempo();
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error al solicitar recuperación: ', error);

        this.solicitudEnviada = false;
        
        this.mensajeError =
          error.error?.mensaje ??
          'No fue posible procesar la solicitud, Intenta nuevamente';

        this.cargando = false;

        this.changeDetectorRef.detectChanges();

        this.ocultarMensajeDespuesDeTiempo();
      },
    });
  }
  
  private ocultarMensajeDespuesDeTiempo(): void {
    clearTimeout(this.temporizadorMensaje);

    this.temporizadorMensaje = setTimeout(() => {
      this.solicitudEnviada = false;
      this.mensajeError = '';

      this.changeDetectorRef.detectChanges();
    }, 5000);
  }
}

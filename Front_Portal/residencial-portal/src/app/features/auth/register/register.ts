import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { RegistroRequest } from '../../../core/models/auth.models';
import { HttpErrorResponse } from '@angular/common/http';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';

function passwordsMatchValidator(
  control: AbstractControl
): ValidationErrors | null {
  const password = control.get('clave')?.value;
  const confirmPassword = control.get('confirmarClave')?.value;

  if (password !== confirmPassword) {
    return { passwordsMismatch: true };
  }

  return null;
}


function mayorDeEdadValidator(
  control: AbstractControl
): ValidationErrors | null {
  if (!control.value) return null;

  const nacimiento = new Date(`${control.value}T00:00:00`);
  const hoy = new Date();

  let edad = hoy.getFullYear() - nacimiento.getFullYear();

  if (
    hoy.getMonth() < nacimiento.getMonth() ||
    (hoy.getMonth() === nacimiento.getMonth() &&
      hoy.getDate() < nacimiento.getDate())
  ) {
    edad--;
  }

  return edad >= 18 ? null : { menorDeEdad: true };
}

function fechaNoFuturaValidator(
  control: AbstractControl
): ValidationErrors | null {
  if (!control.value) return null;

  const hoy = new Date();
  const fechaActual = [
    hoy.getFullYear(),
    String(hoy.getMonth() + 1).padStart(2, '0'),
    String(hoy.getDate()).padStart(2, '0'),
  ].join('-');

  return control.value > fechaActual
    ? { fechaFutura: true }
    : null;
}


@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  submitted = false;
  showPassword = false;
  showConfirmPassword = false;
  registroExitoso = false;
  mensajeError = '';
  mostrarModal = false;


  readonly fechaMaxima = this.obtenerFechaActual();

  private obtenerFechaActual(): string {
    const hoy = new Date();

    return [
      hoy.getFullYear(),
      String(hoy.getMonth() + 1).padStart(2, '0'),
      String(hoy.getDate()).padStart(2, '0'),
    ].join('-');
  }


  readonly registerForm = this.formBuilder.group({
    nombre: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(
        /^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?: [A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$/
      ),
    ]),

    apellido: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(
        /^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?: [A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$/
      ),
    ]),

    correo: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.email,
    ]),

    telefono: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/^[0-9]{8}$/),
    ]),

    clave: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).+$/),
    ]),

    confirmarClave: this.formBuilder.nonNullable.control('', [
      Validators.required,
    ]),

    fechaNacimiento: this.formBuilder.nonNullable.control('', [
      Validators.required,
      mayorDeEdadValidator,
      fechaNoFuturaValidator,
    ]),

    dpi: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/^[0-9]{13}$/),
    ]),

    direccion: this.formBuilder.nonNullable.control('', [
      Validators.required,
    ]),
  },
    {
      validators: passwordsMatchValidator,
    }
  );

  onSubmit(): void {
    this.registroExitoso = false;
    this.mensajeError = '';
    this.submitted = true;

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const formValue = this.registerForm.getRawValue();

    const request: RegistroRequest = {
      nombre: formValue.nombre,
      apellido: formValue.apellido,
      correo: formValue.correo,
      clave: formValue.clave,
      fecha_nacimiento: formValue.fechaNacimiento,
      dpi: formValue.dpi,
      direccion: formValue.direccion,
    };

    this.authService.registrarUsuario(request).subscribe({
      next: (response) => {
        console.log('Registro exitoso:', response);

        this.registroExitoso = true;
        this.mensajeError = '';
        this.mostrarModal = true;

        this.registerForm.reset();
        this.submitted = false;
        this.showPassword = false;
        this.showConfirmPassword = false;

        this.changeDetectorRef.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        console.error('Error en registro:', error);

        this.registroExitoso = false;

        if (error.status === 409) {
          this.mensajeError =
            error.error?.mensaje ??
            'El dato ya se encuentra registrado.';
        } else {
          this.mensajeError =
            error.error?.mensaje ??
            'No fue posible completar el registro. Intenta nuevamente.';
        }

        this.mostrarModal = true;
        this.changeDetectorRef.markForCheck();
        console.log('registroExitoso:', this.registroExitoso);
        console.log('mensajeError:', this.mensajeError);
        console.log('mostrarModal:', this.mostrarModal);

      },
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onDpiInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const numericValue = input.value.replace(/\D/g, '').slice(0, 13);

    this.registerForm.controls.dpi.setValue(numericValue);
  }

  isInvalid(controlName: keyof typeof this.registerForm.controls): boolean {
    const control = this.registerForm.controls[controlName];

    return control.invalid && (control.touched || this.submitted);
  }

  isValid(controlName: keyof typeof this.registerForm.controls): boolean {
    const control = this.registerForm.controls[controlName];

    return control.valid && control.touched;
  }

  onTelefonoInput(event: Event): void {
    const input = event.target as HTMLInputElement;

    input.value = input.value
      .replace(/\D/g, '')
      .slice(0, 8);

    this.registerForm.controls.telefono.setValue(input.value);
  }


  onNombreApellidoInput(
    event: Event,
    campo: 'nombre' | 'apellido'
  ): void {
    const input = event.target as HTMLInputElement;

    const valor = input.value.replace(
      /[^a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\s]/g,
      ''
    );

    input.value = valor;
    this.registerForm.controls[campo].setValue(valor);
  }

  cerrarMensaje(): void {
    this.mostrarModal = false;
    this.registroExitoso = false;
    this.mensajeError = '';
  }
}
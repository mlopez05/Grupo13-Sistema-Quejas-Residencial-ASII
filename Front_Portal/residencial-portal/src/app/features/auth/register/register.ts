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
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';
import { fechaMaximaNacimiento, fechaNoFuturaValidator, mayorDeEdadValidator } from '../../../core/validators/fechas';

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


@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, LucideAngularModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {

  readonly icono = ICONOS;

  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  submitted = false;
  showPassword = false;
  showConfirmPassword = false;
  registroExitoso = false;
  mensajeError = '';
  mostrarModal = false;


  readonly fechaMaxima = fechaMaximaNacimiento();


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
      telefono: formValue.telefono,
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
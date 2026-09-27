import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../../core/services/auth';
import { RegistroRequestPortal } from '../../../../core/models/auth.models';


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
  selector: 'app-nuevo-usuario',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './nuevo-usuario.html',
  styleUrl: './nuevo-usuario.css',
})
export class NuevoUsuario {

  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  registroExitoso = false;
  mensajeError = '';

  submitted = false;
  showPassword = false;
  showConfirmPassword = false;

  readonly roles = [
    { id: 1, nombre: 'Administrador' },
    { id: 2, nombre: 'Analista' },
    { id: 3, nombre: 'Supervisor' },
  ];

  readonly usuarioForm = this.formBuilder.nonNullable.group({
    nombre: ['', [Validators.required]],
    apellido: ['', [Validators.required]],
    correo: ['', [Validators.required, Validators.email]],
    clave: ['', [Validators.required, Validators.minLength(8)]],
    confirmarClave: ['', [Validators.required]],
    fechaNacimiento: ['', [Validators.required]],
    dpi: ['', [
      Validators.required,
      Validators.pattern(/^[0-9]{13}$/),
    ]],
    rol: ['', [Validators.required]],
    direccion: [''],
  },
    {
      validators: passwordsMatchValidator,
    }
  );

  onSubmit(): void {
    this.submitted = true;
    this.mensajeError = '';

    if (this.usuarioForm.invalid) {
      this.usuarioForm.markAllAsTouched();
      return;
    }

    const formValue = this.usuarioForm.getRawValue();

    const request: RegistroRequestPortal = {
      nombre: formValue.nombre,
      apellido: formValue.apellido,
      correo: formValue.correo,
      clave: formValue.clave,
      fecha_nacimiento: formValue.fechaNacimiento,
      dpi: formValue.dpi,
      id_rol: Number(formValue.rol),
      direccion: formValue.direccion,
    };

    this.authService.registrarAdministrativo(request).subscribe({
      next: () => {
        this.registroExitoso = true;
        this.submitted = false;

        this.usuarioForm.reset({
          nombre: '',
          apellido: '',
          correo: '',
          clave: '',
          confirmarClave: '',
          fechaNacimiento: '',
          dpi: '',
          rol: '',
          direccion: '',
        });

        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.mensajeError =
          error.error?.mensaje ??
          'Ocurrió un error al registrar el usuario administrativo.';

        this.cdr.markForCheck();
      },
    });
  }

  cancelar(): void {
    this.router.navigate(['/admin/usuarios']);
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onDpiInput(event: Event): void {
    const input = event.target as HTMLInputElement;

    const numericValue = input.value
      .replace(/\D/g, '')
      .slice(0, 13);

    this.usuarioForm.controls.dpi.setValue(numericValue);
  }

  isInvalid(controlName: keyof typeof this.usuarioForm.controls): boolean {
    const control = this.usuarioForm.controls[controlName];

    return control.invalid && (control.touched || this.submitted);
  }

  cerrarModalExito(): void {
    this.registroExitoso = false;
    this.router.navigate(['/admin/usuarios']);
  }

  cerrarModalError(): void {
    this.mensajeError = '';
  }

}
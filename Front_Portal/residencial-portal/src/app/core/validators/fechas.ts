import { AbstractControl, ValidationErrors } from '@angular/forms';

/* REGLAS DE LA FECHA DE NACIMIENTO */

export const EDAD_MINIMA = 18;

/* FECHA DE HOY */
export function fechaHoy(): string {
  const hoy = new Date();

  return [
    hoy.getFullYear(),
    String(hoy.getMonth() + 1).padStart(2, '0'),
    String(hoy.getDate()).padStart(2, '0'),
  ].join('-');
}

export function mayorDeEdadValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;

  const nacimiento = new Date(`${control.value}T00:00:00`);
  const hoy = new Date();

  let edad = hoy.getFullYear() - nacimiento.getFullYear();

  if (
    hoy.getMonth() < nacimiento.getMonth() ||
    (hoy.getMonth() === nacimiento.getMonth() && hoy.getDate() < nacimiento.getDate())
  ) {
    edad--;
  }

  return edad >= EDAD_MINIMA ? null : { menorDeEdad: true };
}

export function fechaNoFuturaValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;

  return control.value > fechaHoy() ? { fechaFutura: true } : null;
}

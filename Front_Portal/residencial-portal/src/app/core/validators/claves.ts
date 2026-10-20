import { AbstractControl, ValidationErrors } from '@angular/forms';

/* REGLAS DE LA CONTRASEÑA */
export const LONGITUD_MINIMA_CLAVE = 8;
export const PATRON_CLAVE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).+$/;

/* VALIDA EL CAMBIO DE CONTRASEÑA */
export function validarCambioClave(grupo: AbstractControl): ValidationErrors | null {
  const actual = grupo.get('claveActual')?.value;
  const nueva = grupo.get('claveNueva')?.value;
  const confirmar = grupo.get('confirmarClave')?.value;
  const errores: ValidationErrors = {};

  if (nueva && confirmar && nueva !== confirmar) {
    errores['noCoinciden'] = true;
  }

  if (actual && nueva && actual === nueva) {
    errores['igualALaActual'] = true;
  }

  return Object.keys(errores).length ? errores : null;
}

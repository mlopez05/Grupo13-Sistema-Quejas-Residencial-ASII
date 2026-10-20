import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse, HttpEventType } from '@angular/common/http';
import { CasosService } from '../../../core/services/casos';
import { CasoResponse, CategoriaResponse, TipoIncidenteResponse } from '../../../core/models/caso.models';
import { SelectorAdjuntos } from '../selector-adjuntos/selector-adjuntos';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';

const DESCRIPCION_MIN = 20;
const DESCRIPCION_MAX = 2000;

@Component({
  selector: 'app-nuevo-caso',
  imports: [ReactiveFormsModule, RouterLink, SelectorAdjuntos, LucideAngularModule],
  templateUrl: './nuevo-caso.html',
  styleUrl: './nuevo-caso.css',
})
export class NuevoCaso implements OnInit {

  readonly icono = ICONOS;

  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly casosService = inject(CasosService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly descripcionMin = DESCRIPCION_MIN;
  readonly descripcionMax = DESCRIPCION_MAX;

  catalogo: CategoriaResponse[] = [];
  tiposDisponibles: TipoIncidenteResponse[] = [];
  cargandoCatalogo = true;
  errorCatalogo = '';

  submitted = false;
  enviando = false;
  etapa = '';
  progresoCarga: number | null = null;
  casoRegistrado: CasoResponse | null = null;
  mensajeError = '';

  archivos: File[] = [];

  readonly casoForm = this.formBuilder.nonNullable.group({
    idCategoria: ['', [Validators.required]],
    idTipoIncidente: [{ value: '', disabled: true }, [Validators.required]],
    descripcion: ['', [
      Validators.required,
      Validators.minLength(DESCRIPCION_MIN),
      Validators.maxLength(DESCRIPCION_MAX),
      Validators.pattern(/\S/),
    ]],
  });

  ngOnInit(): void {
    this.cargarCatalogo();

    /* AL CAMBIAR LA CATEGORÍA SE REINICIA EL TIPO */
    this.casoForm.controls.idCategoria.valueChanges.subscribe((idCategoria) => {
      const categoria = this.catalogo.find(c => c.id === Number(idCategoria));
      const tipoControl = this.casoForm.controls.idTipoIncidente;

      this.tiposDisponibles = categoria?.tipos ?? [];
      tipoControl.setValue('');

      if (this.tiposDisponibles.length > 0) {
        tipoControl.enable();
      } else {
        tipoControl.disable();
      }
    });
  }

  cargarCatalogo(): void {
    this.cargandoCatalogo = true;
    this.errorCatalogo = '';

    this.casosService.obtenerCatalogo().subscribe({
      next: (catalogo) => {
        this.catalogo = catalogo;
        this.cargandoCatalogo = false;
        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.errorCatalogo =
          error.error?.mensaje ??
          'No se pudo cargar el catálogo de categorías.';
        this.cargandoCatalogo = false;
        this.cdr.markForCheck();
      },
    });
  }

  get longitudDescripcion(): number {
    return this.casoForm.controls.descripcion.value.trim().length;
  }

  onSubmit(): void {
    this.submitted = true;
    this.mensajeError = '';

    if (this.casoForm.invalid || this.enviando) {
      this.casoForm.markAllAsTouched();
      return;
    }

    const formValue = this.casoForm.getRawValue();

    this.enviando = true;
    this.etapa = this.archivos.length > 0 ? 'Enviando caso y adjuntos...' : 'Registrando caso...';
    this.progresoCarga = this.archivos.length > 0 ? 0 : null;

    /* REGISTRA EL CASO CON SUS ADJUNTOS */
    this.casosService.registrarCaso({
      idCategoria: Number(formValue.idCategoria),
      idTipoIncidente: Number(formValue.idTipoIncidente),
      descripcion: formValue.descripcion.trim(),
    }, this.archivos).subscribe({
      next: (evento) => {
        if (evento.type === HttpEventType.UploadProgress && evento.total && this.progresoCarga !== null) {
          this.progresoCarga = Math.round((evento.loaded / evento.total) * 100);
          /* ARCHIVOS ENVIADOS, FALTA LA RESPUESTA */
          if (this.progresoCarga === 100) {
            this.etapa = 'Validando adjuntos y registrando caso...';
          }
          this.cdr.markForCheck();
        }

        if (evento.type === HttpEventType.Response && evento.body) {
          this.enviando = false;
          this.progresoCarga = null;
          this.casoRegistrado = evento.body;
          this.cdr.markForCheck();
        }
      },

      error: (error: HttpErrorResponse) => {
        this.enviando = false;
        this.progresoCarga = null;
        this.mensajeError =
          error.error?.mensaje ??
          'Ocurrió un error al registrar el caso.';
        this.cdr.markForCheck();
      },
    });
  }

  /* CANCELA EL REGISTRO */
  cancelar(): void {
    this.router.navigate(['/dashboard']);
  }

  isInvalid(controlName: keyof typeof this.casoForm.controls): boolean {
    const control = this.casoForm.controls[controlName];

    return control.invalid && (control.touched || this.submitted);
  }

  verCaso(): void {
    if (this.casoRegistrado) {
      this.router.navigate(['/casos', this.casoRegistrado.id]);
    }
  }

  irAMisCasos(): void {
    this.router.navigate(['/casos']);
  }

  cerrarModalError(): void {
    this.mensajeError = '';
  }
}

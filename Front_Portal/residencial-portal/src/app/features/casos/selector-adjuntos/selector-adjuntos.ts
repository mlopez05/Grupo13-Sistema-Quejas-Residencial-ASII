import { Component, OnDestroy, computed, input, model, signal } from '@angular/core';
import {
  EXTENSIONES_ACEPTADAS,
  ICONO_CATEGORIA,
  MAXIMO_ARCHIVOS_POR_CARGA,
  MAXIMO_TOTAL_ADJUNTOS,
  REGLAS_ADJUNTOS,
  extensionDe,
  formatearTamano,
} from '../../../core/models/caso.models';
import { LucideAngularModule, LucideIconData } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';

/* SELECTOR DE ARCHIVOS ADJUNTOS */
@Component({
  selector: 'app-selector-adjuntos',
  imports: [LucideAngularModule],
  templateUrl: './selector-adjuntos.html',
  styleUrl: './selector-adjuntos.css',
})
export class SelectorAdjuntos implements OnDestroy {

  readonly icono = ICONOS;

  readonly archivos = model<File[]>([]);

  /* TAMAÑO YA ADJUNTADO AL CASO */
  readonly usadoBytes = input(0);
  readonly deshabilitado = input(false);

  readonly errores = signal<string[]>([]);
  readonly arrastrando = signal(false);

  readonly extensionesAceptadas = EXTENSIONES_ACEPTADAS;
  readonly formatearTamano = formatearTamano;

  private readonly miniaturas = new Map<File, string>();

  readonly totalSeleccionado = computed(() =>
    this.archivos().reduce((total, archivo) => total + archivo.size, 0));

  readonly disponibleBytes = computed(() =>
    Math.max(0, MAXIMO_TOTAL_ADJUNTOS - this.usadoBytes() - this.totalSeleccionado()));

  onSeleccion(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.agregar(Array.from(input.files ?? []));
    input.value = ''; /* PERMITE ELEGIR EL MISMO ARCHIVO OTRA VEZ */
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();

    if (!this.deshabilitado()) {
      this.arrastrando.set(true);
    }
  }

  onDragLeave(): void {
    this.arrastrando.set(false);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.arrastrando.set(false);

    if (!this.deshabilitado()) {
      this.agregar(Array.from(event.dataTransfer?.files ?? []));
    }
  }

  quitar(archivo: File): void {
    this.liberarMiniatura(archivo);
    this.archivos.update(lista => lista.filter(a => a !== archivo));
  }

  /* LIMPIA LA SELECCIÓN */
  limpiar(): void {
    this.archivos().forEach(archivo => this.liberarMiniatura(archivo));
    this.archivos.set([]);
    this.errores.set([]);
  }

  iconoArchivo(archivo: File): LucideIconData {
    const regla = REGLAS_ADJUNTOS[extensionDe(archivo.name)];
    return regla ? ICONO_CATEGORIA[regla.categoria] : ICONOS.Paperclip;
  }

  miniatura(archivo: File): string | null {
    return this.miniaturas.get(archivo) ?? null;
  }

  ngOnDestroy(): void {
    this.miniaturas.forEach(url => URL.revokeObjectURL(url));
    this.miniaturas.clear();
  }

  private agregar(nuevos: File[]): void {
    const errores: string[] = [];
    const aceptados: File[] = [];
    let disponible = this.disponibleBytes();
    let espacioEnLote = MAXIMO_ARCHIVOS_POR_CARGA - this.archivos().length;

    for (const archivo of nuevos) {
      const regla = REGLAS_ADJUNTOS[extensionDe(archivo.name)];

      if (!regla) {
        errores.push(`"${archivo.name}": formato no permitido.`);
      } else if (archivo.size === 0) {
        errores.push(`"${archivo.name}": el archivo está vacío.`);
      } else if (archivo.size > regla.maximoBytes) {
        errores.push(`"${archivo.name}": supera el máximo de ${formatearTamano(regla.maximoBytes)} para este tipo.`);
      } else if (archivo.size > disponible) {
        errores.push(`"${archivo.name}": se supera el máximo de 150 MB de adjuntos por caso.`);
      } else if (espacioEnLote <= 0) {
        errores.push(`"${archivo.name}": puede adjuntar hasta ${MAXIMO_ARCHIVOS_POR_CARGA} archivos a la vez.`);
      } else if (this.archivos().some(a => a.name === archivo.name && a.size === archivo.size)) {
        continue; /* YA ESTABA SELECCIONADO */
      } else {
        aceptados.push(archivo);
        disponible -= archivo.size;
        espacioEnLote--;

        if (regla.categoria === 'IMAGEN') {
          this.miniaturas.set(archivo, URL.createObjectURL(archivo));
        }
      }
    }

    this.errores.set(errores);

    if (aceptados.length > 0) {
      this.archivos.update(lista => [...lista, ...aceptados]);
    }
  }

  private liberarMiniatura(archivo: File): void {
    const url = this.miniaturas.get(archivo);

    if (url) {
      URL.revokeObjectURL(url);
      this.miniaturas.delete(archivo);
    }
  }
}

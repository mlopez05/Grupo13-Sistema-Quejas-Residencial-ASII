import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';
import { ContenidoService } from '../../../core/services/contenido';
import { CategoriaAdmin } from '../../../core/models/contenido.models';

/* DATOS DEL FORMULARIO */
interface EdicionCatalogo {
  clase: 'categoria' | 'tipo';
  id: number | null;
  idCategoria: number | null;
  nombre: string;
  activo: boolean;
}

/* CATÁLOGO DE CATEGORÍAS Y TIPOS DE INCIDENTE */
@Component({
  selector: 'app-catalogo',
  imports: [FormsModule, LucideAngularModule],
  templateUrl: './catalogo.html',
})
export class Catalogo implements OnInit {

  readonly icono = ICONOS;

  private readonly contenidoService = inject(ContenidoService);
  private readonly cdr = inject(ChangeDetectorRef);

  categorias: CategoriaAdmin[] = [];
  cargando = true;
  error = '';

  edicion: EdicionCatalogo | null = null;
  guardando = false;
  errorEdicion = '';

  ngOnInit(): void {
    this.contenidoService.listarCatalogo().subscribe({
      next: (categorias) => this.aplicar(categorias),
      error: () => {
        this.error = 'No se pudo cargar el catálogo.';
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  nuevaCategoria(): void {
    this.abrir({ clase: 'categoria', id: null, idCategoria: null, nombre: '', activo: true });
  }

  editarCategoria(categoria: CategoriaAdmin): void {
    this.abrir({ clase: 'categoria', id: categoria.id, idCategoria: null, nombre: categoria.nombre, activo: categoria.activo });
  }

  nuevoTipo(categoria: CategoriaAdmin): void {
    this.abrir({ clase: 'tipo', id: null, idCategoria: categoria.id, nombre: '', activo: true });
  }

  editarTipo(categoria: CategoriaAdmin, tipo: CategoriaAdmin['tipos'][number]): void {
    this.abrir({ clase: 'tipo', id: tipo.id, idCategoria: categoria.id, nombre: tipo.nombre, activo: tipo.activo });
  }

  cerrar(): void {
    if (!this.guardando) {
      this.edicion = null;
    }
  }

  guardar(): void {
    const edicion = this.edicion;

    if (!edicion || this.guardando) {
      return;
    }

    const nombre = edicion.nombre.trim();

    if (!nombre) {
      this.errorEdicion = 'Ingresa un nombre.';
      return;
    }

    let peticion: Observable<CategoriaAdmin[]>;

    if (edicion.clase === 'categoria') {
      peticion = edicion.id === null
        ? this.contenidoService.crearCategoria(nombre, edicion.activo)
        : this.contenidoService.actualizarCategoria(edicion.id, nombre, edicion.activo);
    } else {
      const idCategoria = edicion.idCategoria!;
      peticion = edicion.id === null
        ? this.contenidoService.crearTipo(idCategoria, nombre, edicion.activo)
        : this.contenidoService.actualizarTipo(edicion.id, idCategoria, nombre, edicion.activo);
    }

    this.guardando = true;
    this.errorEdicion = '';

    peticion.subscribe({
      next: (categorias) => {
        this.guardando = false;
        this.edicion = null;
        this.aplicar(categorias);
      },
      error: (error: HttpErrorResponse) => {
        this.errorEdicion = error.error?.mensaje ?? 'No se pudo guardar.';
        this.guardando = false;
        this.cdr.markForCheck();
      },
    });
  }

  /* SI NINGUN CASO LA USA SE PUEDE ELIMINAR; SI NO, SOLO DESACTIVAR */
  eliminarCategoria(categoria: CategoriaAdmin): void {
    if (!confirm(`¿Eliminar la categoría "${categoria.nombre}" y sus ${categoria.tipos.length} tipos? Esta acción no se puede deshacer.`)) {
      return;
    }

    this.eliminar(this.contenidoService.eliminarCategoria(categoria.id));
  }

  eliminarTipo(tipo: CategoriaAdmin['tipos'][number]): void {
    if (!confirm(`¿Eliminar el tipo "${tipo.nombre}"? Esta acción no se puede deshacer.`)) {
      return;
    }

    this.eliminar(this.contenidoService.eliminarTipo(tipo.id));
  }

  tiposActivos(categoria: CategoriaAdmin): number {
    return categoria.tipos.filter(t => t.activo).length;
  }

  get tituloEdicion(): string {
    if (!this.edicion) {
      return '';
    }

    const accion = this.edicion.id === null ? 'Nueva' : 'Editar';
    return this.edicion.clase === 'categoria'
      ? `${accion} categoría`
      : `${this.edicion.id === null ? 'Nuevo' : 'Editar'} tipo de incidente`;
  }

  get categoriaDeEdicion(): string {
    return this.categorias.find(c => c.id === this.edicion?.idCategoria)?.nombre ?? '';
  }

  private abrir(edicion: EdicionCatalogo): void {
    this.edicion = edicion;
    this.errorEdicion = '';
  }

  private eliminar(peticion: Observable<CategoriaAdmin[]>): void {
    this.error = '';

    peticion.subscribe({
      next: (categorias) => this.aplicar(categorias),
      error: (error: HttpErrorResponse) => {
        this.error = error.error?.mensaje ?? 'No se pudo eliminar.';
        this.cdr.markForCheck();
      },
    });
  }

  private aplicar(categorias: CategoriaAdmin[]): void {
    this.categorias = categorias;
    this.cargando = false;
    this.cdr.markForCheck();
  }
}

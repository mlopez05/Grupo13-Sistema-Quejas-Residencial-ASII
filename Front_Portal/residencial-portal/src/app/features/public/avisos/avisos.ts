import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';
import { ContenidoService } from '../../../core/services/contenido';
import {
  AvisoResponse,
  CATEGORIAS_AVISO,
  CategoriaAviso,
  ESTILO_CATEGORIA_AVISO,
} from '../../../core/models/contenido.models';

/* AVISOS DE LA COMUNIDAD */
@Component({
  selector: 'app-avisos',
  imports: [DatePipe, LucideAngularModule],
  templateUrl: './avisos.html',
  styleUrl: './avisos.css',
})
export class Avisos implements OnInit {
  readonly icono = ICONOS;
  readonly categorias = CATEGORIAS_AVISO;
  readonly estiloCategoria = ESTILO_CATEGORIA_AVISO;

  private readonly contenidoService = inject(ContenidoService);
  private readonly cdr = inject(ChangeDetectorRef);

  avisos: AvisoResponse[] = [];
  categoria: CategoriaAviso | null = null;
  cargando = true;
  error = false;

  ngOnInit(): void {
    this.contenidoService.avisosVigentes().subscribe({
      next: (avisos) => {
        this.avisos = avisos;
        this.cargando = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = true;
        this.cargando = false;
        this.cdr.markForCheck();
      },
    });
  }

  get avisosFiltrados(): AvisoResponse[] {
    return this.categoria ? this.avisos.filter(a => a.categoria === this.categoria) : this.avisos;
  }

  /* CATEGORÍAS CON AVISOS */
  get categoriasConAvisos() {
    return this.categorias.filter(c => this.avisos.some(a => a.categoria === c.valor));
  }
}

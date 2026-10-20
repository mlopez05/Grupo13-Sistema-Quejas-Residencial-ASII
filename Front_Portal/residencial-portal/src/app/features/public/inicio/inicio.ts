import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';
import { ContenidoService } from '../../../core/services/contenido';
import { AvisoResponse, ESTILO_CATEGORIA_AVISO } from '../../../core/models/contenido.models';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink, DatePipe, LucideAngularModule],
  templateUrl: './inicio.html',
  styleUrl: './inicio.css',
})
export class Inicio implements OnInit {
  readonly icono = ICONOS;
  readonly estiloCategoria = ESTILO_CATEGORIA_AVISO;

  private readonly contenidoService = inject(ContenidoService);
  private readonly cdr = inject(ChangeDetectorRef);

  /* DATOS DEL RESIDENCIAL */
  readonly configuracion = this.contenidoService.configuracion;

  avisos: AvisoResponse[] = [];
  avisosCargados = false;

  ngOnInit(): void {
    this.contenidoService.avisosVigentes(3).subscribe({
      next: (avisos) => {
        this.avisos = avisos;
        this.avisosCargados = true;
        this.cdr.markForCheck();
      },
      error: () => {
        this.avisosCargados = true;
        this.cdr.markForCheck();
      },
    });
  }
}

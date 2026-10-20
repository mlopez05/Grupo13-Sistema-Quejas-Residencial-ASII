import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';
import { ContenidoService } from '../../../core/services/contenido';
import { ServicioResponse } from '../../../core/models/contenido.models';

/* SERVICIOS DEL RESIDENCIAL */
@Component({
  selector: 'app-servicios',
  imports: [RouterLink, LucideAngularModule],
  templateUrl: './servicios.html',
  styleUrl: './servicios.css',
})
export class Servicios implements OnInit {
  readonly icono = ICONOS;

  private readonly contenidoService = inject(ContenidoService);
  private readonly cdr = inject(ChangeDetectorRef);

  servicios: ServicioResponse[] = [];
  cargando = true;
  error = false;

  ngOnInit(): void {
    this.contenidoService.serviciosActivos().subscribe({
      next: (servicios) => {
        this.servicios = servicios;
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
}

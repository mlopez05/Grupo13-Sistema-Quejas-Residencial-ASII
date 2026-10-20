import { Component, inject } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../../core/iconos';
import { ContenidoService } from '../../../core/services/contenido';

/* PÁGINA NOSOTROS */
@Component({
  selector: 'app-nosotros',
  imports: [LucideAngularModule],
  templateUrl: './nosotros.html',
  styleUrl: './nosotros.css',
})
export class Nosotros {
  readonly icono = ICONOS;

  readonly configuracion = inject(ContenidoService).configuracion;
}

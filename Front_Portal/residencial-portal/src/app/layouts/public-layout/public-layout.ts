import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { ICONOS } from '../../core/iconos';
import { ContenidoService } from '../../core/services/contenido';

@Component({
  selector: 'app-public-layout',
  imports: [RouterOutlet,
    RouterLink,
    RouterLinkActive, LucideAngularModule],
  templateUrl: './public-layout.html',
  styleUrl: './public-layout.css',
})
export class PublicLayout implements OnInit {
  readonly icono = ICONOS;
  readonly anio = new Date().getFullYear();

  private readonly contenidoService = inject(ContenidoService);

  /* DATOS DEL RESIDENCIAL */
  readonly configuracion = this.contenidoService.configuracion;

  ngOnInit(): void {
    this.contenidoService.cargarConfiguracion();
  }
}

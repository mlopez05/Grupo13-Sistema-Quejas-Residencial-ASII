import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  rol: string;
  estado: 'Activo' | 'Inactivo';
}

@Component({
  selector: 'app-usuarios',
  imports: [FormsModule, RouterLink],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios {

  readonly roles = [
    'Analista',
    'Supervisor',
    'Administrador',
    'Residente'
  ];

  readonly usuarios: Usuario[] = [
    {
      id: 1,
      nombre: 'Juan Pérez',
      correo: 'juan.perez@residencial.com',
      rol: 'Analista',
      estado: 'Activo',
    },
    {
      id: 2,
      nombre: 'María López',
      correo: 'maria.lopez@residencial.com',
      rol: 'Supervisor',
      estado: 'Activo',
    },
    {
      id: 3,
      nombre: 'Carlos Méndez',
      correo: 'carlos.mendez@residencial.com',
      rol: 'Administrador',
      estado: 'Activo',
    },
    {
      id: 4,
      nombre: 'Ana Castillo',
      correo: 'ana.castillo@residencial.com',
      rol: 'Analista',
      estado: 'Inactivo',
    },
    {
      id: 5,
      nombre: 'Carlos Juarez',
      correo: 'c.juarez@residencial.com',
      rol: 'Residente',
      estado: 'Activo',
    },
  ];

  terminoBusqueda = '';
  rolSeleccionado = '';
  estadoSeleccionado = '';

  get usuariosFiltrados(): Usuario[] {
    const termino = this.terminoBusqueda.toLowerCase().trim();

    return this.usuarios.filter(usuario => {

      const coincideBusqueda =
        !termino ||
        usuario.nombre.toLowerCase().includes(termino) ||
        usuario.correo.toLowerCase().includes(termino);

      const coincideRol =
        !this.rolSeleccionado ||
        usuario.rol === this.rolSeleccionado;

      const coincideEstado =
        !this.estadoSeleccionado ||
        usuario.estado === this.estadoSeleccionado;

      return coincideBusqueda && coincideRol && coincideEstado;
    });
  }
}
import { Routes } from '@angular/router';

import { PublicLayout } from './layouts/public-layout/public-layout';
import { authGuard } from './core/guards/auth.guard';
import { MainLayout } from './layout/main-layout/main-layout';
import { roleGuard } from './core/guards/role-guard';
import { AdminLayout } from './layout/admin-layout/admin-layout';

export const routes: Routes = [

  // =========================
  // ÁREA PÚBLICA
  // =========================
  {
    path: '',
    component: PublicLayout,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/public/inicio/inicio')
            .then(m => m.Inicio),
      },
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/login/login')
            .then(m => m.Login),
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/register/register')
            .then(m => m.Register),
      },
      {
        path: 'nosotros',
        loadComponent: () =>
          import('./features/public/nosotros/nosotros')
            .then(m => m.Nosotros),
      },
      {
        path: 'servicios',
        loadComponent: () =>
          import('./features/public/servicios/servicios')
            .then(m => m.Servicios),
      },
      {
        path: 'avisos',
        loadComponent: () =>
          import('./features/public/avisos/avisos')
            .then(m => m.Avisos),
      },
    ],
  },


  // =========================
  // ÁREA PRIVADA RESIDENTE
  // =========================
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard')
            .then(m => m.Dashboard),
      },
    ],
  },


  // =========================
  // ÁREA ADMINISTRATIVA
  // =========================
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['Analista', 'Supervisor', 'Administrador'],
    },
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard/admin-dashboard/admin-dashboard')
            .then(m => m.AdminDashboard),
      },
      {
        path: 'usuarios/nuevo',
        canActivate: [roleGuard],
        data: {
          roles: ['Administrador'],
        },
        loadComponent: () =>
          import('./features/admin/usuarios/nuevo-usuario/nuevo-usuario')
            .then(m => m.NuevoUsuario),
      },
      {
        path: 'usuarios',
        canActivate: [roleGuard],
        data: {
          roles: ['Administrador'],
        },
        loadComponent: () =>
          import('./features/admin/usuarios/usuarios/usuarios')
            .then(m => m.Usuarios),
      },
    ],
  },


  // =========================
  // RUTA NO ENCONTRADA
  // =========================
  {
    path: '**',
    redirectTo: '',
  },
];
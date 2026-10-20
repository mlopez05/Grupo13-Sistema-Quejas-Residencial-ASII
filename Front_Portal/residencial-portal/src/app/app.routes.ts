import { Routes } from '@angular/router';

import { PublicLayout } from './layouts/public-layout/public-layout';
import { authGuard, claveTemporalGuard } from './core/guards/auth.guard';
import { MainLayout } from './layout/main-layout/main-layout';
import { roleGuard } from './core/guards/role-guard';
import { AdminLayout } from './layout/admin-layout/admin-layout';

export const routes: Routes = [

  /* ÁREA PÚBLICA */
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
      {
        path: 'restaurar-clave',
        loadComponent: () =>
          import('./features/auth/forgot-password/forgot-password')
            .then(m => m.ForgotPassword)
      },
      {
        path: 'restablecer-password',
        loadComponent: () =>
          import('./features/auth/reset-password/reset-password')
            .then(m => m.ResetPassword),
      }

    ],
  },


  /* PRIMER INGRESO DEL PERSONAL */
  {
    path: 'cambiar-clave-inicial',
    canActivate: [claveTemporalGuard],
    loadComponent: () =>
      import('./features/auth/cambiar-clave-inicial/cambiar-clave-inicial')
        .then(m => m.CambiarClaveInicial),
  },


  /* ÁREA DEL RESIDENTE */
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
      {
        path: 'perfil',
        loadComponent: () =>
          import('./features/perfil/perfil')
            .then(m => m.Perfil),
      },
      {
        path: 'casos',
        data: { modo: 'residente' },
        loadComponent: () =>
          import('./features/casos/lista-casos/lista-casos')
            .then(m => m.ListaCasos),
      },
      {
        path: 'casos/nuevo',
        loadComponent: () =>
          import('./features/casos/nuevo-caso/nuevo-caso')
            .then(m => m.NuevoCaso),
      },
      {
        path: 'casos/:id',
        data: { modo: 'residente' },
        loadComponent: () =>
          import('./features/casos/detalle-caso/detalle-caso')
            .then(m => m.DetalleCaso),
      },
    ],
  },


  /* ÁREA ADMINISTRATIVA */
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
        path: 'perfil',
        loadComponent: () =>
          import('./features/perfil/perfil')
            .then(m => m.Perfil),
      },
      {
        path: 'casos',
        data: { modo: 'personal' },
        loadComponent: () =>
          import('./features/casos/lista-casos/lista-casos')
            .then(m => m.ListaCasos),
      },
      {
        path: 'casos/:id',
        data: { modo: 'personal' },
        loadComponent: () =>
          import('./features/casos/detalle-caso/detalle-caso')
            .then(m => m.DetalleCaso),
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
        path: 'usuarios/editar/:id',
        canActivate: [roleGuard],
        data: {
          roles: ['Administrador'],
        },
        loadComponent: () =>
          import('./features/admin/usuarios/editar-usuario/editar-usuario')
            .then(m => m.EditarUsuario),
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
      {
        path: 'reportes',
        canActivate: [roleGuard],
        data: {
          roles: ['Supervisor', 'Administrador'],
        },
        loadComponent: () =>
          import('./features/admin/reportes/reportes')
            .then(m => m.Reportes),
      },
      {
        path: 'catalogo',
        canActivate: [roleGuard],
        data: {
          roles: ['Administrador'],
        },
        loadComponent: () =>
          import('./features/admin/catalogo/catalogo')
            .then(m => m.Catalogo),
      },
      {
        path: 'contenido',
        canActivate: [roleGuard],
        data: {
          roles: ['Administrador'],
        },
        loadComponent: () =>
          import('./features/admin/contenido/contenido')
            .then(m => m.Contenido),
      },
    ],
  },


  /* RUTA NO ENCONTRADA */
  {
    path: '**',
    redirectTo: '',
  },
];
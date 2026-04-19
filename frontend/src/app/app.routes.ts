import { Routes } from '@angular/router';
import { Home } from './features/home/components/home/home';
import { Contact } from './features/contact/contact';
import { Catalog } from './features/catalog/catalog';
import { About } from './features/about/about';
import { Login } from './features/auth/components/login/login';
import { Register } from './features/auth/components/register/register';
import { Dashboard } from './features/dashboard/dashboard';
import { authGuard } from './shared/guards/auth.guard';
import { adminGuard } from './shared/guards/admin.guard';
import { IntranetRedirect } from './features/intranet/intranet-redirect';
import { IntranetAgricultor } from './features/intranet/intranet-agricultor';
import { IntranetAdmin } from './features/intranet/intranet-admin';

export const routes: Routes = [
  { path: '',              component: Home },
  { path: 'form',          component: Contact },
  { path: 'catalog',       component: Catalog },
  { path: 'about',         component: About },
  { path: 'login',         component: Login },
  { path: 'register',      component: Register },
  { path: 'dashboard',     component: Dashboard,           canActivate: [authGuard] },
  // ── INTRANET ──────────────────────────────────────────
  { path: 'intranet',           component: IntranetRedirect,    canActivate: [authGuard] },
  { path: 'intranet/agricultor', component: IntranetAgricultor, canActivate: [authGuard] },
  { path: 'intranet/admin',      component: IntranetAdmin,      canActivate: [adminGuard] },
];
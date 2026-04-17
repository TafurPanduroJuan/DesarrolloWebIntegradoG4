import { Routes } from '@angular/router';
import { Home } from './features/home/components/home/home';
import { Contact } from './features/contact/contact';
import { Catalog } from './features/catalog/catalog';
import { About } from './features/about/about';
import { Login } from './features/auth/components/login/login';
import { Register } from './features/auth/components/register/register';
import { Dashboard } from './features/dashboard/dashboard';
import { authGuard } from './shared/guards/auth.guard';

export const routes: Routes = [
  { path: '',          component: Home },
  { path: 'form',      component: Contact },
  { path: 'catalog',   component: Catalog },
  { path: 'about',     component: About },
  { path: 'login',     component: Login },
  { path: 'register',  component: Register },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
];
import { Routes } from '@angular/router';
import { Home } from './features/home/components/home/home';
import { Contact } from './features/contact/contact';
import { Catalog } from './features/catalog/catalog';
import { About } from './features/about/about';

export const routes: Routes = [
  { path: '', component: Home },
  {path: 'form', component:Contact },
  {path: 'catalog', component: Catalog},
  {path: 'about', component: About}
];
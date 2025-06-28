import { Routes } from '@angular/router';
import {Dashboard} from './admin/dashboard/dashboard';
import {Notfoundcomponent} from './share/notfoundcomponent/notfoundcomponent';
import {Login} from './login/login';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full'},
  {
    path: 'login',
    component: Login
  },
  {
    path: 'admin/dashboard',
    component: Dashboard
  },
  {
    path: '**',
    component: Notfoundcomponent
  }
];

import { Routes } from '@angular/router';
import {Dashboard} from './admin/dashboard/dashboard';
import {Notfoundcomponent} from './share/notfoundcomponent/notfoundcomponent';

export const routes: Routes = [
  {
    path: 'admin/dashboard',
    component: Dashboard
  },
  {
    path: '**',
    component: Notfoundcomponent
  }
];

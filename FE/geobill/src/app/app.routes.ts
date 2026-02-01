import { Routes } from '@angular/router';
import {Dashboard} from './admin/dashboard/dashboard';
import {StaffDashboard} from './staff/dashboard/dashboard';
import {UserDashboard} from './users/dashboard/dashboard';
import {Notfoundcomponent} from './share/notfoundcomponent/notfoundcomponent';
import {Login} from './login/login';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';
import {HostingService} from './users/hosting-service/hosting-service';
import {Producttype} from './admin/masterdata/producttype/producttype';
import {Productcomponent} from './admin/masterdata/productcomponent/productcomponent';
import {Usercomponent} from './admin/masterdata/usercomponent/usercomponent';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full'},
  {
    path: 'login',
    component: Login
  },
  {
    path: 'admin/dashboard',
    component: Dashboard,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/product_type',
    component: Producttype,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/product',
    component: Productcomponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/users',
    component: Usercomponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'staff/dashboard',
    component: StaffDashboard,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['STAFF'] }
  },
  {
    path: 'users/dashboard',
    component: UserDashboard,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['USER'] }
  },
  {
    path: 'users/services',
    component: HostingService,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['USER'] }
  },
  {
    path: '**',
    component: Notfoundcomponent
  }
];

/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import { inject, PLATFORM_ID } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { Localstorageservice } from '../utils/localstorage/localstorageservice';
import { isPlatformBrowser } from '@angular/common';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state) => {
  const platformId = inject(PLATFORM_ID);

  // During SSR, allow the route to pass (will be handled on client side)
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  const localStorageService = inject(Localstorageservice);
  const router = inject(Router);

  const userRole = localStorageService.getItem('role');
  const allowedRoles = route.data['roles'] as Array<string>;

  if (!userRole) {
    router.navigate(['/login']);
    return false;
  }

  if (allowedRoles && allowedRoles.length > 0) {
    const hasRole = allowedRoles.some(role => role.toUpperCase() === userRole.toUpperCase());
    if (!hasRole) {
      const roleLower = userRole.toLowerCase();
      if (roleLower === 'admin') {
        router.navigate(['/admin/dashboard']);
      } else if (roleLower === 'staff') {
        router.navigate(['/staff/dashboard']);
      } else if (roleLower === 'user') {
        router.navigate(['/users/dashboard']);
      } else {
        router.navigate(['/login']);
      }
      return false;
    }
  }

  return true;
};


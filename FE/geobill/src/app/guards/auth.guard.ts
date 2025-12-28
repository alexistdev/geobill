import { inject, PLATFORM_ID } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { Localstorageservice } from '../utils/localstorage/localstorageservice';
import { isPlatformBrowser } from '@angular/common';

export const authGuard: CanActivateFn = (route, state) => {
  const platformId = inject(PLATFORM_ID);
  
  // During SSR, allow the route to pass (will be handled on client side)
  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  const localStorageService = inject(Localstorageservice);
  const router = inject(Router);

  const userId = localStorageService.getItem('userId');
  const role = localStorageService.getItem('role');

  if (userId && role) {
    return true;
  }

  // Redirect to login if not authenticated
  router.navigate(['/login']);
  return false;
};


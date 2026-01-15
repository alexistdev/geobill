import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable, inject, PLATFORM_ID} from '@angular/core';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {Router} from '@angular/router';
import {CredentialEncryptedService} from '../utils/auth/credential-encrypted.service';
import {Localstorageservice} from '../utils/localstorage/localstorageservice';
import {isPlatformBrowser} from '@angular/common';

/**
 * HTTP Interceptor untuk menambahkan Basic Auth header
 * menggunakan encrypted credentials dari CredentialEncryptedService
 */
@Injectable()
export class CredentialEncryptedInterceptor implements HttpInterceptor {
  private platformId = inject(PLATFORM_ID);
  private router = inject(Router);
  private credentialService = inject(CredentialEncryptedService);
  private localStorageService = inject(Localstorageservice);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const isBrowser = isPlatformBrowser(this.platformId);

    if (req.url.includes('/auth/login') || req.url.includes('/auth/registration')) {
      return next.handle(req);
    }

    if (!isBrowser) {
      return next.handle(req);
    }

    const credentials = this.credentialService.getCredentials();

    if (this.credentialService.isSessionExpired()) {
      this.handleSessionExpired();
      return throwError(() => new Error('Session expired'));
    }

    if (!credentials || !credentials.email || !credentials.password) {
      this.handleSessionExpired();
      return throwError(() => new Error('No credentials found'));
    }

    this.credentialService.refreshSession();

    const authToken = btoa(`${credentials.email}:${credentials.password}`);

    const authReq = req.clone({
      setHeaders: {
        Authorization: `Basic ${authToken}`
      }
    });

    return next.handle(authReq).pipe(
      catchError((error) => {
        if (error.status === 401) {
          this.handleSessionExpired();
        }
        return throwError(() => error);
      })
    );
  }

  /**
   * Handle session expired atau unauthorized
   */
  private handleSessionExpired(): void {
    // Clear credentials
    this.credentialService.clearCredentials();

    // Clear other session data
    this.localStorageService.clearItem();

    // Redirect ke login page
    this.router.navigate(['/login']);
  }
}

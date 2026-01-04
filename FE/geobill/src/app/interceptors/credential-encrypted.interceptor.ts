import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { CredentialEncryptedService } from '../utils/auth/credential-encrypted.service';
import { Localstorageservice } from '../utils/localstorage/localstorageservice';

/**
 * HTTP Interceptor untuk menambahkan Basic Auth header
 * menggunakan encrypted credentials dari CredentialEncryptedService
 */
@Injectable()
export class CredentialEncryptedInterceptor implements HttpInterceptor {
    private router = inject(Router);
    private credentialService = inject(CredentialEncryptedService);
    private localStorageService = inject(Localstorageservice);

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Skip auth header untuk login endpoint
        if (req.url.includes('/auth/login') || req.url.includes('/auth/registration')) {
            return next.handle(req);
        }

        // Check session expired
        if (this.credentialService.isSessionExpired()) {
            this.handleSessionExpired();
            return throwError(() => new Error('Session expired'));
        }

        // Get credentials dari encrypted storage
        const credentials = this.credentialService.getCredentials();

        if (!credentials || !credentials.email || !credentials.password) {
            // Tidak ada credentials, redirect ke login
            this.handleSessionExpired();
            return throwError(() => new Error('No credentials found'));
        }

        // Refresh session timestamp pada setiap request
        this.credentialService.refreshSession();

        // Create Basic Auth token
        const authToken = btoa(`${credentials.email}:${credentials.password}`);

        // Clone request dan tambahkan Authorization header
        const authReq = req.clone({
            setHeaders: {
                Authorization: `Basic ${authToken}`
            }
        });

        return next.handle(authReq).pipe(
            catchError((error) => {
                // Handle 401 Unauthorized - session invalid atau credentials salah
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

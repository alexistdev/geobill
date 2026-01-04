import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { CredentialEncryptedService } from './credential-encrypted.service';
import { Localstorageservice } from '../localstorage/localstorageservice';

/**
 * Service untuk handle logout functionality
 */
@Injectable({
    providedIn: 'root'
})
export class LogoutService {

    constructor(
        private router: Router,
        private credentialService: CredentialEncryptedService,
        private localStorageService: Localstorageservice
    ) { }

    /**
     * Logout user dan clear semua data
     */
    logout(): void {
        // Clear encrypted credentials
        this.credentialService.clearCredentials();

        // Clear session storage
        this.localStorageService.clearItem();

        // Redirect to login page
        this.router.navigate(['/login']);
    }
}

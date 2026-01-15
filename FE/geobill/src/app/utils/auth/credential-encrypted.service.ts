import {Injectable, PLATFORM_ID, inject} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';
import * as CryptoJS from 'crypto-js';

/**
 * Service untuk menyimpan kredensial dengan enkripsi AES-256
 */
@Injectable({
  providedIn: 'root'
})
export class CredentialEncryptedService {
  private platformId = inject(PLATFORM_ID);
  private readonly CREDENTIALS_KEY = '_auth_cred';
  private readonly SESSION_KEY = '_auth_session';
  private readonly ENCRYPTION_KEY = '_auth_key';
  private readonly SESSION_TIMEOUT = 60 * 60 * 1000; // 30 menit dalam milliseconds

  constructor() {
    this.initializeEncryptionKey();
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  /**
   * Generate unique encryption key untuk session ini
   * Key disimpan di sessionStorage dan akan hilang saat tab ditutup
   */
  private initializeEncryptionKey(): void {
    if (!this.isBrowser()) {
      return;
    }

    let key = sessionStorage.getItem(this.ENCRYPTION_KEY);
    if (!key) {
      // Generate random key untuk session ini
      key = CryptoJS.lib.WordArray.random(256 / 8).toString();
      sessionStorage.setItem(this.ENCRYPTION_KEY, key);
    }
  }

  /**
   * Get encryption key dari sessionStorage
   */
  private getEncryptionKey(): string {
    if (!this.isBrowser()) {
      return '';
    }
    return sessionStorage.getItem(this.ENCRYPTION_KEY) || '';
  }

  /**
   * Encrypt data menggunakan AES-256
   */
  private encrypt(data: string): string {
    const key = this.getEncryptionKey();
    if (!key) {
      return '';
    }
    return CryptoJS.AES.encrypt(data, key).toString();
  }

  /**
   * Decrypt data menggunakan AES-256
   */
  private decrypt(encryptedData: string): string {
    const key = this.getEncryptionKey();
    if (!key) {
      return '';
    }
    try {
      const bytes = CryptoJS.AES.decrypt(encryptedData, key);
      return bytes.toString(CryptoJS.enc.Utf8);
    } catch (e) {
      console.error('Decryption failed:', e);
      return '';
    }
  }

  /**
   * Simpan credentials dengan enkripsi
   */
  setCredentials(email: string, password: string): void {
    if (!this.isBrowser()) {
      return;
    }

    const credentials = {
      email: email,
      password: password
    };

    const encryptedData = this.encrypt(JSON.stringify(credentials));
    sessionStorage.setItem(this.CREDENTIALS_KEY, encryptedData);

    // Set session timestamp
    const sessionData = {
      timestamp: Date.now(),
      timeout: this.SESSION_TIMEOUT
    };
    sessionStorage.setItem(this.SESSION_KEY, JSON.stringify(sessionData));
  }

  /**
   * Ambil credentials dan decrypt
   * Returns null jika session expired atau tidak ada credentials
   */
  getCredentials(): { email: string; password: string } | null {
    if (!this.isBrowser()) {
      console.log('Browser not detected, skipping credentials...');
      return null;
    }

    // Check session timeout
    if (this.isSessionExpired()) {
      console.log('Session expired, clearing credentials...');
      this.clearCredentials();
      return null;
    }

    const encryptedData = sessionStorage.getItem(this.CREDENTIALS_KEY);
    if (!encryptedData) {
      return null;
    }

    const decryptedData = this.decrypt(encryptedData);
    if (!decryptedData) {
      return null;
    }

    try {
      return JSON.parse(decryptedData);
    } catch (e) {
      console.error('Failed to parse credentials:', e);
      return null;
    }
  }

  /**
   * Check apakah session sudah expired
   */
  isSessionExpired(): boolean {
    if (!this.isBrowser()) {
      return false;
    }

    const sessionDataStr = sessionStorage.getItem(this.SESSION_KEY);
    if (!sessionDataStr) {
      return true;
    }

    try {
      const sessionData = JSON.parse(sessionDataStr);
      const currentTime = Date.now();
      const elapsedTime = currentTime - sessionData.timestamp;
      return elapsedTime > sessionData.timeout;
    } catch (e) {
      return true;
    }
  }

  /**
   * Update session timestamp (untuk extend session)
   */
  refreshSession(): void {
    if (!this.isBrowser()) {
      return;
    }

    const sessionData = {
      timestamp: Date.now(),
      timeout: this.SESSION_TIMEOUT
    };
    sessionStorage.setItem(this.SESSION_KEY, JSON.stringify(sessionData));
  }

  /**
   * Clear credentials dari sessionStorage
   */
  clearCredentials(): void {
    if (!this.isBrowser()) {
      return;
    }

    sessionStorage.removeItem(this.CREDENTIALS_KEY);
    sessionStorage.removeItem(this.SESSION_KEY);
    sessionStorage.removeItem(this.ENCRYPTION_KEY);
  }

  /**
   * Get session timeout dalam menit
   */
  getSessionTimeoutMinutes(): number {
    return this.SESSION_TIMEOUT / (60 * 1000);
  }

  /**
   * Set custom session timeout (dalam menit)
   * Berguna untuk testing atau custom requirements
   */
  setSessionTimeout(minutes: number): void {
    if (!this.isBrowser()) {
      return;
    }

    const sessionDataStr = sessionStorage.getItem(this.SESSION_KEY);
    if (sessionDataStr) {
      try {
        const sessionData = JSON.parse(sessionDataStr);
        sessionData.timeout = minutes * 60 * 1000;
        sessionStorage.setItem(this.SESSION_KEY, JSON.stringify(sessionData));
      } catch (e) {
        console.error('Failed to update session timeout:', e);
      }
    }
  }
}

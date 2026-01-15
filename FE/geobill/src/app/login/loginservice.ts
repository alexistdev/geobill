import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Localstorageservice } from '../utils/localstorage/localstorageservice';
import { CredentialEncryptedService } from '../utils/auth/credential-encrypted.service';
import { catchError, map, Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Loginservice {

  constructor(
    private http: HttpClient,
    private localStorageService: Localstorageservice,
    private credentialService: CredentialEncryptedService
  ) { }

  AuthLogin(userName: string, userPw: string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    return this.http.post<any>('http://localhost:8082/api/v1/auth/login', { 'email': userName, 'password': userPw }, {
      headers: headers,
      withCredentials: true
    }).pipe(
      map(res => {
        if (!res || !res.payload) {
          return { success: false };
        }
        const data = res.payload;

        // Simpan credentials dengan enkripsi AES-256
        this.credentialService.setCredentials(userName, userPw);

        // Simpan data user lainnya (bukan credentials) di sessionStorage
        this.localStorageService.setItem("userId", data.id);
        this.localStorageService.setItem("role", data.role);

        // Store menus as object
        if (data.menus) {
          this.localStorageService.setItemAsObject("menus", data.menus);
        }

        return {
          success: true,
          role: data.role,
          payload: {
            ...data,
            homeUrl: data.homeUrl
          }
        };
      }),
      catchError((err) => {
        console.error(err);
        return of({ success: false });
      })
    );
  }
}

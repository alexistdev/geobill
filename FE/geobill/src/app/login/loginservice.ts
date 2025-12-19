import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Localstorageservice} from '../utils/localstorage/localstorageservice';
import {catchError, map, Observable, Observer, of} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Loginservice {

  constructor(
    private http:HttpClient,
    private localStorageService: Localstorageservice
  ) { }

  AuthLogin(userName: string, userPw : string): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    return this.http.post<any>('http://localhost:8082/v1/api/auth/login', {'email': userName , 'password' : userPw},{ headers: headers,
      withCredentials: true }).pipe(
        map(res => {
          if(!res || !res.payload) {
            return { success : false };
          }
          const data = res.payload;
          this.localStorageService.setItem("userId", data.id);
          this.localStorageService.setItem("role", data.role);
          this.localStorageService.setItem("email", data.email);
          this.localStorageService.setItem("keyPs", userPw);
          return { success: true, role: data.role };
        }),
        catchError((err) => {
        console.error(err);
        return of({ success: false });
      })
    );
    // return new Observable((observer: Observer<any>) => {
    //   this.http.post<any>('http://localhost:8082/v1/api/auth/login', {'email': userName , 'password' : userPw},{ headers: headers,
    //     withCredentials: true })
    //     .subscribe({
    //       next: (res) => {
    //         if(!res || !res.payload){
    //           observer.next({ success: false });
    //           return;
    //         }
    //         let stringifiedData = JSON.stringify(res);
    //         let parsedJson = JSON.parse(stringifiedData);
    //         let data = parsedJson.payload;
    //         this.localStorageService.setItem("userId",data.id);
    //         this.localStorageService.setItem("role",data.role);
    //         this.localStorageService.setItem("email",data.email);
    //         this.localStorageService.setItem("keyPs", userPw);
    //         observer.next({ success: true, role: data.role });
    //       },
    //       error: (e) => {
    //         observer.next({ success: false });
    //         console.log(e)
    //       },
    //     });
    // });
  }
}

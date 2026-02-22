/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Apiresponse} from '../../../share/response/apiresponse';
import {Usermodel} from './usermodel.model';
import {Userregisterrequest} from './request/userregisterrequest.model';

@Injectable({
  providedIn: 'root'
})
export class Userservice {
  private apiUrl = '/api/v1/users';
  private apiValidateEmail = '/api/v1/auth/validate';
  private apiAuth = '/api/v1/auth';

  constructor(private http: HttpClient) { }

  getUsers(page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<Usermodel>> {
    return this.http.get<Apiresponse<Usermodel>>(
      `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  getUsersByFilter(keyword: string, page: number, size: number, sortBy: string, direction: string):Observable<Apiresponse<Usermodel>> {
    return this.http.get<Apiresponse<Usermodel>>(
      `${this.apiUrl}/search?filter=${keyword}&page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  validateEmail(email: string): Observable<Apiresponse<any>> {
    return this.http.post<Apiresponse<any>>(`${this.apiAuth}/validate`, { email }, { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) });
  }

  saveUser(userRequest:Userregisterrequest): Observable<Apiresponse<Usermodel>>{
    return this.http.post<Apiresponse<Usermodel>>(`${this.apiAuth}/register`, userRequest, { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) });
  }


}


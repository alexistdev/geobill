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

@Injectable({
  providedIn: 'root'
})
export class Userservice {
  private apiUrl = '/api/v1/users';

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

}


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
import {Apiresponse} from '../../../../share/response/apiresponse';
import {UserDetailModel} from './userdetailmodel.model';
import {Userdetailrequest} from './userdetailrequest.model';

@Injectable({
  providedIn: 'root'
})
export class Userdetailservice {
  private apiUrl = '/api/v1/users';

  constructor(private http:HttpClient) { }

  getUsersDetail(id: string): Observable<Apiresponse<UserDetailModel>> {
    return this.http.get<Apiresponse<UserDetailModel>>(`${this.apiUrl}/${id}` ,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  updateUsersDetail(id: string, user: Userdetailrequest): Observable<Apiresponse<UserDetailModel>> {
    return this.http.patch<Apiresponse<UserDetailModel>>(`${this.apiUrl}/${id}`, user,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }
}

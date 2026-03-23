/*
 * Copyright (c) 2026.
 * Project : GeoBill
 * Author : Alexsander Hendra Wijaya
 * Github : https://github.com/alexistdev
 * Email : alexistdev@gmail.com
 */

import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Localstorageservice} from '../../../utils/localstorage/localstorageservice';
import {Observable} from 'rxjs';
import {Apiresponse} from '../../../share/response/apiresponse';
import {InvoiceResponseModel} from '../model/response/invoice-response.model';

@Injectable({
  providedIn: 'root'
})
export class InvoiceServiceApi{
  private apiUrl = '/api/v1/invoice';
  private userId: String = '';

  constructor(private http: HttpClient, private localStorage: Localstorageservice) {}

  getAllInvoices(page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<InvoiceResponseModel>>  {
    this.userId = this.localStorage.getItem("userId") ?? '';
    return this.http.get<Apiresponse<InvoiceResponseModel>>(
      `${this.apiUrl}/${this.userId}/data?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }
}

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
import {Api_base_response_single} from '../../../share/response/apiresponsesingle';
import {InvoiceResponseModel} from '../model/response/invoice-response.model'; // Keep this import as it's used
import {Apiresponse} from '../../../share/response/apiresponse'; // Keep this import as it's used

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

  getInvoiceDetail(invoiceId: string): Observable<Api_base_response_single<InvoiceResponseModel>> {
    return this.http.get<Api_base_response_single<InvoiceResponseModel>>(
      `${this.apiUrl}/${invoiceId}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }
}

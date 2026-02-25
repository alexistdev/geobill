/*
 * Copyright (c) 2026.
 * Project : GeoBill
 * Author : Alexsander Hendra Wijaya
 * Github : https://github.com/alexistdev
 * Email : alexistdev@gmail.com
 */

import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Apiresponse} from '../../share/response/apiresponse';
import {Producttypemodel} from '../../admin/masterdata/producttype/producttypemodel.model';

@Injectable({
  providedIn: 'root'
})
export class Orderhostingservice {

  private apiUrl = '/api/v1/producttypes';

  constructor(private http: HttpClient) { }

  getProductType(page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<Producttypemodel>> {
    return this.http.get<Apiresponse<Producttypemodel>>(
      `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }
}

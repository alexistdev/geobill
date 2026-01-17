/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Apiresponse } from '../../../share/response/apiresponse';
import { Producttypemodel } from './producttypemodel.model';
import {Producttyperequest} from './producttyperequest.model';
import {Producttype} from './producttype';

@Injectable({
  providedIn: 'root'
})
export class Producttypeservice {

  private apiUrl = '/api/v1/producttypes';

  constructor(private http: HttpClient) { }

  getProductType(page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<Producttypemodel>> {
    return this.http.get<Apiresponse<Producttypemodel>>(
      `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  getProductTypeByFilter(keyword: string, page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<Producttypemodel>> {
    return this.http.get<Apiresponse<Producttypemodel>>(
      `${this.apiUrl}/search?filter=${keyword}&page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  saveProductType(producttype: Producttyperequest): Observable<Apiresponse<Producttype>> {
    return this.http.post<Apiresponse<Producttype>>(
        `${this.apiUrl}`, producttype,
            { headers: new HttpHeaders({ 'Content-Type': 'application/json' })
      });
  }

  updateProductType(request: Producttyperequest): Observable<Apiresponse<Producttype>> {
    return this.http.patch<Apiresponse<Producttype>>(
      `${this.apiUrl}`, request,
        { headers: new HttpHeaders({ 'Content-Type': 'application/json' })
      });
  }

  deleteProductType(id: string): Observable<Apiresponse<Producttype>> {
    return this.http.delete<Apiresponse<Producttype>>(
      `${this.apiUrl}/${id}`,
    )
  }
}

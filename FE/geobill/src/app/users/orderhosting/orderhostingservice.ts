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
import {Productmodel} from '../../admin/masterdata/productcomponent/productmodel.model';

@Injectable({
  providedIn: 'root'
})
export class Orderhostingservice {

  private apiUrl = '/api/v1/producttypes';

  private apiUrlProduct = '/api/v1/products';

  constructor(private http: HttpClient) { }

  getProductType(page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<Producttypemodel>> {
    return this.http.get<Apiresponse<Producttypemodel>>(
      `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  getProductByProductTypeId(id: string, page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<Productmodel>> {
    return this.http.get<Apiresponse<Productmodel>>(
      `${this.apiUrlProduct}/search-by-type?id=${id}&page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  getProductById(id: string): Observable<Apiresponse<Productmodel>> {
    return this.http.get<Apiresponse<Productmodel>>(
      `${this.apiUrlProduct}/${id}`,
    )
  }
}

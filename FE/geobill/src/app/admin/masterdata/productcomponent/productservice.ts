import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Apiresponse} from '../../../share/response/apiresponse';
import {Productmodel} from './productmodel.model';
import {Producttypemodel} from '../producttype/producttypemodel.model';
import {Productrequest} from './productrequest.model';
import {Producttyperequest} from '../producttype/producttyperequest.model';

@Injectable({
  providedIn: 'root',
})
export class Productservice {

  private apiUrl = '/api/v1/products';


  constructor(private http: HttpClient) {}

  getProduct(page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<Productmodel>>  {
    return this.http.get<Apiresponse<Productmodel>>(
      `${this.apiUrl}?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  getProductByFilter(keyword: string, page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<Productmodel>> {
    return this.http.get<Apiresponse<Productmodel>>(
      `${this.apiUrl}/search?filter=${keyword}&page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }

  saveProduct(product: Productrequest): Observable<Apiresponse<Productmodel>> {
    return this.http.post<Apiresponse<Productmodel>>(
      `${this.apiUrl}`, product,
      {
        headers: new HttpHeaders({'Content-Type': 'application/json'})
      });
  }

  updateProduct(request: Producttyperequest): Observable<Apiresponse<Productmodel>> {
    return this.http.patch<Apiresponse<Productmodel>>(
      `${this.apiUrl}`, request,
      { headers: new HttpHeaders({'Content-Type': 'application/json'}) }
    );
  }

  deleteProduct(id: string): Observable<Apiresponse<Productmodel>> {
    return this.http.delete<Apiresponse<Productmodel>>(
      `${this.apiUrl}/${id}`,
    )
  }
}

import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Apiresponse} from '../../../share/response/apiresponse';
import {HostingResponseModel} from '../model/response/hosting-response.model';
import {Localstorageservice} from '../../../utils/localstorage/localstorageservice';

@Injectable({
  providedIn: 'root',
})
export class HostingServiceApi {
  private apiUrl = '/api/v1/hosting';
  private userId: String = '';

  constructor(private http: HttpClient, private localStorage: Localstorageservice) {}

  getAllHostingService(page: number, size: number, sortBy: string, direction: string): Observable<Apiresponse<HostingResponseModel>>  {
    this.userId = this.localStorage.getItem("userId") ?? '';
    return this.http.get<Apiresponse<HostingResponseModel>>(
      `${this.apiUrl}/${this.userId}/hostings?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`,
      { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) }
    );
  }
}

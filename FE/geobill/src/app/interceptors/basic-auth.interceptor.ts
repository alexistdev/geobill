import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Localstorageservice} from '../utils/localstorage/localstorageservice';

@Injectable()
export class BasicAuthInterceptor implements HttpInterceptor {
  constructor(private localStorageService: Localstorageservice) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const username: string = this.localStorageService.getItem('email') || '';
    const password: string = this.localStorageService.getItem('keyPs') || '';

    const decodeUsername: string = this.localStorageService.decode(username);
    const decodePassword: string = this.localStorageService.decode(password);

    const authToken: string = btoa(`${decodeUsername}:${decodePassword}`);

    const authReq = req.clone({
      setHeaders: {
        Authorization: `Basic ${authToken}`
      }
    });

    return next.handle(authReq);
  }

}

import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Localstorageservice} from '../utils/localstorage/localstorageservice';
import {CredentialEncryptedService} from '../utils/auth/credential-encrypted.service';

@Injectable()
export class BasicAuthInterceptor implements HttpInterceptor {

  constructor(private credentialEncryptedService: CredentialEncryptedService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    const credentials = this.credentialEncryptedService.getCredentials();

    if(!credentials){
      return next.handle(req);
    }

    const authToken: string = btoa(`${credentials.email}:${credentials.password}`);

    const authReq = req.clone({
      setHeaders: {
        Authorization: `Basic ${authToken}`
      }
    });

    return next.handle(authReq);
  }

}

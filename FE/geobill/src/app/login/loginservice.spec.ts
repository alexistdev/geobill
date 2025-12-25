import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { Loginservice } from './loginservice';
import { Localstorageservice } from '../utils/localstorage/localstorageservice';
import { of } from 'rxjs';

describe('Loginservice', () => {
  let service: Loginservice;
  let httpMock: HttpTestingController;
  let localStorageSpy: jasmine.SpyObj<Localstorageservice>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('Localstorageservice', ['setItem']);

    TestBed.configureTestingModule({
      providers: [
        Loginservice,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Localstorageservice, useValue: spy }
      ]
    });
    service = TestBed.inject(Loginservice);
    httpMock = TestBed.inject(HttpTestingController);
    localStorageSpy = TestBed.inject(Localstorageservice) as jasmine.SpyObj<Localstorageservice>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login successfully and store user data', () => {
    const mockResponse = {
      payload: {
        id: '123',
        role: 'admin',
        email: 'test@example.com'
      }
    };
    const userName = 'test@example.com';
    const userPw = 'password';

    service.AuthLogin(userName, userPw).subscribe(res => {
      expect(res.success).toBeTrue();
      expect(res.role).toBe('admin');
      expect(localStorageSpy.setItem).toHaveBeenCalledWith('userId', '123');
      expect(localStorageSpy.setItem).toHaveBeenCalledWith('role', 'admin');
      expect(localStorageSpy.setItem).toHaveBeenCalledWith('email', 'test@example.com');
      expect(localStorageSpy.setItem).toHaveBeenCalledWith('keyPs', userPw);
    });

    const req = httpMock.expectOne('http://localhost:8082/v1/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should handle login failure (no payload)', () => {
    const mockResponse = { payload: null };
    const userName = 'test@example.com';
    const userPw = 'wrongpassword';

    service.AuthLogin(userName, userPw).subscribe(res => {
      expect(res.success).toBeFalse();
    });

    const req = httpMock.expectOne('http://localhost:8082/v1/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should handle http error', () => {
    const userName = 'test@example.com';
    const userPw = 'password';

    service.AuthLogin(userName, userPw).subscribe(res => {
      expect(res.success).toBeFalse();
    });

    const req = httpMock.expectOne('http://localhost:8082/v1/api/auth/login');
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });
});

import { TestBed } from '@angular/core/testing';

import { Localstorageservice } from './localstorageservice';

describe('Localstorageservice', () => {
  let service: Localstorageservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Localstorageservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

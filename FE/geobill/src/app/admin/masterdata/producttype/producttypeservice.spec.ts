import { TestBed } from '@angular/core/testing';

import { Producttypeservice } from './producttypeservice';

describe('Producttypeservice', () => {
  let service: Producttypeservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Producttypeservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

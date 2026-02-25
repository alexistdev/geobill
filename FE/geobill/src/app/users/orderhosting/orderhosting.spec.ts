/*
 * Copyright (c) 2026.
 * Project : GeoBill
 * Author : Alexsander Hendra Wijaya
 * Github : https://github.com/alexistdev
 * Email : alexistdev@gmail.com
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Orderhosting } from './orderhosting';

describe('Orderhosting', () => {
  let component: Orderhosting;
  let fixture: ComponentFixture<Orderhosting>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Orderhosting]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Orderhosting);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Producttypemodal } from './producttypemodal';

describe('Producttypemodal', () => {
  let component: Producttypemodal;
  let fixture: ComponentFixture<Producttypemodal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Producttypemodal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Producttypemodal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

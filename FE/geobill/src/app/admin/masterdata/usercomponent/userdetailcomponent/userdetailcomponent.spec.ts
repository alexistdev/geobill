/*
 * Copyright (c) 2026.
 * Project: GeoBill
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Userdetailcomponent } from './userdetailcomponent';

describe('Userdetailcomponent', () => {
  let component: Userdetailcomponent;
  let fixture: ComponentFixture<Userdetailcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Userdetailcomponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Userdetailcomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

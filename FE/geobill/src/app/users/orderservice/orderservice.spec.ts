import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Orderservice } from './orderservice';

describe('Orderservice', () => {
  let component: Orderservice;
  let fixture: ComponentFixture<Orderservice>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Orderservice]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Orderservice);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

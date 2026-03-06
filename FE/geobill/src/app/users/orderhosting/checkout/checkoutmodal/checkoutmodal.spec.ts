import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Checkoutmodal } from './checkoutmodal';

describe('Checkoutmodal', () => {
  let component: Checkoutmodal;
  let fixture: ComponentFixture<Checkoutmodal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Checkoutmodal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Checkoutmodal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

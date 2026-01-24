import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Productmodal } from './productmodal';

describe('Productmodal', () => {
  let component: Productmodal;
  let fixture: ComponentFixture<Productmodal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Productmodal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Productmodal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

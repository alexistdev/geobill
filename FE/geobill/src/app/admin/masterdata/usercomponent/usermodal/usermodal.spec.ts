import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Usermodal } from './usermodal';

describe('Usermodal', () => {
  let component: Usermodal;
  let fixture: ComponentFixture<Usermodal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Usermodal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Usermodal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

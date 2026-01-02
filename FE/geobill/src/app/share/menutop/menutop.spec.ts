import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Menutop } from './menutop';

describe('Menutop', () => {
  let component: Menutop;
  let fixture: ComponentFixture<Menutop>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Menutop]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Menutop);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

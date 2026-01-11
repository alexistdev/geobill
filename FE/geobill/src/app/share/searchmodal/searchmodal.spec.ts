import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Searchmodal } from './searchmodal';

describe('Searchmodal', () => {
  let component: Searchmodal;
  let fixture: ComponentFixture<Searchmodal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Searchmodal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Searchmodal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

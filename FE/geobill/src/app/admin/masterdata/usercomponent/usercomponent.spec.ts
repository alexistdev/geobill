import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Usercomponent } from './usercomponent';

describe('Usercomponent', () => {
  let component: Usercomponent;
  let fixture: ComponentFixture<Usercomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Usercomponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Usercomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

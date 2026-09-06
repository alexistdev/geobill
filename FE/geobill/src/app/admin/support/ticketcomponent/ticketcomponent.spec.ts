import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Ticketcomponent } from './ticketcomponent';

describe('Ticketcomponent', () => {
  let component: Ticketcomponent;
  let fixture: ComponentFixture<Ticketcomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Ticketcomponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Ticketcomponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

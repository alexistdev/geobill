import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HostingService } from './hosting-service';

describe('HostingService', () => {
  let component: HostingService;
  let fixture: ComponentFixture<HostingService>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HostingService]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HostingService);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BadgesWindow } from './badges-window';

describe('BadgesWindow', () => {
  let component: BadgesWindow;
  let fixture: ComponentFixture<BadgesWindow>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BadgesWindow]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BadgesWindow);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

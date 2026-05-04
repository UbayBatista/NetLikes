import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ForoSso } from './foro-sso';

describe('ForoSso', () => {
  let component: ForoSso;
  let fixture: ComponentFixture<ForoSso>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForoSso],
    }).compileComponents();

    fixture = TestBed.createComponent(ForoSso);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

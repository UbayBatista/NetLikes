import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordVerifyModalComponent } from './password-ask-modal';

describe('PasswordVerifyModalComponent', () => {
  let component: PasswordVerifyModalComponent;
  let fixture: ComponentFixture<PasswordVerifyModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordVerifyModalComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PasswordVerifyModalComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

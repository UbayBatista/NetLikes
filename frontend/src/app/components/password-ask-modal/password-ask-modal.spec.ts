import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordAskModal } from './password-ask-modal';

describe('PasswordAskModal', () => {
  let component: PasswordAskModal;
  let fixture: ComponentFixture<PasswordAskModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordAskModal],
    }).compileComponents();

    fixture = TestBed.createComponent(PasswordAskModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

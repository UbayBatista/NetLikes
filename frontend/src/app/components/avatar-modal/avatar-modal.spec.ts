import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AvatarModal } from './avatar-modal';

describe('AvatarModal', () => {
  let component: AvatarModal;
  let fixture: ComponentFixture<AvatarModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AvatarModal],
    }).compileComponents();

    fixture = TestBed.createComponent(AvatarModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

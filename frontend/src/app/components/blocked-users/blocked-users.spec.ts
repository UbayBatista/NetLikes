import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BlockedUsersModalComponent } from './blocked-users';

describe('BlockedUsersModalComponent', () => {
  let component: BlockedUsersModalComponent;
  let fixture: ComponentFixture<BlockedUsersModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BlockedUsersModalComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BlockedUsersModalComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ForumSsoComponent } from './forum-sso';
import { provideRouter } from '@angular/router';

describe('ForumSsoComponent', () => {
  let component: ForumSsoComponent;
  let fixture: ComponentFixture<ForumSsoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForumSsoComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(ForumSsoComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Social } from './social';
import { provideRouter } from '@angular/router';

describe('Social', () => {
  let component: Social;
  let fixture: ComponentFixture<Social>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Social],
      providers: [provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(Social);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendPanel } from './recommend-panel';

describe('RecommendPanel', () => {
  let component: RecommendPanel;
  let fixture: ComponentFixture<RecommendPanel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecommendPanel],
    }).compileComponents();

    fixture = TestBed.createComponent(RecommendPanel);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

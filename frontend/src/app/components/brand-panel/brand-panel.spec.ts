import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrandPanel } from './brand-panel';
import { RouterModule } from '@angular/router';

describe('BrandPanel', () => {
  let component: BrandPanel;
  let fixture: ComponentFixture<BrandPanel>;

  beforeEach(async () => {
  await TestBed.configureTestingModule({
    imports: [BrandPanel, RouterModule.forRoot([])],
  }).compileComponents();

  fixture = TestBed.createComponent(BrandPanel);
  component = fixture.componentInstance;
  fixture.detectChanges();

});

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchBarComponent } from './search-bar';

describe('SearchBar', () => {
  let component: SearchBarComponent;
  let fixture: ComponentFixture<SearchBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchBarComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SearchBarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should emit the search value when onSearchChange is called', () => {
    const emitSpy = vi.spyOn(component.searchEvent, 'emit');
    
    component.onSearchChange('Batman');
    
    expect(emitSpy).toHaveBeenCalledWith('Batman');
  });
});

import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ProfileBody } from './profile-components'; 
import { SimpleChange } from '@angular/core';

describe('ProfileBody', () => {
  let component: ProfileBody;
  let fixture: ComponentFixture<ProfileBody>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileBody]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileBody);
    component = fixture.componentInstance;
    component.title = 'Mis Películas';
  });

  it('should create the component', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('Lifecycle & Inputs', () => {
    it('should render the title correctly', () => {
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const titleEl = compiled.querySelector('.section-title');
      expect(titleEl?.textContent?.trim()).toBe('Mis Películas');
    });

    it('should update isVisibleLocal when ngOnChanges is called with a new isVisible value', () => {
      fixture.detectChanges();
      component.isVisibleLocal = true;
      component.isVisible = false; 

      component.ngOnChanges({
        isVisible: new SimpleChange(true, false, false)
      });

      expect(component.isVisibleLocal).toBe(false);
    });
  });
});
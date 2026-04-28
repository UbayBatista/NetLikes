import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Step4 } from './step4';
import { GenreService } from '../../../services/genre.service';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('Step4', () => {
  let component: Step4;
  let fixture: ComponentFixture<Step4>;
  let genreService: GenreService;

  const mockGenres = [
    { id: 1, name: 'Acción' },
    { id: 2, name: 'Comedia' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Step4],
      providers: [
        GenreService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Step4);
    component = fixture.componentInstance;
    genreService = TestBed.inject(GenreService);

    vi.spyOn(genreService, 'getAllGenres').mockReturnValue(of(mockGenres));

    fixture.detectChanges(); 
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('debería cargar los géneros y añadir la propiedad selected: false', () => {
    expect(component.generos().length).toBe(2);
    expect(component.generos()[0].selected).toBe(false);
  });

  it('should toggle a genre selection', () => {
    component.toggleGenero(0);
    expect(component.generos()[0].selected).toBe(true);
    
    component.toggleGenero(0);
    expect(component.generos()[0].selected).toBe(false);
  });

  it('should calculate totalSelected correctly', () => {
    expect(component.totalSelected).toBe(0);
    component.toggleGenero(0);
    component.toggleGenero(1);
    expect(component.totalSelected).toBe(2);
  });

  it('debería emitir los IDs seleccionados al llamar a handleEnd', () => {
    const emitSpy = vi.spyOn(component.toEnd, 'emit');
    component.toggleGenero(0); 
    component.handleEnd();
    expect(emitSpy).toHaveBeenCalledWith([1]);
  });
});
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Step1 } from './step1';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';

describe('Step1', () => {
  let component: Step1;
  let fixture: ComponentFixture<Step1>;
  let mockAuthService: any;
  let mockRouter: any;

  beforeEach(async () => {
    mockAuthService = {
      checkEmailExists: vi.fn().mockReturnValue(of(false))
    };
    mockRouter = { navigate: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [Step1, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Step1);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debería iniciar con un formulario inválido', () => {
    expect(component.form.valid).toBe(false);
  });

  it('debería validar que el usuario tenga al menos 16 años', () => {
    const today = new Date();
    const year15Ago = today.getFullYear() - 15;

    component.form.patchValue({
      userName: 'TestUser',
      email: 'test@test.com',
      day: today.getDate(),
      month: today.getMonth() + 1,
      year: year15Ago
    });

    expect(component.form.errors?.['validateAge']).toBe(true);
    expect(component.form.valid).toBe(false);
  });

  it('debería marcar el error si el email ya existe en la BD', () => {
    mockAuthService.checkEmailExists.mockReturnValue(of(true));
    
    component.form.patchValue({
      userName: 'TestUser',
      email: 'existente@test.com',
      day: 1, month: 1, year: 1990
    });

    component.notifyNext();

    expect(component.emailExists).toBe(true);
    expect(component.form.get('email')?.hasError('alreadyExists')).toBe(true);
  });

  it('debería emitir los datos y avanzar si el formulario es válido y el email no existe', () => {
    const spyEmit = vi.spyOn(component.toNext, 'emit');
    
    component.form.patchValue({
      userName: 'Alicia',
      email: 'nueva@test.com',
      day: 23, month: 4, year: 2000
    });

    component.notifyNext();

    expect(mockAuthService.checkEmailExists).toHaveBeenCalledWith('nueva@test.com');
    expect(spyEmit).toHaveBeenCalledWith({
      userName: 'Alicia',
      email: 'nueva@test.com',
      birthdate: '2000-04-23'
    });
  });

  it('debería recuperar los datos iniciales al cargar (Persistencia)', () => {
    component.initialData = {
      userName: 'Marta',
      email: 'marta@test.com',
      birthdate: '1995-05-10'
    };

    component.ngOnInit();

    expect(component.form.get('userName')?.value).toBe('Marta');
    expect(component.form.get('year')?.value).toBe(1995);
  });
});
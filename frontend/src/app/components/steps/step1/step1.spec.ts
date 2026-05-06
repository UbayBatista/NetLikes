import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Step1 } from './step1';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';

describe('Step1 Component', () => {
  let component: Step1;
  let fixture: ComponentFixture<Step1>;
  let mockAuthService: any;
  let mockRouter: any;

  beforeEach(async () => {
    mockAuthService = {
      checkEmailExists: vi.fn().mockReturnValue(of(false)),
      checkNameExists: vi.fn().mockReturnValue(of(false))
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

  describe('Initialization and State Recovery', () => {
    it('should initiate with an invalid form', () => {
      expect(component.form.valid).toBe(false);
    });

    it('should recover initial data on load including birthdate parsing', () => {
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

  describe('Synchronous Form Validation', () => {
    it('should fail validation if the user is under 16 years old', () => {
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
  });

  describe('Asynchronous Validation (API Checks)', () => {
    it('should set an error if the email already exists in the database', () => {
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

    it('should set an error if the username already exists in the database', () => {
      mockAuthService.checkNameExists.mockReturnValue(of(true));
      
      component.form.patchValue({
        userName: 'Cogido',
        email: 'nuevo@test.com',
        day: 1, month: 1, year: 1990
      });

      component.notifyNext();

      expect(component.nameExists).toBe(true);
      expect(component.form.get('userName')?.hasError('alreadyExists')).toBe(true);
    });
  });

  describe('Navigation and Event Emitters', () => {
    it('should emit formatted data and proceed if the form is valid and credentials do not exist', () => {
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
  });
});
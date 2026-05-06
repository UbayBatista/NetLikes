import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Step2 } from './step2';
import { ReactiveFormsModule } from '@angular/forms';

describe('Step2 Component', () => {
  let component: Step2;
  let fixture: ComponentFixture<Step2>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Step2, ReactiveFormsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(Step2);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Initialization and State Recovery', () => {
    it('should initiate with an invalid form and empty fields', () => {
      expect(component.form.valid).toBe(false);
      expect(component.showPassword).toBe(false);
    });

    it('should recover previous data including password confirmation on init', () => {
      component.initialData = {
        password: 'passwordRecuperada',
        securityQuestion: '¿En qué país naciste?',
        answer: 'Italia'
      };

      component.ngOnInit();

      expect(component.form.get('password')?.value).toBe('passwordRecuperada');
      expect(component.form.get('confirmPassword')?.value).toBe('passwordRecuperada');
      expect(component.form.get('question')?.value).toBe('¿En qué país naciste?');
    });
  });

  describe('Form Validation', () => {
    it('should fail validation if passwords do not match', () => {
      component.form.patchValue({
        password: 'Password123',
        confirmPassword: 'Password456',
        question: '¿En qué país naciste?',
        answer: 'España'
      });

      expect(component.form.errors?.['notSamePasswords']).toBe(true);
      expect(component.form.valid).toBe(false);
    });

    it('should be valid if passwords match and all fields are filled', () => {
      component.form.patchValue({
        password: 'Password123!',
        confirmPassword: 'Password123!',
        question: '¿En qué país naciste?',
        answer: 'España'
      });

      expect(component.form.valid).toBe(true);
    });
  });

  describe('UI Interaction', () => {
    it('should toggle password visibility when togglePassword is called', () => {
      expect(component.showPassword).toBe(false);
      component.togglePassword();
      expect(component.showPassword).toBe(true);
    });
  });

  describe('Navigation and Event Emitters', () => {
    it('should emit correct data when notifyNext is called with a valid form', () => {
      const spyEmit = vi.spyOn(component.toNext, 'emit');
      
      component.form.patchValue({
        password: 'Misuperpassword123',
        confirmPassword: 'Misuperpassword123',
        question: '¿Cuál es el nombre de tu madre?',
        answer: 'Maria'
      });

      component.notifyNext();

      expect(spyEmit).toHaveBeenCalledWith({
        password: 'Misuperpassword123',
        securityQuestion: '¿Cuál es el nombre de tu madre?',
        answer: 'Maria'
      });
    });

    it('should emit toPrev event when notifyPrev is called', () => {
      const spyEmit = vi.spyOn(component.toPrev, 'emit');
      component.notifyPrev();
      expect(spyEmit).toHaveBeenCalled();
    });
  });
});
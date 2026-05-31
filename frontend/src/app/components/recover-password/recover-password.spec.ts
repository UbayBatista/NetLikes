import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ReactiveFormsModule } from '@angular/forms';
import { SimpleChange } from '@angular/core';
import { of } from 'rxjs';
import { RecoverPassword } from './recover-password';
import { AuthService } from '../../services/auth.service';

describe('RecoverPassword', () => {
  let component: RecoverPassword;
  let fixture: ComponentFixture<RecoverPassword>;
  let authServiceMock: any;

  beforeEach(async () => {
    authServiceMock = {
      getSecurityQuestion: vi.fn().mockReturnValue(of('¿Cuál es tu color favorito?')),
      isValidAnswer: vi.fn(),
      changePassword: vi.fn().mockReturnValue(of(void 0))
    };

    await TestBed.configureTestingModule({
      imports: [RecoverPassword, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RecoverPassword);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Initialization Logic (ngOnChanges)', () => {
    it('should load security question and set step to "question" if skipQuestion is false', () => {
      component.email = 'test@test.com';
      component.skipQuestion = false;
      
      component.ngOnChanges({
        email: new SimpleChange(null, 'test@test.com', true)
      });

      expect(authServiceMock.getSecurityQuestion).toHaveBeenCalledWith('test@test.com');
      expect(component.securityQuestion).toBe('¿Cuál es tu color favorito?');
      expect(component.step).toBe('question');
    });

    it('should skip question and go to "password" step if skipQuestion is true', () => {
      component.email = 'test@test.com';
      component.skipQuestion = true;
      
      component.ngOnChanges({
        email: new SimpleChange(null, 'test@test.com', true)
      });

      expect(authServiceMock.getSecurityQuestion).not.toHaveBeenCalled();
      expect(component.step).toBe('password');
    });
  });

  describe('Security Question Step', () => {
    it('should mark form as touched if answer is invalid on submit', () => {
      component.submitAnswer();
      expect(component.answerForm.touched).toBe(true);
      expect(authServiceMock.isValidAnswer).not.toHaveBeenCalled();
    });

    it('should advance to "password" step if answer is correct', () => {
      component.userEmail = 'test@test.com';
      component.answerForm.setValue({ answer: 'Azul' });
      authServiceMock.isValidAnswer.mockReturnValue(of(true));

      component.submitAnswer();

      expect(component.step).toBe('password');
    });

    it('should set wrongAnswer error if answer is incorrect', () => {
      component.userEmail = 'test@test.com';
      component.answerForm.setValue({ answer: 'Rojo' });
      authServiceMock.isValidAnswer.mockReturnValue(of(false));

      component.submitAnswer();

      expect(component.answerForm.get('answer')?.hasError('wrongAnswer')).toBe(true);
    });
  });

  describe('Password Validation', () => {
    it('should invalidate passwords shorter than 6 characters', () => {
      component.passwordForm.patchValue({ newPassword: 'Aa1' });
      expect(component.passwordForm.get('newPassword')?.hasError('minlength')).toBe(true);
    });

    it('should invalidate passwords without a number', () => {
      component.passwordForm.patchValue({ newPassword: 'Password' });
      expect(component.passwordForm.get('newPassword')?.hasError('pattern')).toBe(true);
    });

    it('should invalidate passwords without an uppercase letter', () => {
      component.passwordForm.patchValue({ newPassword: 'password1' });
      expect(component.passwordForm.get('newPassword')?.hasError('pattern')).toBe(true);
    });

    it('should invalidate passwords with spaces', () => {
      component.passwordForm.patchValue({ newPassword: 'Pass 123' });
      expect(component.passwordForm.get('newPassword')?.hasError('pattern')).toBe(true);
    });

    it('should validate passwords that meet all criteria', () => {
      component.passwordForm.patchValue({ newPassword: 'ValidPass1' });
      expect(component.passwordForm.get('newPassword')?.valid).toBe(true);
    });

    it('should set mismatch error if passwords do not match', () => {
      component.passwordForm.patchValue({
        newPassword: 'ValidPass1',
        confirmPassword: 'ValidPass2'
      });
      expect(component.passwordForm.hasError('mismatch')).toBe(true);
    });
  });

  describe('Submit Password Step', () => {
    it('should call changePassword and set step to "success" when valid', () => {
      component.userEmail = 'test@test.com';
      component.passwordForm.setValue({
        newPassword: 'ValidPass1',
        confirmPassword: 'ValidPass1'
      });

      component.submitPassword();

      expect(authServiceMock.changePassword).toHaveBeenCalledWith('test@test.com', 'ValidPass1');
      expect(component.step).toBe('success');
    });
  });
});
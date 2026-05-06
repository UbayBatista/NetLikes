import { describe, it, expect, vi, beforeEach } from 'vitest';
import { Welcome } from './welcome';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { of, throwError } from 'rxjs';

describe('Welcome Component', () => {
  let component: Welcome;
  let mockRouter: any;
  let mockAuthService: any;

  beforeEach(() => {
    mockRouter = { navigate: vi.fn() };
    mockAuthService = { 
      register: vi.fn().mockReturnValue(of({ name: 'Test User' })) 
    };

    component = new Welcome(mockRouter as Router, mockAuthService as AuthService);
  });

  describe('Initialization', () => {
    it('should start at step 0', () => {
      expect(component.currentStep).toBe(0);
    });
  });

  describe('Step Navigation Logic', () => {
    it('should advance to the next step when nextStep is called', () => {
      component.nextStep();
      expect(component.currentStep).toBe(1);
    });

    it('should not advance beyond step 4', () => {
      component.currentStep = 4;
      component.nextStep();
      expect(component.currentStep).toBe(4);
    });
  });

  describe('Data Handling', () => {
    it('should accumulate Step 1 data and advance to the next step', () => {
      const step1Data = { userName: 'UserTest', email: 'test@test.com', birthdate: '2000-01-01' };
      
      component.handleStep1(step1Data);
      
      expect(component.registrationData.userName).toBe('UserTest');
      expect(component.currentStep).toBe(1);
    });
  });

  describe('Registration and Finalization', () => {
    it('should call the register service and navigate to home upon completion', () => {
      const genreIds = [1, 2, 3];
      
      component.handleEnd(genreIds);

      expect(mockAuthService.register).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/home']);
    });

    it('should log an error if registration fails', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      mockAuthService.register.mockReturnValue(throwError(() => new Error('Server Error')));
      
      component.handleEnd([1]);
      
      expect(consoleSpy).toHaveBeenCalled();
      consoleSpy.mockRestore();
    });
  });
});
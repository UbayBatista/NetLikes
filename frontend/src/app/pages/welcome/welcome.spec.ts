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

  it('debería iniciar en el paso 0', () => {
    expect(component.currentStep).toBe(0);
  });

  it('debería avanzar de paso con nextStep', () => {
    component.nextStep();
    expect(component.currentStep).toBe(1);
  });

  it('no debería avanzar más allá del paso 4', () => {
    component.currentStep = 4;
    component.nextStep();
    expect(component.currentStep).toBe(4);
  });

  it('debería acumular los datos del Step 1 y avanzar', () => {
    const step1Data = { userName: 'UserTest', email: 'test@test.com', birthdate: '2000-01-01' };
    component.handleStep1(step1Data);
    
    expect(component.registrationData.userName).toBe('UserTest');
    expect(component.currentStep).toBe(1);
  });

  it('debería llamar al servicio de registro y navegar al home al finalizar', () => {
    const genreIds = [1, 2, 3];
    
    component.handleEnd(genreIds);

    expect(mockAuthService.register).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/home']);
  });

  it('debería loguear un error si el registro falla', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    mockAuthService.register.mockReturnValue(throwError(() => new Error('Server Error')));
    
    component.handleEnd([1]);
    
    expect(consoleSpy).toHaveBeenCalled();
  });
});
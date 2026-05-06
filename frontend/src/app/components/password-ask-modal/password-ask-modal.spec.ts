import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { PasswordVerifyModalComponent } from './password-ask-modal';
import { UserService } from '../../services/user.service';

describe('PasswordVerifyModalComponent', () => {
  let component: PasswordVerifyModalComponent;
  let fixture: ComponentFixture<PasswordVerifyModalComponent>;
  let userServiceMock: any;

  beforeEach(async () => {
    userServiceMock = {
      verifyPassword: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [PasswordVerifyModalComponent, FormsModule],
      providers: [
        { provide: UserService, useValue: userServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PasswordVerifyModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Initialization', () => {
    it('should create the component successfully', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('Form Validation', () => {
    it('should set an error message and not call the service if the password is empty', () => {
      component.password = '';
      
      component.verify();

      expect(component.errorMessage).toBe('Por favor, introduce tu contraseña.');
      expect(userServiceMock.verifyPassword).not.toHaveBeenCalled();
    });
  });

  describe('Verification Logic', () => {
    it('should emit the verified event and stop loading on successful verification', () => {
      const emitSpy = vi.spyOn(component.verified, 'emit');
      userServiceMock.verifyPassword.mockReturnValue(of({ valid: true }));
      
      component.password = 'correctPassword123';
      component.verify();

      expect(component.isLoading).toBe(false);
      expect(userServiceMock.verifyPassword).toHaveBeenCalledWith('correctPassword123');
      expect(emitSpy).toHaveBeenCalled();
    });

    it('should set an incorrect password error message and stop loading on a 401 error', () => {
      const error401 = { status: 401 };
      userServiceMock.verifyPassword.mockReturnValue(throwError(() => error401));
      
      component.password = 'wrongPassword';
      component.verify();

      expect(component.isLoading).toBe(false);
      expect(component.errorMessage).toBe('Contraseña incorrecta. Inténtalo de nuevo.');
    });

    it('should set a generic server error message and stop loading on a non-401 error', () => {
      const error500 = { status: 500 };
      userServiceMock.verifyPassword.mockReturnValue(throwError(() => error500));
      
      component.password = 'somePassword';
      component.verify();

      expect(component.isLoading).toBe(false);
      expect(component.errorMessage).toBe('Error en el servidor. Inténtalo más tarde.');
    });
  });

  describe('Close Logic', () => {
    it('should emit the close event when onClose is called', () => {
      const closeSpy = vi.spyOn(component.close, 'emit');
      
      component.onClose();

      expect(closeSpy).toHaveBeenCalled();
    });
  });
});
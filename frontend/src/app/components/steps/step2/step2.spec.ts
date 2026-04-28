import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Step2 } from './step2';
import { ReactiveFormsModule } from '@angular/forms';

describe('Step2', () => {
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

  it('debería iniciar con el formulario inválido y los campos vacíos', () => {
    expect(component.form.valid).toBe(false);
    expect(component.showPassword).toBe(false);
  });

  it('debería fallar la validación si las contraseñas no coinciden', () => {
    component.form.patchValue({
      password: 'password123',
      confirmPassword: 'password456',
      question: '¿En qué país naciste?',
      answer: 'España'
    });

    expect(component.form.errors?.['notSamePasswords']).toBe(true);
    expect(component.form.valid).toBe(false);
  });

  it('debería ser válido si las contraseñas coinciden y todos los campos están llenos', () => {
    component.form.patchValue({
      password: 'password123',
      confirmPassword: 'password123',
      question: '¿En qué país naciste?',
      answer: 'España'
    });

    expect(component.form.valid).toBe(true);
  });

  it('debería alternar la visibilidad de la contraseña con togglePassword', () => {
    expect(component.showPassword).toBe(false);
    component.togglePassword();
    expect(component.showPassword).toBe(true);
  });

  it('debería emitir los datos correctos al llamar a notifyNext si es válido', () => {
    const spyEmit = vi.spyOn(component.toNext, 'emit');
    
    component.form.patchValue({
      password: 'misuperpassword',
      confirmPassword: 'misuperpassword',
      question: '¿Cuál es el nombre de tu madre?',
      answer: 'Maria'
    });

    component.notifyNext();

    expect(spyEmit).toHaveBeenCalledWith({
      password: 'misuperpassword',
      securityQuestion: '¿Cuál es el nombre de tu madre?',
      answer: 'Maria'
    });
  });

  it('debería emitir toPrev al llamar a notifyPrev', () => {
    const spyEmit = vi.spyOn(component.toPrev, 'emit');
    component.notifyPrev();
    expect(spyEmit).toHaveBeenCalled();
  });

  it('debería recuperar datos previos (persistencia) incluyendo la confirmación', () => {
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
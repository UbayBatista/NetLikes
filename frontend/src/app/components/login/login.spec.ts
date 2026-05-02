import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Subject } from 'rxjs';

import { LoginForm } from './login';
import { AuthService } from '../../services/auth.service';

describe('LoginForm', () => {
  let component: LoginForm;
  let fixture: ComponentFixture<LoginForm>;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/users';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginForm, ReactiveFormsModule],
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: { navigate: () => {} } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginForm);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });
  
  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('form should be invalid when empty', () => {
    expect(component.form.valid).toBeFalsy();
  });

  it('email field should be invalid when empty', () => {
    component.form.get('email')?.setValue('');
    expect(component.form.get('email')?.hasError('required')).toBeTruthy();
  });

  it('email field should be invalid when format is wrong', () => {
    component.form.get('email')?.setValue('noesuncorreo');
    expect(component.form.get('email')?.hasError('email')).toBeTruthy();
  });

  it('password field should be invalid when shorter than 6 characters', () => {
    component.form.get('password')?.setValue('123');
    expect(component.form.get('password')?.hasError('minlength')).toBeTruthy();
  });

  it('form should be valid with correct email and password', () => {
    component.form.get('email')?.setValue('juan@email.com');
    component.form.get('password')?.setValue('SuperMan23');
    expect(component.form.valid).toBeTruthy();
  });

  it('login() should emit logIn event when form is valid', () => {
    let emitted: any = null;
    component.logIn.subscribe(value => emitted = value);

    component.form.get('email')?.setValue('juan@email.com');
    component.form.get('password')?.setValue('SuperMan23');
    component.login();

    expect(emitted).toEqual({ email: 'juan@email.com', password: 'SuperMan23' });
  });

  it('login() should not emit logIn event when form is invalid', () => {
    let emitted = false;
    component.logIn.subscribe(() => emitted = true);

    component.login();

    expect(emitted).toBeFalsy();
  });

  it('login() should mark all fields as touched when form is invalid', () => {
    component.login();

    expect(component.form.get('email')?.touched).toBeTruthy();
    expect(component.form.get('password')?.touched).toBeTruthy();
  });

  it('createAccount() should emit toNext event', () => {
    let emitted = false;
    component.toNext.subscribe(() => emitted = true);

    component.createAccount();

    expect(emitted).toBeTruthy();
  });

  it('togglePassword() should toggle showPassword', () => {
    expect(component.showPassword).toBeFalsy();
    component.togglePassword();
    expect(component.showPassword).toBeTruthy();
    component.togglePassword();
    expect(component.showPassword).toBeFalsy();
  });

  it('credentialsError input should set wrongCredentials error on password', () => {
    const subject = new Subject<void>();
    component.credentialsError = subject;

    subject.next();

    expect(component.form.get('password')?.hasError('wrongCredentials')).toBeTruthy();
  });
});
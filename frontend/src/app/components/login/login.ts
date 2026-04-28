import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import { NgClass } from '@angular/common';
import { Router } from '@angular/router';
import { Credentials } from '../../models/user.models';
import { AuthService } from '../../services/auth.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-login-form',
  imports: [ReactiveFormsModule, NgClass],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginForm {
  private destroy$ = new Subject<void>();

  @Input() set credentialsError(obs: Subject<void>) {
    obs.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.form.get('password')?.setErrors({ wrongCredentials: true });
    });
  }

  @Output() toNext = new EventEmitter<void>();
  @Output() logIn = new EventEmitter<Credentials>()

  form: FormGroup;
  showPassword: boolean = false;


  constructor(private router: Router, private fb: FormBuilder, private authService: AuthService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.form.get('password')?.valueChanges.subscribe(() => {
      if (this.form.get('password')?.hasError('wrongCredentials')) {
        this.form.get('password')?.setErrors(null);
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
  
  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  login() {
    if (this.form.valid) {
      this.logIn.emit({email: this.form.get('email')?.value, password: this.form.get('password')?.value})
    } else {
      this.form.markAllAsTouched();
    }
  }

  createAccount() {
    this.toNext.emit();
  }

  forgotPassword() {
    console.log('Redirigiendo a recuperación de contraseña');
    // TODO: Implement navigation to forgot password page
  }
}

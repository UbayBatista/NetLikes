import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import { NgClass } from '@angular/common';
import { Router } from '@angular/router';
import { Credentials } from '../../models/user.models';
import { AuthService } from '../../services/auth.service';
import { RecoverPassword } from '../recover-password/recover-password';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-login-form',
  imports: [ReactiveFormsModule, NgClass, RecoverPassword],
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
  showRecoverModal: boolean = false;
  showRecoverError = false;


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

    this.form.get('email')?.valueChanges.subscribe(() => {
      this.showRecoverError = false;
      if (this.form.get('email')?.hasError('notFound')) {
        this.form.get('email')?.setErrors(null);
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
    if (!this.form.get('email')?.valid) {
        this.showRecoverError = true;
        this.form.get('email')?.markAsTouched();
        return;
    }

    const email = this.form.get('email')!.value;
    this.authService.checkEmailExists(email).subscribe(exists => {
        if (!exists) {
            this.showRecoverError = true;
            this.form.get('email')?.setErrors({ notFound: true });
            return;
        }
        this.showRecoverError = false;
        this.showRecoverModal = true;
    });
}
}

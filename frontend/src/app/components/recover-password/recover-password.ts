import { Component, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { AuthService } from '../../services/auth.service';

type Step = 'question' | 'password' | 'success';

@Component({
    selector: 'app-recover-password',
    standalone: true,
    imports: [ReactiveFormsModule, NgClass],
    templateUrl: './recover-password.html',
    styleUrl: './recover-password.css'
})
export class RecoverPassword {
  @Input() set email(value: string) {
    if (value) {
      this.userEmail = value;
      this.authService.getSecurityQuestion(value).subscribe(question => {
        this.securityQuestion = question;
        this.step = 'question';
        this.cdr.detectChanges();
      });
    }
    }
  @Output() close = new EventEmitter<void>();

  step: Step = 'question';
  userEmail = '';
  securityQuestion = '';
  errorMessage = '';
  showPassword = false;
  showConfirmPassword = false;

  answerForm: FormGroup;
  passwordForm: FormGroup;

  constructor(private fb: FormBuilder, private authService: AuthService,  private cdr: ChangeDetectorRef) {
    this.answerForm = this.fb.group({
      answer: ['', Validators.required]
    });

    this.passwordForm = this.fb.group({
      newPassword: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.pattern(/^(?=.*[A-Z])(?=.*\d)(?!.*\s).+$/)
      ]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordsMatch });
  }

  passwordsMatch(group: FormGroup) {
    const p = group.get('newPassword')?.value;
    const c = group.get('confirmPassword')?.value;
    return p === c ? null : { mismatch: true };
  }

  submitAnswer() {
    if (this.answerForm.invalid) {
      this.answerForm.markAllAsTouched();
      return;
    }
    const answer = this.answerForm.get('answer')!.value;
    this.authService.isValidAnswer(this.userEmail, answer).subscribe({
      next: (valid) => {
        if (!valid) {
          this.answerForm.get('answer')?.setErrors({ wrongAnswer: true });
          return;
        }
        this.step = 'password';
      },
      error: () => this.answerForm.get('answer')?.setErrors({ wrongAnswer: true })
    });
  }

  submitPassword() {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    const newPassword = this.passwordForm.get('newPassword')!.value;
    this.authService.changePassword(this.userEmail, newPassword).subscribe(() => {
      this.step = 'success';
    });
  }

  togglePassword() { this.showPassword = !this.showPassword; }
  toggleConfirmPassword() { this.showConfirmPassword = !this.showConfirmPassword; }
}
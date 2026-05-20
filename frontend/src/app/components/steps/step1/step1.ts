import { Component, Output, EventEmitter, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { forkJoin } from 'rxjs';

function validateAge(control: AbstractControl) {
  const value = control.value;
  if (!value) return null;

  const birthDate = new Date(value);
  const today = new Date();
  
  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();
  const dayDiff = today.getDate() - birthDate.getDate();

  if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
    age--;
  }

  return age >= 16 ? null : { minAge: true };
}

@Component({
  selector: 'app-step1',
  standalone: true,
  imports: [ReactiveFormsModule, NgClass],
  templateUrl: './step1.html',
  styleUrls: ['../steps.css', './step1.css']
})
export class Step1 {
  @Input() initialData: any;
  @Output() toNext = new EventEmitter<{ userName: string; email: string; birthdate: string }>();
  @Output() toPrev = new EventEmitter<void>();

  form: FormGroup;
  emailExists: boolean = false;
  nameExists: boolean = false;

  constructor(private router: Router, private fb: FormBuilder, private authService: AuthService) {
    this.form = this.fb.group({
      userName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      birthdate: ['', [Validators.required, validateAge]]
    });
  }

  ngOnInit() {
    if (this.initialData) {
      this.form.patchValue({
        userName: this.initialData.userName,
        email: this.initialData.email,
        birthdate: this.initialData.birthdate || ''
      });
    }

    this.form.get('email')?.valueChanges.subscribe(() => {
        this.emailExists = false;
    });
    this.form.get('userName')?.valueChanges.subscribe(() => {
        this.nameExists = false;
    });
  }

  notifyNext() {
    if (this.form.valid) {
      const email = this.form.get('email')?.value;
      const userName = this.form.get('userName')?.value;

      forkJoin({
        emailEnUso: this.authService.checkEmailExists(email),
        nameEnUso: this.authService.checkNameExists(userName)
      }).subscribe({
        next: (respuestas: { emailEnUso: boolean; nameEnUso: boolean }) => {
          this.emailExists = respuestas.emailEnUso;
          this.nameExists = respuestas.nameEnUso;

          if (this.emailExists) this.form.get('email')?.setErrors({ alreadyExists: true });
          if (this.nameExists) this.form.get('userName')?.setErrors({ alreadyExists: true });

          if (!this.emailExists && !this.nameExists) {
            const val = this.form.value;
            this.toNext.emit({ userName: val.userName, email: val.email, birthdate: val.birthdate });
          }
        },
        error: (err: any) => console.error('Error al comprobar credenciales', err)
      });
    } else {
      this.form.markAllAsTouched();
    }
  }

  goBackToLogin() {
    this.toPrev.emit();
  }
}
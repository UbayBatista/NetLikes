import { Component, Output, EventEmitter, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import { Router } from '@angular/router';
import as from '@angular/common/locales/extra/as';
import { AuthService } from '../../../services/auth.service';

function validateAge(group: FormGroup) {
  const day = group.get('day')?.value;
  const month = group.get('month')?.value;
  const year = group.get('year')?.value;

  if (!day || !month || !year) return null;

  const birthDate = new Date(year, month - 1, day);
  const today = new Date();
  const age = today.getFullYear() - birthDate.getFullYear();
  const gotBirthday = today >= new Date(today.getFullYear(), birthDate.getMonth(), birthDate.getDate());
  const realAge = gotBirthday ? age : age - 1;

  return realAge >= 16 ? null : { validateAge: true };
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

  days = Array.from({ length: 31 }, (_, i) => i + 1);
  months = [
    { value: 1, name: 'Enero' }, { value: 2, name: 'Febrero' },
    { value: 3, name: 'Marzo' }, { value: 4, name: 'Abril' },
    { value: 5, name: 'Mayo' }, { value: 6, name: 'Junio' },
    { value: 7, name: 'Julio' }, { value: 8, name: 'Agosto' },
    { value: 9, name: 'Septiembre' }, { value: 10, name: 'Octubre' },
    { value: 11, name: 'Noviembre' }, { value: 12, name: 'Diciembre' }
  ];
  years = Array.from({ length: 100 }, (_, i) => new Date().getFullYear() - i);

  constructor(private router: Router, private fb: FormBuilder, private authService: AuthService) {
    this.form = this.fb.group({
      userName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      day: ['', Validators.required],
      month: ['', Validators.required],
      year: ['', Validators.required]
    }, { validators: validateAge });
  }

  ngOnInit() {
    if (this.initialData && this.initialData.birthdate) {
      const dateParts = this.initialData.birthdate.split('-');

      if (dateParts.length === 3) {
        this.form.patchValue({
          userName: this.initialData.userName,
          email: this.initialData.email,
          year: parseInt(dateParts[0], 10),
          month: parseInt(dateParts[1], 10),
          day: parseInt(dateParts[2], 10)
        });
      }
      this.form.get('email')?.valueChanges.subscribe(() => {
        this.emailExists = false;
      });
    } else if (this.initialData) {
      this.form.patchValue({
        userName: this.initialData.userName,
        email: this.initialData.email
      });
      this.form.get('email')?.valueChanges.subscribe(() => {
        this.emailExists = false;
      });
    }
  }

  notifyNext() {
    if (this.form.valid) {
      const email = this.form.get('email')?.value;
      this.authService.checkEmailExists(email).subscribe({
        next: (exists) => {
          if (exists) {
            this.emailExists = true;
            this.form.get('email')?.setErrors({ alreadyExists: true });
          } else {
            this.emailExists = false;
            const val = this.form.value;
            const birthdate = `${val.year}-${String(val.month).padStart(2, '0')}-${String(val.day).padStart(2, '0')}`;
            this.toNext.emit({ userName: val.userName, email: val.email, birthdate });
          }
        },
        error: (err) => console.error('Error al comprobar el email', err)
      });
    } else {
      this.form.markAllAsTouched();
    }
  }

  goBackToLogin() {
    this.toPrev.emit();
  }
}
import { Component } from '@angular/core';
import { BrandPanel } from '../../components/brand-panel/brand-panel';
import { LoginForm } from '../../components/login/login';
import { Step1 } from '../../components/steps/step1/step1';     
import { Step2 } from '../../components/steps/step2/step2';
import { Step3 } from '../../components/steps/step3/step3';
import { Step4 } from '../../components/steps/step4/step4';
import { Router } from '@angular/router';
import { RegisterData } from '../../models/user.models';
import { AuthService } from '../../services/auth.service';
import { Credentials } from '../../models/user.models';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-welcome',
  imports: [BrandPanel, LoginForm, Step1, Step2, Step3, Step4],
  templateUrl: './welcome.html',
  styleUrl: './welcome.css',
})
export class Welcome {
    currentStep: number = 0;
    termsAccepted: boolean = false;
    credentialsError$ = new Subject<void>();
    registrationData: RegisterData = {
        userName: '',
        email: '',
        birthdate: '',
        password: '',
        securityQuestion: '',
        answer: '',
        favoriteGenres: []
    };

    constructor(private router: Router, private authService: AuthService) {}

    nextStep() {
        if (this.currentStep < 4) {
            this.currentStep++;
        }
    }

    prevStep() {
        if (this.currentStep > 0) {
            this.currentStep--;
        }
    }

    handleStep1(data: {userName: string, email: string, birthdate: string}) {
        this.registrationData = { ...this.registrationData, ...data };
        this.nextStep();
    }

    handleStep2(data: {password: string, securityQuestion: string, answer: string}) {
        this.registrationData = { ...this.registrationData, ...data };
        this.nextStep();
    }

    handleStep3(status: boolean) {
        this.termsAccepted = status
        this.nextStep();
    }

    handleEnd(genreIds: number[]) {
        this.registrationData.favoriteGenres = genreIds.map(idValue => ({ 
            id: idValue, 
            genre: '' 
        }));
        this.authService.register(this.registrationData).subscribe({
            next: (user) => {
                this.router.navigate(['/home']);
            },
            error: (err) => console.error('Error al registrar:', err)
        });
    }

    logIn(credentials: Credentials){
        this.authService.login(credentials).subscribe({
            next: (user) => {
                this.router.navigate(['/home']);
            },
            error: (err) => this.credentialsError$.next()
        });
    }
}
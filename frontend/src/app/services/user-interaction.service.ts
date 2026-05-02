import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import { Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserInteractionService {

    private http = inject(HttpClient);
    private authService = inject(AuthService);
    private readonly markUrl = 'http://localhost:8080/marks';
    private readonly rateUrl = 'http://localhost:8080/rates';

    toggleMark(filmId: number, type: 'SEEN' | 'WATCHLATER'): Observable<any> {
        const email = this.authService.getCurrentUserEmail();
        if (!email) throw new Error('No hay email');

        return this.http.post(`${this.markUrl}/${email}/toggle/${filmId}?type=${type}`, {});
    }

    getMarkStatus(filmId: number): Observable<any> {
        const email = this.authService.getCurrentUserEmail();
        if (!email) return of(null);
        return this.http.get(`${this.markUrl}/${email}/status/${filmId}`);
    }

    toggleRate(filmId: number, score: string): Observable<any> {
        const email = this.authService.getCurrentUserEmail();
        return this.http.post(`${this.rateUrl}/${email}/${filmId}?score=${score.toUpperCase()}`, {});
    }

    getRateStatus(filmId: number): Observable<any> {
        const email = this.authService.getCurrentUserEmail();
        if (!email) return of(null);
        return this.http.get(`${this.rateUrl}/${email}/${filmId}`);
    }
}
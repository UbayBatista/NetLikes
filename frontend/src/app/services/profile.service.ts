import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, switchMap, take, of, tap } from 'rxjs';
import { UserService } from './user.service';
import { AuthService } from './auth.service';
import { MyProfile, UserProfile } from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class ProfileService {

    private profile$ = new BehaviorSubject<MyProfile | UserProfile | null>(null);
    private itsMe$ = new BehaviorSubject<boolean>(false);

    constructor(private userService: UserService, private authService: AuthService) {}

    loadProfile(username: string): void {
        this.profile$.next(null);

        this.authService.getCurrentUser().pipe(
            take(1),
            switchMap(currentUser => {
                const isMe = username === 'my-profile' 
                          || username === currentUser!.userName;

                this.itsMe$.next(isMe);

                if (isMe) {
                    return this.userService.getMyProfile(currentUser!.email);
                } else {
                    return this.userService.getUserProfile(username, currentUser!.email);
                }
            })
        ).subscribe(profile => this.profile$.next(profile));
    }

    updatePrivacy(isPrivate: boolean): void {
        this.profile$.pipe(take(1)).subscribe(profile => {
            if (!profile) return;
            this.userService.updatePrivacy(profile.email, isPrivate).subscribe(() =>{
                    this.profile$.next({ ...profile, isPrivate })
            });
        });
    }

    getProfile(): Observable<MyProfile | UserProfile | null> {
        return this.profile$.asObservable();
    }

    isMyProfile(): Observable<boolean> {
        return this.itsMe$.asObservable();
    }

    updateBio(email: string, bio: string): Observable<void> {
        return this.userService.updateBio(email, bio).pipe(
            tap(() => {
            const current = this.profile$.value;
            if (current) this.profile$.next({ ...current, bio });
            })
        );
    }

    updateAvatar(email: string, seed: string): Observable<void> {
        return this.userService.updateAvatar(email, seed).pipe(
            tap(() => {
            const current = this.profile$.value;
            if (current) this.profile$.next({ ...current, profilePicture: seed });
            })
        );
    }

    updateListVisibility(email: string, listType: 'WatchedFilms' | 'FilmsToWatchLater' | 'RecommendedFilms', isVisible: boolean): Observable<void> {
        const request$ = listType === 'WatchedFilms'
            ? this.userService.updateWatchedFilmsVisibility(email, isVisible)
            : listType === 'FilmsToWatchLater'
            ? this.userService.updateFilmsToWatchLaterVisibility(email, isVisible)
            : this.userService.updateRecommendedFilmsVisibility(email, isVisible);

        const updatedField = listType === 'WatchedFilms'
            ? { showWatchedFilms: isVisible }
            : listType === 'FilmsToWatchLater'
            ? { showFilmsToWatchLater: isVisible }
            : { showRecommendedFilms: isVisible };

        return request$.pipe(
            tap(() => {
            const current = this.profile$.value;
            if (current) this.profile$.next({ ...current, ...updatedField });
            })
        );
    }
}
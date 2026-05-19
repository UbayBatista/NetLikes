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

    updateBio(bio: string): void {
        this.profile$.pipe(take(1)).subscribe(profile => {
            if (!profile) return;
            this.userService.updateBio(profile.email, bio).subscribe(() => {
            this.profile$.next({ ...profile, bio });
            });
        });
    }

    updateAvatar(seed: string): void {
        this.profile$.pipe(take(1)).subscribe(profile => {
            if (!profile) return;
            this.userService.updateAvatar(profile.email, seed).subscribe(() => {
            this.profile$.next({ ...profile, profilePicture: seed });
            });
        });
    }

    updateListVisibility(listType: 'WatchedFilms' | 'FilmsToWatchLater' | 'RecommendedFilms', isVisible: boolean): Observable<void> {
    return this.profile$.pipe(
        take(1),
        switchMap(profile => {
        if (!profile) return of(void 0 as void);

        const request$ = listType === 'WatchedFilms'
            ? this.userService.updateWatchedFilmsVisibility(profile.email, isVisible)
            : listType === 'FilmsToWatchLater'
            ? this.userService.updateFilmsToWatchLaterVisibility(profile.email, isVisible)
            : this.userService.updateRecommendedFilmsVisibility(profile.email, isVisible);

        const updatedField = listType === 'WatchedFilms'
            ? { showWatchedFilms: isVisible }
            : listType === 'FilmsToWatchLater'
            ? { showFilmsToWatchLater: isVisible }
            : { showRecommendedFilms: isVisible };

        return request$.pipe(
            tap(() => this.profile$.next({ ...profile, ...updatedField }))
        );
        })
    );
    }
}
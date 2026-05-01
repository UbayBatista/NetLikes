import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, inject, DestroyRef } from "@angular/core";
import { CommonModule, AsyncPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router'; 
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, filter, map, Observable, take } from 'rxjs';

import { ProfileBody } from "../../components/profile-components/profile-components";
import { ProfileHeader } from "../../components/profile-header/profile-header";
import { Film } from "../../components/film/film";
import { SocialModal } from "../../components/social-modal/social-modal";

import { AuthService } from '../../services/auth.service';
import { ProfileService } from "../../services/profile.service";
import { FollowService } from "../../services/follow";
import { MyProfile, UserProfile } from '../../models/user.models';
import { ConfirmationModalComponent } from '../../components/confirmation-modal/confirmation-modal';

type SocialType = 'Seguidores' | 'Seguidos';
type FollowStatus = 'NONE' | 'PENDING' | 'ACCEPTED';

@Component({
  selector: "app-profile-complete",
  standalone: true,
  imports: [CommonModule, ProfileBody, ProfileHeader, Film, SocialModal, AsyncPipe, ConfirmationModalComponent],
  templateUrl: "./profile-body.html",
  styleUrl: "./profile-body.css"
})
export class ProfileComplete implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly profileService = inject(ProfileService);
  private readonly followService = inject(FollowService);
  private readonly destroyRef = inject(DestroyRef);

  profile$: Observable<MyProfile | UserProfile | null> = this.profileService.getProfile();
  itsMe$: Observable<boolean> = this.profileService.isMyProfile();

  followersCount$ = new BehaviorSubject<number>(0);
  followingCount$ = new BehaviorSubject<number>(0);

  isEditing = false;
  isSocialModalOpen = false;
  socialType: SocialType = 'Seguidores';
  socialData: any[] = [];
  canScrollLeft = false;
  canScrollRight = true;

  private followStateSubject = new BehaviorSubject<FollowStatus>('NONE');
  followState$ = this.followStateSubject.asObservable();

  followButtonText$: Observable<string> = this.followState$.pipe(
    map(state => {
      if (state === 'PENDING') return 'Pendiente';
      if (state === 'ACCEPTED') return 'Siguiendo';
      return 'Seguir';
    })
  );

  showConfirmModal = false;
  confirmModalMessage = '';
  private userToFollow: string = '';
  private actionToConfirm: 'UNFOLLOW' | 'REMOVE_FOLLOWER' = 'UNFOLLOW';

  ngOnInit() {
    this.route.params
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const username = params['username'];
        this.profileService.loadProfile(username);
      });

    this.profile$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(profile => {

        this.followersCount$.next((profile as any)?.followers || 0);
        this.followingCount$.next((profile as any)?.following || 0);

        const profileEmail = (profile as any)?.email; 
        
        if (profileEmail) {
          this.followService.checkFollowStatus(profileEmail).subscribe({
            next: (response) => {
              this.followStateSubject.next(response.state);
            },
            error: (err) => console.error('Error al obtener estado de seguimiento', err)
          });
        }
      });
  }

  onFollowRequest(userName: string, email: string) {
    this.userToFollow = email;

    const currentState = this.followStateSubject.value;

    if (currentState === 'ACCEPTED') {
      this.actionToConfirm = 'UNFOLLOW';
      this.confirmModalMessage = `¿Estás seguro de que quieres dejar de seguir a @${userName}?`;
      this.showConfirmModal = true;
    } 
    else if (currentState === 'PENDING') {
      this.executeUnfollow(this.userToFollow);
    } else {
      this.executeFollow(this.userToFollow);
    }
  }

  handleUnfollowConfirmation(confirmed: boolean) {
    this.showConfirmModal = false;

    if (confirmed && this.userToFollow) {
      if (this.actionToConfirm === 'UNFOLLOW' || this.actionToConfirm === 'REMOVE_FOLLOWER') {
        this.executeSocialAction(this.userToFollow, this.actionToConfirm);
      } else {
        this.executeFollow(this.userToFollow);
      }
    } else {
      this.userToFollow = '';
    }
  }

  private executeFollow(targetEmail: string) {
    this.followService.requestFollow(targetEmail).subscribe({
      next: (followResponse) => {
        this.followStateSubject.next(followResponse.state);

        if (followResponse.state === 'ACCEPTED') {
          const currentFollowers = this.followersCount$.value;
          this.followersCount$.next(currentFollowers + 1);
        }
      },
      error: (error) => console.error('Error al intentar seguir:', error),
      complete: () => {
        this.userToFollow = ''; 
      }
    });
  }

  private executeUnfollow(targetEmail: string) {
    this.followService.unfollow(targetEmail).subscribe({
      next: () => {        
        this.followStateSubject.next('NONE');

        const currentFollowers = this.followersCount$.value;
        this.followersCount$.next(Math.max(0, currentFollowers - 1));
      },
      error: (error) => console.error('Error al dejar de seguir:', error),
      complete: () => {
        this.userToFollow = ''; 
      }
    });
  }

  handleSocialAction(event: { user: any, type: 'Seguidores' | 'Seguidos' }) {
    const { user, type } = event;
    this.userToFollow = user.email;

    if (type === 'Seguidores') {
      this.actionToConfirm = 'REMOVE_FOLLOWER';
      this.confirmModalMessage = `¿Estás seguro de que quieres eliminar a @${user.name} de tus seguidores?`;
    } else {
      this.actionToConfirm = 'UNFOLLOW';
      this.confirmModalMessage = `¿Estás seguro de que quieres dejar de seguir a @${user.name}?`;
    }

    this.showConfirmModal = true;
  }

  private executeSocialAction(targetEmail: string, type: 'UNFOLLOW' | 'REMOVE_FOLLOWER') {
    const request$ = type === 'REMOVE_FOLLOWER'
      ? this.followService.remove(targetEmail)
      : this.followService.unfollow(targetEmail);

    request$.subscribe({
      next: () => {
        this.socialData = this.socialData.filter(u => u.email !== targetEmail);

        this.itsMe$.pipe(take(1)).subscribe(itsMe => {
          if (itsMe) {
            if (type === 'REMOVE_FOLLOWER') {
              this.followersCount$.next(Math.max(0, this.followersCount$.value - 1));
            } else {
              this.followingCount$.next(Math.max(0, this.followingCount$.value - 1));
            }
          } else {
            this.followersCount$.next(Math.max(0, this.followersCount$.value - 1));

            this.followStateSubject.next('NONE');
          }
        });

        this.cdr.detectChanges();
      },
      error: (err) => console.error(`Error al ejecutar ${type}:`, err),
      complete: () => {
        this.userToFollow = '';
      }
    });
  }

  showSocial(type: SocialType) {
    this.socialType = type;
    this.isSocialModalOpen = true;

    this.socialData = []; 

    this.profile$.pipe(
      filter((profile: any) => profile !== null), 
      take(1)
    ).subscribe(profile => {
      const currentProfileEmail = (profile as any)?.email;

      if (!currentProfileEmail) {
        console.error('El perfil actual no tiene email. Revisa la interfaz del perfil.');
        return;
      }

      if (type === 'Seguidores') {
        this.followService.getFollowers(currentProfileEmail).subscribe({
          next: (users) => {
            this.socialData = users.map(u => ({
              name: u.userName,
              avatar: u.profilePicture || 'assets/default-avatar.png', 
              email: u.email
            }));
            this.cdr.detectChanges();
          },
          error: (err) => console.error('Error cargando seguidores', err)
        });
      } else {
        this.followService.getFollowing(currentProfileEmail).subscribe({
          next: (users) => {
            this.socialData = users.map(u => ({
              name: u.userName,
              avatar: u.profilePicture || 'assets/ProfilePicture.jpg',
              email: u.email
            }));
            this.cdr.detectChanges();
          },
          error: (err) => console.error('Error cargando seguidos', err)
        });
      }
    });
  }

  toggleEdit() {
    this.isEditing = !this.isEditing;
  }

  onPrivacyChange(isPrivate: boolean): void {
    this.profileService.updatePrivacy(isPrivate);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
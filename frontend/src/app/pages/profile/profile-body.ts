import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, inject, DestroyRef } from "@angular/core";
import { CommonModule, AsyncPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router'; 
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, filter, forkJoin, map, Observable, take, of } from 'rxjs';

import { ProfileBody } from "../../components/profile-components/profile-components";
import { ProfileHeader } from "../../components/profile-header/profile-header";
import { Film } from "../../components/film/film";
import { SocialModal } from "../../components/social-modal/social-modal";
import { PasswordVerifyModalComponent } from '../../components/password-ask-modal/password-ask-modal';
import { RecoverPassword } from "../../components/recover-password/recover-password";

import { AuthService } from '../../services/auth.service';
import { ProfileService } from "../../services/profile.service";
import { FollowService } from "../../services/follow.service";
import { MyProfile, UserProfile } from '../../models/user.models';
import { ConfirmationModalComponent } from '../../components/confirmation-modal/confirmation-modal';
import { BlockedUsersModalComponent } from '../../components/blocked-users/blocked-users';
import { BioComponent } from "../../components/bio-component/bio-component";
import { UserService } from "../../services/user.service";
import { AvatarModal } from "../../components/avatar-modal/avatar-modal";
import { FilmListItem } from "../../models/film.models";

type SocialType = 'Seguidores' | 'Seguidos';
type FollowStatus = 'NONE' | 'PENDING' | 'ACCEPTED' | 'BLOCKED';
type VisibilityType = 'WatchedFilms' | 'FilmsToWatchLater' | 'RecommendedFilms';

@Component({
  selector: "app-profile-complete",
  standalone: true,
  imports: [CommonModule,
    ProfileBody,
    ProfileHeader,
    Film,
    SocialModal,
    AsyncPipe,
    ConfirmationModalComponent,
    BlockedUsersModalComponent,
    PasswordVerifyModalComponent,
    BioComponent, AvatarModal, RecoverPassword],
  templateUrl: "./profile-body.html",
  styleUrl: "./profile-body.css"
})
export class ProfileComplete implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly profileService = inject(ProfileService);
  private readonly userService = inject(UserService);
  private readonly followService = inject(FollowService);
  private readonly destroyRef = inject(DestroyRef);

  profile$: Observable<MyProfile | UserProfile | null> = this.profileService.getProfile();
  itsMe$: Observable<boolean> = this.profileService.isMyProfile();

  followersCount$ = new BehaviorSubject<number>(0);
  followingCount$ = new BehaviorSubject<number>(0);

  pendingVisibility: { type: VisibilityType, isVisible: boolean }[] = [];
  
  canScrollLeft: boolean = false;
  canScrollRight: boolean = true;
  isEditing: boolean = false;
  isSocialModalOpen: boolean = false;
  isAvatarModalOpen: boolean = false;
  isRecoverModalOpen: boolean = false;
  isBlockedModalOpen: boolean = false;
  isPasswordModalOpen: boolean = false;
  isDeleteConfirmModalOpen: boolean = false;
  showSaveModal: boolean = false;
  showConfirmModal: boolean = false;
  thereIsChanges: boolean = false;
  skipSecurityQuestion: boolean = false;
  socialType: SocialType = 'Seguidores';
  socialData: any[] = [];
  pendingBio: string = '';
  pendingAvatar: string = '';
  confirmModalMessage: string = '';
  actionUser: string = '';

  sections: {
    title: string;
    data: FilmListItem[] | null;
    visible: boolean;
    type: VisibilityType;
  }[] = [];

  private followStateSubject = new BehaviorSubject<FollowStatus>('NONE');
  followState$ = this.followStateSubject.asObservable();
  @ViewChild('bioComponent') bioComponent!: BioComponent;

  followButtonText$: Observable<string> = this.followState$.pipe(
    map(state => {
      if (state === 'BLOCKED') return 'Bloqueado';
      if (state === 'PENDING') return 'Pendiente';
      if (state === 'ACCEPTED') return 'Siguiendo';
      return 'Seguir';
    })
  );

  private actionToConfirm: 'UNFOLLOW' | 'BLOCK' | 'DELETE' | 'REMOVE_FOLLOWER' = 'UNFOLLOW';
  passwordModalMode: 'DELETE' | 'CHANGE' = 'DELETE';

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
        const profileEmail = (profile as any)?.email;

        if (profileEmail) {
          this.followService.checkFollowStatus(profileEmail).subscribe({
            next: (response) => {
              this.followStateSubject.next(response.state);

              if (response.state !== 'BLOCKED') {
                this.followersCount$.next((profile as any)?.followers || 0);
                this.followingCount$.next((profile as any)?.following || 0);
              }
            },
            error: (err) => console.error('Error al obtener estado de seguimiento', err)
          });
        }

        if (profile) {
          this.sections = [
            {
              title: 'Películas Vistas',
              data: profile.watchedFilms,
              visible: profile.showWatchedFilms,
              type: 'WatchedFilms'
            },
            {
              title: 'Ver más tarde',
              data: profile.laterFilms,
              visible: profile.showFilmsToWatchLater,
              type: 'FilmsToWatchLater'
            },
            {
              title: 'Películas Recomendadas',
              data: profile.recommendedFilms,
              visible: profile.showRecommendedFilms,
              type: 'RecommendedFilms'
            }
          ];
        }
      });
  }

  onFollowRequest(userName: string, email: string) {
    this.actionUser = email;

    const currentState = this.followStateSubject.value;

    if (currentState === 'ACCEPTED') {
      this.actionToConfirm = 'UNFOLLOW';
      this.confirmModalMessage = `¿Estás seguro de que quieres dejar de seguir a @${userName}?`;
      this.showConfirmModal = true;
    } 
    else if (currentState === 'PENDING') {
      this.executeUnfollow(this.actionUser);
    } else {
      this.executeFollow(this.actionUser);
    }
  }

  handleConfirmation(confirmed: boolean) {
    this.showConfirmModal = false;

    if (confirmed && this.actionUser) {
      if (this.actionToConfirm === 'UNFOLLOW' || this.actionToConfirm === 'REMOVE_FOLLOWER') {
        this.executeSocialAction(this.actionUser, this.actionToConfirm);
      } else if (this.actionToConfirm === 'BLOCK'){
        this.executeBlock(this.actionUser);
      } else if (this.actionToConfirm === 'DELETE'){
        this.executeDelete();
      }  
      else {
        this.executeFollow(this.actionUser);
      }
    } else {
      this.actionUser = '';
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
        this.actionUser = ''; 
      }
    });
  }

  private executeUnfollow(targetEmail: string) {
    this.followService.unfollow(targetEmail).subscribe({
      next: () => {       
        this.followStateSubject.next('NONE');
      },
      error: (error) => console.error('Error al dejar de seguir:', error),
      complete: () => {
        this.actionUser = ''; 
      }
    });
  }

  handleSocialAction(event: { user: any, type: 'Seguidores' | 'Seguidos' }) {
    const { user, type } = event;
    this.actionUser = user.email;

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
            this.route.params.pipe(take(1)).subscribe(params => {
              this.profileService.loadProfile(params['username']);
            });
          }
        });

        this.cdr.detectChanges();
      },
      error: (err) => console.error(`Error al ejecutar ${type}:`, err),
      complete: () => {
        this.actionUser = '';
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
              avatar: u.profilePicture || 'assets/ProfilePicture.jpg', 
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
    if (this.isEditing && this.thereIsChanges) {
      this.showSaveModal = true;
    } else{
      this.isEditing = !this.isEditing;
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  executeBlock(targetEmail: string) {
    this.followService.blockUser(targetEmail).subscribe({
      next: () => {
        this.followStateSubject.next('BLOCKED');
      },
      error: (error) => console.error('Error al intentar bloquear:', error),
      complete: () => {
        this.actionUser = ''; 
      }
    });
  }

  startDeleteProcess(email: string) {
    this.actionUser = email;
    this.passwordModalMode = 'DELETE';
    this.isPasswordModalOpen = true; 
  }

  onPasswordVerified() {
    this.isPasswordModalOpen = false; 
    
    if (this.passwordModalMode === 'DELETE') {
      this.actionToConfirm = 'DELETE';
      this.confirmModalMessage = `¿Estás seguro de que deseas borrar permanentemente tu cuenta?`;
      this.showConfirmModal = true;
    } else if (this.passwordModalMode === 'CHANGE') {
      this.skipSecurityQuestion = true;
      this.isRecoverModalOpen = true;
    }
    this.cdr.detectChanges();
  }

  onPrivacyChange(isPrivate: boolean): void {
    this.profileService.updatePrivacy(isPrivate);
  }

  onBlockRequest(userMail: string, userName: string){
    this.actionUser = userMail;
    this.actionToConfirm = 'BLOCK';
    this.confirmModalMessage = `¿Estás seguro de que quieres dejar de seguir a @${userName}?`;
    this.showConfirmModal = true;
  }

  onBioSave(event: { bio: string, hasChanges: boolean }) {
    this.pendingBio = event.bio;
    this.thereIsChanges = event.hasChanges;
  }

  onAvatarSelected(seed: string) {
    this.pendingAvatar = seed;
    this.thereIsChanges = true;
    this.isAvatarModalOpen = false;
  }

  onVisibilityChange(type: VisibilityType, isVisible: boolean) {
    const existing = this.pendingVisibility.findIndex(v => v.type === type);
    if (existing >= 0) {
      this.pendingVisibility[existing].isVisible = isVisible;
    } else {
      this.pendingVisibility.push({ type, isVisible });
    }
    this.thereIsChanges = true;
  }

  onForgotPasswordClicked() {
    this.isPasswordModalOpen = false;
    this.skipSecurityQuestion = false;
    this.isRecoverModalOpen = true;
  }

  handleSaveConfirmation(confirmed: boolean) {
    this.showSaveModal = false;
    if (confirmed) {
      this.profile$.pipe(take(1)).subscribe(profile => {
        if (!profile) return;

        const requests: Observable<any>[] = [];

        if (this.pendingBio) {
          requests.push(this.profileService.updateBio(profile.email, this.pendingBio));
          this.bioComponent?.updateOriginalBio(this.pendingBio);
        }
        if (this.pendingAvatar) {
          requests.push(this.profileService.updateAvatar(profile.email, this.pendingAvatar));
          this.authService.updateStoredUser({ profilePicture: this.pendingAvatar });
        }
        if (this.pendingVisibility.length > 0) {
          this.pendingVisibility.forEach(v =>
            requests.push(this.profileService.updateListVisibility(profile.email, v.type, v.isVisible))
          );
        }

        forkJoin(requests.length > 0 ? requests : [of(null)]).subscribe(() => {
          this.cdr.detectChanges();
        });
      });
    } else {
      this.bioComponent?.discardChanges();
    }
    this.pendingBio = '';
    this.pendingAvatar = '';
    this.pendingVisibility = [];
    this.thereIsChanges = false;
    this.isEditing = false;
  }

  executeDelete() {
    this.userService.deleteUser().subscribe({
      next: () => {
        this.authService.logout();
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Error eliminando la cuenta:', err);
      }
    });
  }

  startChangePasswordProcess() {
    this.authService.getCurrentUser().pipe(take(1)).subscribe(user => {
      if (user) {
        this.actionUser = user.email;
        this.passwordModalMode = 'CHANGE'; 
        this.isPasswordModalOpen = true;
        this.cdr.detectChanges();
      }
    });
  }
}
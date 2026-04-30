import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, inject, DestroyRef } from "@angular/core";
import { CommonModule, AsyncPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router'; 
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, map, Observable } from 'rxjs';

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

  isEditing = false;
  isSocialModalOpen = false;
  socialType: SocialType = 'Seguidores';
  socialData: any[] = [];
  canScrollLeft = false;
  canScrollRight = true;

  @ViewChild('scrollContainer') scrollContainer?: ElementRef<HTMLDivElement>;

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
  private usernameToFollow: string = '';
  private actionToConfirm: 'FOLLOW' | 'UNFOLLOW' = 'FOLLOW';

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
            },
            error: (err) => console.error('Error al obtener estado de seguimiento', err)
          });
        }
      });
  }

  onFollowRequest(userName: string, email: string) {
    this.userToFollow = email;
    this.usernameToFollow = userName;

    const currentState = this.followStateSubject.value;

    if (currentState === 'ACCEPTED') {
      this.actionToConfirm = 'UNFOLLOW';
      this.confirmModalMessage = `¿Estás seguro de que quieres dejar de seguir a @${userName}?`;
      this.showConfirmModal = true;
    } 
    else if (currentState === 'PENDING') {
      this.actionToConfirm = 'UNFOLLOW';
      this.executeUnfollow(this.userToFollow);
    } else {
      this.actionToConfirm = 'FOLLOW';
      this.executeFollow(this.userToFollow);
    }
  }

  handleFollowConfirmation(confirmed: boolean) {
    this.showConfirmModal = false;

    if (confirmed && this.userToFollow) {
      if (this.actionToConfirm === 'UNFOLLOW') {
        this.executeUnfollow(this.userToFollow);
      } else {
        this.executeFollow(this.userToFollow);
      }
    } else {
      console.log('Acción cancelada');
      this.userToFollow = '';
      this.usernameToFollow = '';
    }
  }

  private executeFollow(targetEmail: string) {
    console.log(`Ejecutando lógica de seguimiento hacia ${targetEmail}...`);
    this.followService.requestFollow(targetEmail).subscribe({
      next: (followResponse) => {
        this.followStateSubject.next(followResponse.state);
      },
      error: (error) => console.error('Error al intentar seguir:', error),
      complete: () => {
        this.userToFollow = ''; 
        this.usernameToFollow = '';
      }
    });
  }

  private executeUnfollow(targetEmail: string) {
    console.log(`Ejecutando lógica para dejar de seguir a ${targetEmail}...`);
    this.followService.unfollow(targetEmail).subscribe({
      next: () => {
        console.log('Se ha dejado de seguir al usuario o cancelado la solicitud.');
        
        this.followStateSubject.next('NONE');
      },
      error: (error) => console.error('Error al dejar de seguir:', error),
      complete: () => {
        this.userToFollow = ''; 
        this.usernameToFollow = '';
      }
    });
  }

  showSocial(type: SocialType) {
    this.socialType = type;
    this.isSocialModalOpen = true;
    this.socialData = type === 'Seguidores' ? this.getFollowersMock() : this.getFollowingMock();
  }

  scroll(direction: 'left' | 'right') {
    if (!this.scrollContainer) return;
    
    const element = this.scrollContainer.nativeElement;
    const scrollAmount = direction === 'left' ? -300 : 300;
    
    element.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    setTimeout(() => this.updateScrollButtons(), 350);
  }

  updateScrollButtons() {
    const el = this.scrollContainer?.nativeElement;
    if (!el) return;

    this.canScrollLeft = el.scrollLeft > 5;
    this.canScrollRight = el.scrollLeft + el.clientWidth < el.scrollWidth - 5;
    this.cdr.detectChanges();
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

  private getFollowersMock() {
    return [
      { name: 'Luis Suárez', avatar: '...', status: 'Seguir también' },
      { name: 'Messi', avatar: '...', status: 'Seguir también' },
    ];
  }

  private getFollowingMock() {
    return [
      { name: 'Cristiano Ronaldo', avatar: '...' },
      { name: 'Zinedine Zidane', avatar: '...' },
    ];
  }
}
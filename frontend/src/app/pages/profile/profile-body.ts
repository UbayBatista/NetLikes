import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, inject, DestroyRef } from "@angular/core";
import { CommonModule, AsyncPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router'; 
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, map } from 'rxjs';

// Componentes e Imports
import { ProfileBody } from "../../components/profile-components/profile-components";
import { ProfileHeader } from "../../components/profile-header/profile-header";
import { Film } from "../../components/film/film";
import { SocialModal } from "../../components/social-modal/social-modal";

// Servicios y Modelos
import { AuthService } from '../../services/auth.service';
import { ProfileService } from "../../services/profile.service";
import { MyProfile, UserProfile } from '../../models/user.models';
import { ConfirmationModalComponent } from '../../components/confirmation-modal/confirmation-modal'
// Definimos un tipo para evitar errores de escritura
type SocialType = 'Seguidores' | 'Seguidos';

@Component({
  selector: "app-profile-complete",
  standalone: true,
  imports: [CommonModule, ProfileBody, ProfileHeader, Film, SocialModal, AsyncPipe, ConfirmationModalComponent],
  templateUrl: "./profile-body.html",
  styleUrl: "./profile-body.css"
})
export class ProfileComplete implements OnInit {
  // Inyección de dependencias moderna (más limpia que en el constructor)
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly profileService = inject(ProfileService);
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

  ngOnInit() {
    this.route.params
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        this.profileService.loadProfile(params['username']);
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

    this.canScrollLeft = el.scrollLeft > 5; // Margen de error de 5px
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

  follow() {
    console.log('Follow logic here');
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

  showConfirmModal = false;
  confirmModalMessage = '';

  onFollowRequest(userName: string) {
    this.confirmModalMessage = `¿Estás seguro de que quieres empezar a seguir a @${userName}?`;
    this.showConfirmModal = true;
  }

  handleFollowConfirmation(confirmed: boolean) {
    this.showConfirmModal = false;

    if (confirmed) {
      this.executeFollow();
    } else {
      console.log('Seguimiento cancelado');
    }
  }

  private executeFollow() {
    console.log('Ejecutando lógica de seguimiento en el servidor...');
  }
}
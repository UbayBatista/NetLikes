import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, inject, DestroyRef } from "@angular/core";
import { CommonModule, AsyncPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router'; 
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';

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
  // Inyecciones de dependencias
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly profileService = inject(ProfileService);
  private readonly followService = inject(FollowService);
  private readonly destroyRef = inject(DestroyRef);

  // Observables del perfil
  profile$: Observable<MyProfile | UserProfile | null> = this.profileService.getProfile();
  itsMe$: Observable<boolean> = this.profileService.isMyProfile();

  // Estados de la UI
  isEditing = false;
  isSocialModalOpen = false;
  socialType: SocialType = 'Seguidores';
  socialData: any[] = [];
  canScrollLeft = false;
  canScrollRight = true;

  @ViewChild('scrollContainer') scrollContainer?: ElementRef<HTMLDivElement>;

  // --- VARIABLES PARA EL SEGUIMIENTO ---
  followState: FollowStatus = 'NONE';
  showConfirmModal = false;
  confirmModalMessage = '';
  private userToFollow: string = ''; // Guarda el email para el backend
  private usernameToFollow: string = ''; // Guarda el @username para la UI

  ngOnInit() {
    // 1. Cargamos el perfil al detectar cambios en la URL
    this.route.params
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const username = params['username'];
        this.profileService.loadProfile(username);
      });

    // 2. Escuchamos al perfil para averiguar el estado de seguimiento inicial
    this.profile$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(profile => {
        // Hacemos un cast a 'any' por si MyProfile/UserProfile no tienen la propiedad email explícita en la interfaz base
        const profileEmail = (profile as any)?.email; 
        
        if (profileEmail) {
          this.followService.checkFollowStatus(profileEmail).subscribe({
            next: (response) => {
              this.followState = response.state;
              this.cdr.detectChanges(); // Forzamos actualización visual
            },
            error: (err) => console.error('Error al obtener estado de seguimiento', err)
          });
        }
      });
  }

  // --- GETTER PARA EL TEXTO DEL BOTÓN ---
  get followButtonText(): string {
    switch (this.followState) {
      case 'PENDING': return 'Pendiente';
      case 'ACCEPTED': return 'Siguiendo';
      default: return 'Seguir';
    }
  }

  // --- LÓGICA DE SEGUIMIENTO ---
  onFollowRequest(userName: string, email: string) {
    // Evitamos pedir confirmación si ya lo seguimos o está pendiente
    if (this.followState === 'ACCEPTED' || this.followState === 'PENDING') {
        console.log('Ya hay una relación de seguimiento activa o pendiente.');
        return; 
    }

    this.userToFollow = email;
    this.usernameToFollow = userName;
    this.confirmModalMessage = `¿Estás seguro de que quieres empezar a seguir a @${userName}?`;
    this.showConfirmModal = true;
  }

  handleFollowConfirmation(confirmed: boolean) {
    this.showConfirmModal = false;

    if (confirmed && this.userToFollow) {
      this.executeFollow(this.userToFollow);
    } else {
      console.log('Seguimiento cancelado');
      this.userToFollow = '';
      this.usernameToFollow = '';
    }
  }

  private executeFollow(targetEmail: string) {
    console.log(`Ejecutando lógica de seguimiento hacia ${targetEmail}...`);
    
    this.followService.requestFollow(targetEmail).subscribe({
      next: (followResponse) => {
        console.log('Respuesta del servidor:', followResponse);
        
        // Actualizamos el estado visual instantáneamente
        if (followResponse.state === 'PENDING') {
          this.followState = 'PENDING';
        } else if (followResponse.state === 'ACCEPTED') {
          this.followState = 'ACCEPTED';
        }

        // Recargamos el perfil (opcional, útil si tienes contadores de seguidores en la UI)
        this.profileService.loadProfile(this.usernameToFollow);
      },
      error: (error) => {
        console.error('Error al intentar seguir:', error);
      },
      complete: () => {
        // Limpiamos la memoria temporal
        this.userToFollow = ''; 
        this.usernameToFollow = '';
      }
    });
  }

  // --- LÓGICA DE INTERFAZ Y MODALES SOCIALES ---
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

  // --- MOCKS ---
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
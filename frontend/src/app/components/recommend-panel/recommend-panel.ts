import { Component, Input, Output, EventEmitter, inject, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserInteractionService } from '../../services/user-interaction.service';
import { AuthService } from '../../services/auth.service';
import { FollowService } from '../../services/follow.service';
import { SearchBarComponent } from '../search-bar/search-bar';

@Component({
  selector: 'app-recommend-panel',
  standalone: true,
  imports: [CommonModule, SearchBarComponent],
  templateUrl: './recommend-panel.html',
  styleUrl: './recommend-panel.css'
})
export class RecommendPanel implements OnChanges {
  @Input() isOpen: boolean = false;
  @Input() filmId!: number;
  @Input() initialRecommended: boolean = false;
  
  @Output() closed = new EventEmitter<void>();
  @Output() recommendedStatusChanged = new EventEmitter<boolean>();

  private interactionService = inject(UserInteractionService);
  private authService = inject(AuthService);
  private followService = inject(FollowService);

  addToProfile: boolean = false;
  selectedUsers: string[] = [];

  followingUsers: any[] = []; 
  filteredFollowers: any[] = []; 

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isOpen'] && changes['isOpen'].currentValue === true) {
      this.addToProfile = this.initialRecommended;
      this.selectedUsers = [];
      
      this.loadFollowing(); 
    }
  }

  close() {
    this.closed.emit();
  }

  loadFollowing() {
    this.authService.getCurrentUser().subscribe(user => {
      if (user && user.email) {
        this.followService.getFollowing(user.email).subscribe({
          next: (users) => {
            this.followingUsers = users.map((u: any) => ({
              name: u.userName,
              pic: u.profilePicture || 'assets/ProfilePicture.jpg',
              email: u.email
            }));
            
            this.filteredFollowers = [...this.followingUsers];
          },
          error: (err) => console.error('Error cargando los seguidos', err)
        });
      }
    });
  }

  toggleMyRecommendation() {
    this.addToProfile = !this.addToProfile;
  }

  toggleUser(userName: string) {
    const index = this.selectedUsers.indexOf(userName);
    if (index > -1) {
      this.selectedUsers.splice(index, 1);
    } else {
      this.selectedUsers.push(userName);
    }
  }

  handleSearch(query: string) {
    if (!query || query.trim() === '') {
      this.filteredFollowers = [...this.followingUsers];
    } else {
      const lowerQuery = query.toLowerCase();
      this.filteredFollowers = this.followingUsers.filter(user =>
        user.name.toLowerCase().includes(lowerQuery)
      );
    }
  }

  submitRecommendation() {
    if (this.addToProfile !== this.initialRecommended) {
      this.interactionService.toggleMark(this.filmId, 'RECOMMENDED').subscribe({
        next: (res) => {
          this.recommendedStatusChanged.emit(this.addToProfile); 
        },
        error: (err) => {
          console.error('Error al actualizar recomendación en perfil', err);
          this.addToProfile = this.initialRecommended; 
        }
      });
    }

    if (this.selectedUsers.length > 0) {
      console.log('Usuarios seleccionados para enviar recomendación (nombres):', this.selectedUsers);
      
      const selectedEmails = this.followingUsers
        .filter(user => this.selectedUsers.includes(user.name))
        .map(user => user.email);
      console.log('Emails de los seleccionados:', selectedEmails);
    }

    this.close();
  }
}
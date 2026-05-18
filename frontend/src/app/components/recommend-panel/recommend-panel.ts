import { Component, Input, Output, EventEmitter, inject, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserInteractionService } from '../../services/user-interaction.service';
import { AuthService } from '../../services/auth.service';
import { FollowService } from '../../services/follow.service';
import { SearchBarComponent } from '../search-bar/search-bar';
import { ConfirmationModalComponent } from '../confirmation-modal/confirmation-modal';
import { RecommendationFollowedService } from '../../services/recommendation-followed';

@Component({
  selector: 'app-recommend-panel',
  standalone: true,
  imports: [CommonModule, SearchBarComponent, ConfirmationModalComponent],
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
  private recService = inject(RecommendationFollowedService);
  private cdr = inject(ChangeDetectorRef);

  addToProfile: boolean = false;
  selectedUsers: string[] = [];

  followingUsers: any[] = []; 
  displayUsers: any[] = [];
  
  alreadyRecommendedEmails: string[] = [];
  topRecentEmails: string[] = [];

  ngOnChanges(changes: SimpleChanges) {
    if (changes['initialRecommended']) {
      this.addToProfile = this.initialRecommended;
    }
    
    if (changes['isOpen'] && this.isOpen) {
      this.addToProfile = this.initialRecommended;
      this.selectedUsers = []; 
      this.loadData(); 
      this.cdr.detectChanges();
    }
  }

  close() {
    this.closed.emit();
  }

  toggleMyRecommendation() {
    this.addToProfile = !this.addToProfile;
  }

  loadData() {
    this.authService.getCurrentUser().subscribe(user => {
      if (user?.email) {
        this.followService.getFollowing(user.email).subscribe(allFollowers => {
          this.followingUsers = allFollowers.map((u: any) => ({
            name: u.userName,
            pic: u.profilePicture || 'assets/ProfilePicture.jpg',
            email: u.email
          }));

          this.recService.getRecipientsForFilm(this.filmId).subscribe(emails => {
            this.alreadyRecommendedEmails = emails;

            this.recService.getRecentRecipients().subscribe(recents => {
              this.topRecentEmails = recents.map(r => r.email);
              this.updateDisplayList(''); 
            });
          });
        });
      }
    });
  }

  toggleUser(email: string) {
    if (this.alreadyRecommendedEmails.includes(email)) {
      return; 
    }

    const index = this.selectedUsers.indexOf(email);
    if (index > -1) {
      this.selectedUsers.splice(index, 1);
    } else {
      this.selectedUsers.push(email);
    }
    this.cdr.detectChanges();
  }

  handleSearch(query: string) {
    this.updateDisplayList(query);
  }

  updateDisplayList(query: string) {
    if (!query || query.trim() === '') {
      let topUsers = this.followingUsers.filter(u => this.topRecentEmails.includes(u.email));
      
      const selectedNotTop = this.followingUsers.filter(u => 
        this.selectedUsers.includes(u.email) && !this.topRecentEmails.includes(u.email)
      );
      
      let combined = [...topUsers, ...selectedNotTop];

      if (combined.length === 0) {
        combined = this.followingUsers.slice(0, 10);
      } else {
        combined = combined.slice(0, Math.max(10, combined.length));
      }

      this.displayUsers = combined;
    } else {
      const lowerQuery = query.toLowerCase();
      this.displayUsers = this.followingUsers.filter(u =>
        u.name.toLowerCase().includes(lowerQuery)
      );
    }
    
    this.cdr.detectChanges(); 
  }

  showConfirmModal: boolean = false;
  confirmMessage: string = '';

  submitRecommendation() {
    if (this.selectedUsers.length === 0) {
      this.executeFinalActions();
      return;
    }

    const count = this.selectedUsers.length;
    this.confirmMessage = `Vas a recomendar esta película a ${count} seguidor${count > 1 ? 'es' : ''}. ¿Estás seguro?`;
    this.showConfirmModal = true;
  }

  handleConfirmation(confirmed: boolean) {
    this.showConfirmModal = false;
    if (confirmed) {
      this.executeFinalActions();
    }
  }

  private executeFinalActions() {
    if (this.addToProfile !== this.initialRecommended) {
      
      this.initialRecommended = this.addToProfile; 
      
      this.interactionService.toggleMark(this.filmId, 'RECOMMENDED').subscribe();
      this.recommendedStatusChanged.emit(this.addToProfile);
    }

    if (this.selectedUsers.length > 0) {
      const targetEmails = this.selectedUsers.filter(e => !this.alreadyRecommendedEmails.includes(e)); 
      
      if (targetEmails.length > 0) {
        this.recService.sendRecommendations(this.filmId, targetEmails).subscribe({
          next: () => console.log("Recomendaciones enviadas con éxito"),
          error: (err) => console.error("Error al recomendar a seguidos", err)
        });
      }
    }
    
    this.close();
  }
}
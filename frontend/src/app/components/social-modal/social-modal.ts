import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from "@angular/router";

interface User {
  name: string;
  avatar: string;
  status?: string;
}

export interface SocialAction {
  user: User;
  type: 'Seguidores' | 'Seguidos';
}
@Component({
  selector: 'app-social-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './social-modal.html',
  styleUrls: ['./social-modal.css', '../notification-panel/notification-panel.css']
})
export class SocialModal {
  @Input() title: 'Seguidores' | 'Seguidos' = 'Seguidores';
  @Input() users: User[] = [];
  @Input() isMyOwnProfile: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() tabChange = new EventEmitter<'Seguidores' | 'Seguidos'>();
  @Output() actionClick = new EventEmitter<SocialAction>();

  constructor(private router: Router) {}

  handleAction(user: any) {
    this.actionClick.emit({
      user: user,
      type: this.title
    });
  }

  closeModal() {
    this.close.emit();
  }

  changeTab(newTab: 'Seguidores' | 'Seguidos') {
    if (this.title === newTab) return;
    
    this.title = newTab;
    this.tabChange.emit(newTab); 
  }
  
  goToProfile(userName: string) {
    this.router.navigate(['/profile', userName]);
    this.closeModal();
  }
}
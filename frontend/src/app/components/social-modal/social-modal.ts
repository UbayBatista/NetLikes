import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

interface User {
  name: string;
  avatar: string;
  status?: string;
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

  closeModal() {
    this.close.emit();
  }

  handleAction(user: User) {
    console.log(`${this.title === 'Seguidores' ? 'Eliminando' : 'Dejando de seguir'} a:`, user.name);
  }

  changeTab(newTab: 'Seguidores' | 'Seguidos') {
  if (this.title === newTab) return;
  
  this.title = newTab;
  this.tabChange.emit(newTab); 
}
}
import { Component, EventEmitter, inject, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FollowService, LoggedUser } from '../../services/follow.service';

@Component({
  selector: 'app-blocked-users-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './blocked-users.html',
  styleUrl: './blocked-users.css'
})
export class BlockedUsersModalComponent implements OnInit {
  @Output() close = new EventEmitter<void>();

  private readonly followService = inject(FollowService);
  private readonly cdr = inject(ChangeDetectorRef);
  
  blockedUsers: LoggedUser[] = [];
  isLoading = true;

  ngOnInit() {
    this.loadBlockedUsers();
  }

  loadBlockedUsers() {
    this.followService.getBlockedUsers().subscribe({
      next: (users) => {
        this.blockedUsers = users;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error cargando usuarios bloqueados', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  unblock(user: LoggedUser) {
    this.followService.unblockUser(user.email).subscribe({
      next: () => {
        this.blockedUsers = this.blockedUsers.filter(u => u.email !== user.email);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al desbloquear', err)
    });
  }

  onClose() {
    this.close.emit();
  }
}
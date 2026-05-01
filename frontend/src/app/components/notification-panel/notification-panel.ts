import { Component, Input, Output, EventEmitter, ChangeDetectorRef} from '@angular/core';
import { Notification } from '../notification/notification';
import { CommonModule } from '@angular/common';
import { FollowService, LoggedUser } from '../../services/follow.service';

type PanelView = 'notifications' | 'requests';

@Component({
  selector: 'app-notification-panel',
  imports: [Notification, CommonModule],
  templateUrl: './notification-panel.html',
  styleUrl: './notification-panel.css',
})
export class NotificationPanel {
  @Input() isOpen: boolean = false;
  @Input() notifications: { image: string; message: string }[] = [];

  @Output() closed = new EventEmitter<void>();
  
  view: PanelView = 'notifications';
  pendingRequests: LoggedUser[] = [];
  loading = false;
  
  constructor(private followService: FollowService, private cdr: ChangeDetectorRef) {}

  close() {
    this.closed.emit();
    this.view = 'notifications';
  }

  showRequests() {
    this.view = 'requests';
    this.loading = true;
    this.followService.getPendingRequests().subscribe((requests: LoggedUser[]) => {
      this.pendingRequests = requests;
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  accept(followerId: string) {
    this.followService.acceptFollow(followerId).subscribe(() => {
      this.pendingRequests = this.pendingRequests.filter(r => r.email !== followerId);
    });
  }

  reject(followerId: string) {
    this.followService.rejectFollow(followerId).subscribe(() => {
      this.pendingRequests = this.pendingRequests.filter(r => r.email !== followerId);
    });
  }

  goBack() {
    this.view = 'notifications';
  }
}

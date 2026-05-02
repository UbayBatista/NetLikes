import { Component, Input, Output, EventEmitter, ChangeDetectorRef, OnInit, OnDestroy, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import { Notification } from '../notification/notification';
import { FollowService, LoggedUser } from '../../services/follow.service';
import { NotifyResponse } from '../../models/notify.models';
import { NotificationService } from '../../services/notification.service';

type PanelView = 'notifications' | 'requests';

@Component({
  selector: 'app-notification-panel',
  imports: [Notification, CommonModule],
  templateUrl: './notification-panel.html',
  styleUrl: './notification-panel.css',
})
export class NotificationPanel implements OnInit, OnDestroy, OnChanges {
  @Input() isOpen: boolean = false;
  
  @Output() closed = new EventEmitter<void>();
  
  view: PanelView = 'notifications';
  pendingRequests: LoggedUser[] = [];
  loading = false;
  
  notifications: NotifyResponse[] = [];
  private notifSub!: Subscription;
  
  constructor(
    private followService: FollowService, 
    private notificationService: NotificationService, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.notifSub = this.notificationService.notifications$.subscribe(notifs => {
      this.notifications = notifs;
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy() {
    if (this.notifSub) {
      this.notifSub.unsubscribe();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isOpen'] && changes['isOpen'].currentValue === true) {
      this.notificationService.markAllAsRead();
    }
  }

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
import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { NotificationPanel } from '../notification-panel/notification-panel';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { Observable } from 'rxjs';
import { User } from '../../models/user.models';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-header',
  imports: [RouterLink, NotificationPanel, AsyncPipe],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header implements OnInit, OnDestroy{
  currentUser$: Observable<User | null>;
  unreadCount$: Observable<number>;

  userName = "my-profile"
  boxOpen = false;

  constructor(private router: Router, private authService: AuthService, private notificationService: NotificationService) {
    this.currentUser$ = this.authService.getCurrentUser();
    this.unreadCount$ = this.notificationService.unreadCount$;
  }

  ngOnInit() {
    this.notificationService.initNotifications();
  }

  ngOnDestroy() {
    this.notificationService.disconnect();
  }

  goToProfile() {
    this.router.navigate(['/profile', this.userName]);
  }

  openBox() {
    this.boxOpen = true;
  }
}
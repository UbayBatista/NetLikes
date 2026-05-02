import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { NotifyResponse } from '../models/notify.models';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly apiUrl = 'http://localhost:8080/notifications';
  private eventSource: EventSource | null = null;

  private notificationsSubject = new BehaviorSubject<NotifyResponse[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient, private authService: AuthService, private zone: NgZone) {}

  initNotifications(): void {
    const email = this.authService.getCurrentUserEmail();
    if (!email) return;

    this.http.get<NotifyResponse[]>(`${this.apiUrl}/${email}`).subscribe(notifs => {
      this.notificationsSubject.next(notifs);
    });

    this.http.get<{unreadCount: number}>(`${this.apiUrl}/${email}/unread-count`).subscribe(res => {
      this.unreadCountSubject.next(res.unreadCount);
    });

    this.connectSSE(email);
  }

  private connectSSE(email: string): void {
    if (this.eventSource) this.eventSource.close();

    this.eventSource = new EventSource(`${this.apiUrl}/stream/${email}`);

    this.eventSource.addEventListener('NEW_NOTIFICATION', (event: MessageEvent) => {
      this.zone.run(() => {
        const newNotif: NotifyResponse = JSON.parse(event.data);
        
        const currentNotifs = this.notificationsSubject.value;
        this.notificationsSubject.next([newNotif, ...currentNotifs]);
        this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
      });
    });

    this.eventSource.onerror = () => {
      this.eventSource?.close();
    };

    // Dentro de connectSSE(email: string) en notification.service.ts

    this.eventSource.addEventListener('DELETE_NOTIFICATION', (event: MessageEvent) => {
    this.zone.run(() => {
        const deletedData = JSON.parse(event.data);
        
        // 1. Filtramos la lista para quitar la notificación borrada
        const currentNotifs = this.notificationsSubject.value;
        const updatedNotifs = currentNotifs.filter(n => 
        !(n.senderEmail === deletedData.senderEmail && n.type === deletedData.type)
        );
        
        // 2. Si la notificación que borramos no estaba leída, bajamos el contador
        const wasUnread = currentNotifs.find(n => 
        n.senderEmail === deletedData.senderEmail && n.type === deletedData.type && !n.read
        );
        
        if (wasUnread) {
        const currentCount = this.unreadCountSubject.value;
        this.unreadCountSubject.next(Math.max(0, currentCount - 1));
        }

        // 3. Actualizamos la lista visual
        this.notificationsSubject.next(updatedNotifs);
    });
    });
  }

  markAllAsRead(): void {
    const email = this.authService.getCurrentUserEmail();
    if (!email) return;

    this.http.put(`${this.apiUrl}/${email}/read-all`, {}).subscribe(() => {
      this.unreadCountSubject.next(0);
      const updatedNotifs = this.notificationsSubject.value.map(n => ({...n, read: true}));
      this.notificationsSubject.next(updatedNotifs);
    });
  }

  disconnect(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}
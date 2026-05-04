import { Component, Output, EventEmitter, Input, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-chat-window',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chat-window.html',
  styleUrl: './chat-window.css'
})
export class ChatWindow {
  @Output() return = new EventEmitter<void>();

  ForumTitle = '';
  forumId: number | null = null;
  saveUrl: SafeResourceUrl | null = null;
  activeForum = false;

  private sanitizer = inject(DomSanitizer);

  @Input() set selectedForumTitle(value: string) {
    this.ForumTitle = value;
  }

  @Input() set selectedForumId(value: number | null ){
      this.forumId = value

      if (value !== null) {
          this.activateForum();
      } else {
          this.saveUrl = null; 
          this.activeForum = false;
      }

  }

  activateForum() {
    if (this.forumId === null) return;

    this.activeForum = true;
    this.saveUrl = null;

    const targetTopic = `/t/${this.forumId}`;
    const ssoUrl = `https://netlikes.duckdns.org/session/sso?return_path=${encodeURIComponent(targetTopic)}`;
    this.saveUrl = this.sanitizer.bypassSecurityTrustResourceUrl(ssoUrl);
    
    console.log("Cargando foro silenciosamente:", ssoUrl);
  }

// Puedes borrar la función chargeForum() por completo, ya no la necesitamos.

  // activateForum() {
  //   if (this.activeForum && this.forumId !== null) {
  //       this.chargeForum(this.forumId);
  //       return;
  //   }

  //   if (localStorage.getItem('foro_sesion_activa') === 'true') {
  //     this.activeForum = true;
  //     this.chargeForum(this.forumId!);
  //     return;
  //   }

  //   this.saveUrl = null;
  //   this.activeForum = false;

  //   const ssoUrl = 'https://netlikes.duckdns.org/session/sso?return_path=%2Flatest%3Fis_popup%3D1';
  //   const popup = window.open(ssoUrl, 'ForoLogin', 'width=600,height=700');

  //   if (!popup) {
  //       alert("Por favor, permite las ventanas emergentes (pop-ups) en tu navegador.");
  //       return; 
  //   }

  //   setTimeout(() => {
  //       if (popup && !popup.closed) {
  //           popup.close();
  //       }
  //   }, 6000);

  //   const timer = setInterval(() => {
  //     if (!popup || popup.closed) {
  //           clearInterval(timer);
            
  //           localStorage.setItem('foro_sesion_activa', 'true');
            
  //           setTimeout(() => {
  //               this.activeForum = true;
  //               if (this.forumId !== null) {
  //                   this.chargeForum(this.forumId);
  //               }
  //           }, 1000);
  //     }
  //   }, 1000);
  // }

  // chargeForum(forumId: number) {
  //     const originalUrl = `https://netlikes.duckdns.org/t/${forumId}`;
  //     console.log("Angular está intentando meter en el iframe exactamente esta URL:", originalUrl);
  //     this.saveUrl = this.sanitizer.bypassSecurityTrustResourceUrl(originalUrl);
  // }
  
  // @Input() set discourseTopicId(value: number | undefined) {
  //   if (value) {
  //     this.currentTopicId = value;
  //     const url = `${environment.discourseUrl}/t/-/${value}`;
  //     this.safeForumUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
  //   }
  // }

  goBack() {
    this.return.emit();
  }
}
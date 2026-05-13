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
  
  goBack() {
    this.return.emit();
  }
}
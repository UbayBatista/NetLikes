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
  currentTopicId?: number;
  safeForumUrl?: SafeResourceUrl; 

  private sanitizer = inject(DomSanitizer);

  @Input() set selectedForumTitle(value: string) {
    this.ForumTitle = value;
  }
  
  @Input() set discourseTopicId(value: number | undefined) {
    if (value) {
      this.currentTopicId = value;
      const url = `${environment.discourseUrl}/t/-/${value}`;
      this.safeForumUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
    }
  }

  goBack() {
    this.return.emit();
  }
}
import { Component, Input, Output, OnInit, OnDestroy, ChangeDetectorRef, EventEmitter } from "@angular/core";
import { MessageBubble } from '../../message-bubble/message-bubble';
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { AuthService } from "../../../services/auth.service";

interface Menssage{
    user: string;
    text: string;
    itsMe: boolean;
}

@Component({
    selector: "app-social-chats-menssages",
    imports: [MessageBubble],
    standalone: true,
    templateUrl: "./menssages.html",
    styleUrl: "./menssages.css"
})
export class Menssages implements OnInit{

    saveUrl: SafeResourceUrl | null = null;
    activeUser = false;
    chatID: number | null = null;

    @Input() person: string = "";
    @Output() return = new EventEmitter<void>();

    @Input() set selectedUserChat(value: number | null) {
        this.chatID = value;

        if (this.chatID !== null) {
            const targetTopic = `/chat/c/dm/${this.chatID}`; 
            const ssoUrl = `https://netlikes.duckdns.org/session/sso?return_path=${encodeURIComponent(targetTopic)}`;
            
            this.saveUrl = this.sanitizer.bypassSecurityTrustResourceUrl(ssoUrl);
            console.log("Cargando foro silenciosamente:", ssoUrl);
        }else {
          this.saveUrl = null; 
          this.activeUser = false;
      }

      this.cdr.detectChanges();

    }

    constructor(
      private cdr: ChangeDetectorRef, 
      private sanitizer: DomSanitizer,
      private authService: AuthService
    ) {}
    
    ngOnInit() {
       
    }

    goBack() {
        this.return.emit();
    }

}

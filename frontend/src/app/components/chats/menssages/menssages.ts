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

    
    // @Input() user: string="Cristiano"
    // @Input() person: string="Messi"
    @Input() person: string = "";
    @Output() return = new EventEmitter<void>();

    @Input() set selectedUserChat(value: number) {
        this.chatID = value;

        if (value !== null) {
          this.activateUser();
      } else {
          this.saveUrl = null; 
          this.activeUser = false;
      }
    }

    private intervalId: any; 
    private countMessage = 0;
    constructor(
      private cdr: ChangeDetectorRef, 
      private sanitizer: DomSanitizer,
      private authService: AuthService
    ) {}

    activateUser() {
        if (this.chatID === null) return;

        this.activeUser = true;
        this.saveUrl = null;

        const targetTopic = `/chat/c/dm/${this.chatID}`; 
        const ssoUrl = `https://netlikes.duckdns.org/session/sso?return_path=${encodeURIComponent(targetTopic)}`;
        this.saveUrl = this.sanitizer.bypassSecurityTrustResourceUrl(ssoUrl);
        
        console.log("Cargando foro silenciosamente:", ssoUrl);
    }
    
    ngOnInit() {
       
    }

    goBack() {
        this.return.emit();
    }

}

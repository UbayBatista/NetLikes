import { Component, Input, Output, ChangeDetectorRef, EventEmitter } from "@angular/core";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { AuthService } from "../../../services/auth.service";

@Component({
    selector: "app-social-chats-menssages",
    imports: [],
    standalone: true,
    templateUrl: "./menssages.html",
    styleUrl: "./menssages.css"
})
export class Menssages{

    saveUrl: SafeResourceUrl | null = null;
    activeUser = false;
    chatId: number | null = null;

    @Input() person: string = "";
    @Output() return = new EventEmitter<void>();

    @Input() set selectedUserChat(value: number | null) {
        this.chatId = value;

        if (this.chatId !== null) {
            const targetTopic = `/chat/c/dm/${this.chatId}`; 
            const ssoUrl = `https://netlikes.duckdns.org/session/sso?return_path=${encodeURIComponent(targetTopic)}`;
            
            setTimeout(() => {
                this.saveUrl = this.sanitizer.bypassSecurityTrustResourceUrl(ssoUrl);
                console.log("Cargando chat silenciosamente:", ssoUrl);
                this.cdr.detectChanges();
            }, 10);
        }else {
          this.saveUrl = null; 
          this.activeUser = false;
          this.cdr.detectChanges();
      }

      this.cdr.detectChanges();

    }

    constructor(
      private cdr: ChangeDetectorRef, 
      private sanitizer: DomSanitizer,
      private authService: AuthService
    ) {}
    
    goBack() {
        this.return.emit();
    }
}

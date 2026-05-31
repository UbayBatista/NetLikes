import { Component, Input, Output, EventEmitter, ChangeDetectorRef } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router } from "@angular/router";
import { AuthService } from "../../services/auth.service";
import { ChatService } from "../../services/chat.service";

@Component({
  selector: "app-profile-header",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./profile-header.html",
  styleUrl: "./profile-header.css"
})
export class ProfileHeader {
  @Input() userPicture: string | null = null;
  @Input() type: string = "Editar Perfil";
  @Input() userName: string = '';
  @Input() isPrivate: boolean = false;
  @Input() isEditing: boolean = false;
  @Input() otherUser: boolean = false;
  @Input() followers: number = 0;
  @Input() following: number = 0;

  @Output() privacyChange = new EventEmitter<boolean>();
  @Output() logOut = new EventEmitter<void>();
  @Output() editClick = new EventEmitter<void>();
  @Output() followClick = new EventEmitter<void>();
  @Output() openSocialModal = new EventEmitter<'Seguidores' | 'Seguidos'>();
  @Output() block = new EventEmitter<void>();
  @Output() openBlockedModal = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
  @Output() changeAvatar = new EventEmitter<void>();
  @Output() changePassword = new EventEmitter<void>();
  
  openMenu: boolean = false;
  mensajeErrorChat: string | null = null;

  constructor(
    private chatService: ChatService,
    private router: Router, 
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  toggleMenu() {
    this.openMenu = !this.openMenu;
  }

  handleMainAction() {
    if (!this.otherUser) {
      this.editClick.emit();
    } else {
      this.followClick.emit();
    }
  }

  openSocial(type: 'Seguidores' | 'Seguidos') {
    this.openSocialModal.emit(type);
  }

  togglePrivacy() {
    this.privacyChange.emit(!this.isPrivate);
  }

  goToBadges() {
    this.router.navigate(['/badges']);
    this.toggleMenu();
  }

  logout() {
    this.logOut.emit();
  }

  applyBlock() {
    this.block.emit();
    this.toggleMenu();
  }

  showBlockedUsers() {
    this.openBlockedModal.emit();
    this.toggleMenu();
  }

  deleteUser() {
    this.delete.emit();
    this.toggleMenu();
  }

  startChat() {
    this.authService.getCurrentUser().subscribe(user => { 
      if (!user) return;

      const myUser = user.userName
      const userFriend = this.userName;
      
      this.chatService.getChatId(myUser, userFriend).subscribe({
          next: (chatId) => {
            this.router.navigate(['/social'], { 
              queryParams: { 
                chatWith: this.userName, 
                chatId: chatId,
                mode: 'Chats'
              } 
            });
          },
          error: (err) => {
            if (err.status === 400) {
              this.mensajeErrorChat = err.error.error || "¡Os tenéis que seguir mutuamente para poder hablar!";
            } else if (err.status === 404) {
              this.mensajeErrorChat = "Este amigo aún no ha activado su chat en el foro.";
            } 
            
            this.cdr.detectChanges();

            if (this.mensajeErrorChat) {
                setTimeout(() => {
                  this.mensajeErrorChat = null;
                  this.cdr.detectChanges();
                }, 3500);
            }

          }
        });
    });
  }

  openAvatarModal() {
    this.changeAvatar.emit();
  }

  changePasswordRequest() {
    this.changePassword.emit();
    this.toggleMenu();
  }
}
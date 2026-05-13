import { Component, Input, Output, EventEmitter, inject } from "@angular/core";
import { CommonModule } from "@angular/common";
import { HttpClient } from "@angular/common/http";
import { Router } from "@angular/router";
import { AuthService } from "../../services/auth.service";

@Component({
  selector: "app-profile-header",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./profile-header.html",
  styleUrl: "./profile-header.css"
})
export class ProfileHeader {
  @Input() userName: string = '';
  @Input() isPrivate: boolean = false;
  @Input() type: string = "Editar Perfil";
  @Input() otherUser: boolean = false;
  @Input() followers: number = 0;
  @Input() following: number = 0;

  private _userPicture: string | null = null;
  @Input() set userPicture(value: string | null) {
    this._userPicture = value || 'assets/ProfilePicture.jpg';
  }
  get userPicture(): string {
    return this._userPicture || 'assets/ProfilePicture.jpg';
  }

  @Output() privacyChange = new EventEmitter<boolean>();
  @Output() logOut = new EventEmitter<void>();
  @Output() editClick = new EventEmitter<void>();
  @Output() followClick = new EventEmitter<void>();
  @Output() openSocialModal = new EventEmitter<'Seguidores' | 'Seguidos'>();
  @Output() block = new EventEmitter<void>();
  @Output() openBlockedModal = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
  
  openMenu: boolean = false;

  constructor(
    private http: HttpClient, 
    private router: Router, 
    private authService: AuthService
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

  starChat() {
  const myUser = this.authService.getCurrentUser(); 
  const userFriend = this.userName;

  this.http.get<number>(`https://api-db.duckdns.org/api/chat/id?miUsuario=${myUser}&otroUsuario=${userFriend}`)
    .subscribe({
      next: (chatId) => {
        this.router.navigate(['/social/chats'], { 
          queryParams: { 
            chatWith: userFriend, 
            chatId: chatId 
          } 
        });
      },
      error: (err) => console.error("Error al obtener/crear el chat", err)
    });
}

}
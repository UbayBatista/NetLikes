import { Component, Input, Output, EventEmitter } from "@angular/core";
import { CommonModule } from "@angular/common";

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
  @Input() option: string = "Ajustes";
  @Input() otherUser: 'Yes' | 'No' = 'No';
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
  
  openMenu: boolean = false;

  toggleMenu() {
    this.openMenu = !this.openMenu;
  }

  handleMainAction() {
    if (this.otherUser === 'No') {
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
      this.logOut.emit()
  }
}
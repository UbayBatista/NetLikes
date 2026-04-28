import { Component, Input, Output, EventEmitter} from "@angular/core";

@Component({
    selector: "app-profile-header",
    standalone: true,
    imports:[],
    templateUrl: "./profile-header.html",
    styleUrl: "./profile-header.css"
})
export class ProfileHeader{
    @Input() userName: string = '';
    @Input() userPicture: string = 'Error';
    @Input() type: string = "Editar Perfil";
    @Input() option: string = "Ajustes";
    @Input() otherUser: string = 'No';
    @Output() logOut = new EventEmitter<void>();
    openMenu: boolean = false;

    @Output() openSocialModal = new EventEmitter<'Seguidores' | 'Seguidos'>();

    Menu(){
        this.openMenu = !this.openMenu;
    }

    logout() {
        this.logOut.emit()
    }

    @Output() editClick = new EventEmitter<void>();
    onEdit() {
        this.editClick.emit();
    }

    openSocial(type: 'Seguidores' | 'Seguidos') {
        this.openSocialModal.emit(type);
    }
}
import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { NotificationPanel } from '../notification-panel/notification-panel';
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs';
import { User } from '../../models/user.models';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-header',
  imports: [RouterLink, NotificationPanel, AsyncPipe],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  currentUser$: Observable<User | null>;
  hasNotifications: boolean = true;
  showNotifications: boolean = false;
  userName = "my-profile"

  constructor(private router: Router, private authService: AuthService) {
    this.currentUser$ = this.authService.getCurrentUser();
  }

  goToProfile() {
    this.router.navigate(['/profile', this.userName]);
  }

  notifications = [
    { image: 'https://revsportz.in/wp-content/uploads/2023/06/Benzema.jpeg', message: 'Benzema ha aceptado tu solicitud de seguimiento.' },
    { image: '', message: 'Te han mencionado en "Los Juegos del Hambre".' },
    { image: '', message: 'Te han mencionado en "Avatar".' },
    { image: 'https://wallpaperaccess.com/full/1264961.jpg', message: 'La Roca te ha recomendado "Rocky".' },
    { image: 'https://i.pinimg.com/736x/f6/28/85/f628856f2e45836d5850d959a1ed5c75.jpg', message: 'Cristiano Ronaldo te ha recomendado "Bichos".' },
    { image: 'https://cdn2.telediario.mx/uploads/media/2022/12/18/lionel-messi-besa-copa-recibir.jpg', message: 'Messi te ha recomendado "Pulgarcito".' },
    { image: 'https://wallpaperaccess.com/full/1264961.jpg', message: 'La Roca ha solicitado seguirte.' },
    { image: 'https://i.pinimg.com/736x/f2/d2/ac/f2d2accd122995993f561826b606d512.jpg', message: 'Luis Suárez te ha recomendado "Mordiscos Peligrosos".' },
    { image: 'https://i.pinimg.com/736x/f6/28/85/f628856f2e45836d5850d959a1ed5c75.jpg', message: 'Cristiano Ronaldo ha solicitado seguirte.' },
    { image: 'https://cdn2.telediario.mx/uploads/media/2022/12/18/lionel-messi-besa-copa-recibir.jpg', message: 'Messi ha solicitado seguirte.' },
    { image: 'https://i.pinimg.com/736x/f2/d2/ac/f2d2accd122995993f561826b606d512.jpg', message: 'Luis Suárez ha solicitado seguirte.' },
  ]

  boxOpen = false;

  openBox() {
    this.boxOpen = true;
    this.hasNotifications = false;
  }
}
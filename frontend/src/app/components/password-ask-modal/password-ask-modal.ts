import { Component, EventEmitter, inject, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-password-verify-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './password-ask-modal.html',
  styleUrl: './password-ask-modal.css'
})
export class PasswordVerifyModalComponent {
  @Output() close = new EventEmitter<void>();
  @Output() verified = new EventEmitter<void>();

  private readonly userService = inject(UserService);

  password = '';
  errorMessage = '';
  isLoading = false;

  verify() {
    if (!this.password) {
      this.errorMessage = 'Por favor, introduce tu contraseña.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.userService.verifyPassword(this.password).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.valid) {
          this.verified.emit();
        }
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 401) {
          this.errorMessage = 'Contraseña incorrecta. Inténtalo de nuevo.';
        } else {
          this.errorMessage = 'Error en el servidor. Inténtalo más tarde.';
        }
      }
    });
  }

  onClose() {
    this.close.emit();
  }
}
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirmation-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation-modal.html',
  styleUrls: ['./confirmation-modal.css']
})
export class ConfirmationModalComponent {
  @Input() message: string = '';
  @Output() result = new EventEmitter<boolean>();

  confirm() {
    this.result.emit(true);
  }

  cancel() {
    this.result.emit(false);
  }
}
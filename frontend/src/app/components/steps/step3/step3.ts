import { Component, Output, EventEmitter, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-step3',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './step3.html',
  styleUrls: ['../steps.css', './step3.css']
})
export class Step3 {
  @Input() initialStatus: boolean = false;
  @Output() toNext = new EventEmitter<boolean>();
  @Output() toPrev = new EventEmitter<void>();

  termsAccepted: boolean = false;

  ngOnInit() {
    this.termsAccepted = this.initialStatus;
  }

  notifyNext() {
    if (this.termsAccepted) {
      this.toNext.emit(this.termsAccepted);
    } else {
      alert('Debes aceptar los términos y condiciones para continuar.');
    }
  }

  notifyPrev() {
    this.toPrev.emit();
  }
}
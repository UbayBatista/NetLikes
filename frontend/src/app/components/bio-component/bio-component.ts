import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-bio-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bio-component.html',
  styleUrl: './bio-component.css'
})
export class BioComponent implements OnChanges {
  @Input() bio: string = '';
  @Input() isEditing: boolean = false;
  @Output() bioSave = new EventEmitter<{ bio: string, hasChanges: boolean }>();
  
  isEditingBio: boolean = false;
  editableBio: string = '';
  pendingBio: string = '';
  hasChanges: boolean = false;

  toggleBioEdit() {
    if (this.isEditingBio) {
      this.pendingBio = this.editableBio;
      this.hasChanges = this.pendingBio !== this.bio;
      this.isEditingBio = false;
      this.bioSave.emit({ bio: this.pendingBio, hasChanges: this.hasChanges });
    } else {
      this.editableBio = this.pendingBio || this.bio;
      this.isEditingBio = true;
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['bio'] && changes['bio'].firstChange) {
      this.editableBio = this.bio;
      this.pendingBio = this.bio;
    }

    if (changes['isEditing'] && !this.isEditing) {
      this.isEditingBio = false;
      this.editableBio = this.bio;
      this.pendingBio = this.bio;
      this.hasChanges = false;
    }
  }

  discardChanges() {
    this.pendingBio = this.bio;
    this.editableBio = this.bio;
    this.hasChanges = false;
  }

  updateOriginalBio(newBio: string) {
    this.bio = newBio;
    this.pendingBio = newBio;
    this.editableBio = newBio;
    this.hasChanges = false;
  }
  
  onBioInput(value: string) {
    this.editableBio = value;
  }
}
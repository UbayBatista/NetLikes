import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BioComponent } from './bio-component';
import { SimpleChange } from '@angular/core';

describe('BioComponent', () => {
  let component: BioComponent;
  let fixture: ComponentFixture<BioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BioComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(BioComponent);
    component = fixture.componentInstance;
    component.bio = 'Bio original';
    component.isEditing = false;
  });

  it('should initialize editableBio and pendingBio with bio on first change', () => {
    component.ngOnChanges({
      bio: new SimpleChange(null, 'Bio original', true)
    });

    expect(component.editableBio).toBe('Bio original');
    expect(component.pendingBio).toBe('Bio original');
  });

  it('should open textarea with current pendingBio when pencil is clicked', () => {
    component.pendingBio = 'Bio original';
    component.isEditing = false;
    component.toggleBioEdit();
    fixture.detectChanges();

    expect(component.editableBio).toBe('Bio original');
    expect(component.isEditingBio).toBe(true);
  });

  it('should save editableBio as pendingBio and close textarea when check is clicked', () => {
    component.isEditingBio = true;
    component.editableBio = 'Nueva bio';
    component.bio = 'Bio original';

    component.toggleBioEdit();

    expect(component.isEditingBio).toBe(false);
    expect(component.pendingBio).toBe('Nueva bio');
    expect(component.hasChanges).toBe(true);
  });

  it('should set hasChanges to false when check is clicked with same bio', () => {
    component.isEditingBio = true;
    component.editableBio = 'Bio original';
    component.bio = 'Bio original';

    component.toggleBioEdit();

    expect(component.hasChanges).toBe(false);
  });

  it('should emit bioSave with new bio and hasChanges true when check is clicked with changes', () => {
    const bioSaveSpy = vi.spyOn(component.bioSave, 'emit');
    component.isEditingBio = true;
    component.editableBio = 'Nueva bio';
    component.bio = 'Bio original';

    component.toggleBioEdit();

    expect(bioSaveSpy).toHaveBeenCalledWith({ bio: 'Nueva bio', hasChanges: true });
  });

  it('should emit bioSave with hasChanges false when check is clicked without changes', () => {
    const bioSaveSpy = vi.spyOn(component.bioSave, 'emit');
    component.isEditingBio = true;
    component.editableBio = 'Bio original';
    component.bio = 'Bio original';

    component.toggleBioEdit();

    expect(bioSaveSpy).toHaveBeenCalledWith({ bio: 'Bio original', hasChanges: false });
  });

  it('should update editableBio when user types in textarea', () => {
    component.onBioInput('Texto nuevo');

    expect(component.editableBio).toBe('Texto nuevo');
  });

  it('should reset state when isEditing changes to false', () => {
    component.isEditingBio = true;
    component.hasChanges = true;
    component.pendingBio = 'Bio pendiente';
    component.editableBio = 'Bio pendiente';

    component.ngOnChanges({
      isEditing: new SimpleChange(true, false, false)
    });

    expect(component.isEditingBio).toBe(false);
    expect(component.editableBio).toBe('Bio original');
    expect(component.pendingBio).toBe('Bio original');
    expect(component.hasChanges).toBe(false);
  });

  it('should restore original bio when discardChanges is called', () => {
    component.pendingBio = 'Bio pendiente';
    component.editableBio = 'Bio pendiente';
    component.hasChanges = true;

    component.discardChanges();

    expect(component.pendingBio).toBe('Bio original');
    expect(component.editableBio).toBe('Bio original');
    expect(component.hasChanges).toBe(false);
  });
});
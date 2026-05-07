import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';

import { ConfirmationModalComponent } from './confirmation-modal';

describe('ConfirmationModalComponent', () => {
  let component: ConfirmationModalComponent;
  let fixture: ComponentFixture<ConfirmationModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfirmationModalComponent, CommonModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmationModalComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create the component successfully', () => {
      expect(component).toBeTruthy();
    });

    it('should display the input message correctly', () => {
      const testMessage = '¿Estás seguro de borrar este elemento?';
      component.message = testMessage;
      fixture.detectChanges();

      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.modal-message')?.textContent).toContain(testMessage);
    });
  });

  describe('Confirmation Logic', () => {
    it('should emit true when confirm() is called', () => {
      const emitSpy = vi.spyOn(component.result, 'emit');
      
      component.confirm();

      expect(emitSpy).toHaveBeenCalledWith(true);
    });

    it('should emit true when the confirm button is clicked', () => {
      const emitSpy = vi.spyOn(component.result, 'emit');
      const confirmBtn = fixture.nativeElement.querySelector('.btn-confirm');
      
      confirmBtn.click();

      expect(emitSpy).toHaveBeenCalledWith(true);
    });
  });

  describe('Cancel Logic', () => {
    it('should emit false when cancel() is called', () => {
      const emitSpy = vi.spyOn(component.result, 'emit');
      
      component.cancel();

      expect(emitSpy).toHaveBeenCalledWith(false);
    });

    it('should emit false when the cancel button is clicked', () => {
      const emitSpy = vi.spyOn(component.result, 'emit');
      const cancelBtn = fixture.nativeElement.querySelector('.btn-cancel');
      
      cancelBtn.click();

      expect(emitSpy).toHaveBeenCalledWith(false);
    });

    it('should emit false when clicking on the overlay', () => {
      const emitSpy = vi.spyOn(component.result, 'emit');
      const overlay = fixture.nativeElement.querySelector('.modal-overlay');
      
      overlay.click();

      expect(emitSpy).toHaveBeenCalledWith(false);
    });
  });
});
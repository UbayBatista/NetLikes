import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Step3 } from './step3';
import { FormsModule } from '@angular/forms';

describe('Step3 Component', () => {
  let component: Step3;
  let fixture: ComponentFixture<Step3>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Step3, FormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(Step3);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Initialization', () => {
    it('should create the component successfully', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('Terms and Conditions Logic', () => {
    it('should display an alert when terms are not accepted', () => {
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});
      
      component.termsAccepted = false;
      component.notifyNext();

      expect(alertSpy).toHaveBeenCalledWith('Debes aceptar los términos y condiciones para continuar.');
      
      alertSpy.mockRestore();
    });

    it('should emit toNext event when terms are accepted', () => {
      const emitSpy = vi.spyOn(component.toNext, 'emit');
      
      component.termsAccepted = true;
      component.notifyNext();

      expect(emitSpy).toHaveBeenCalled();
    });
  });

  describe('Navigation Logic', () => {
    it('should emit toPrev event when previous button is clicked', () => {
      const emitSpy = vi.spyOn(component.toPrev, 'emit');
      
      component.notifyPrev();

      expect(emitSpy).toHaveBeenCalled();
    });
  });
});
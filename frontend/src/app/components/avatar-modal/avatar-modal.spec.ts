import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AvatarModal } from './avatar-modal';
import { vi } from 'vitest';

describe('AvatarModal', () => {
  let component: AvatarModal;
  let fixture: ComponentFixture<AvatarModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AvatarModal]
    }).compileComponents();

    fixture = TestBed.createComponent(AvatarModal);
    component = fixture.componentInstance;
    fixture.detectChanges(); // Ejecuta el ciclo inicial de Angular y renderiza el HTML
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with 20 avatars generated from seeds', () => {
    expect(component.avatars.length).toBe(20);
    expect(component.avatars[0].seed).toBe('CyberPunk');
    expect(component.avatars[0].url).toContain('https://api.dicebear.com/9.x/fun-emoji/svg?seed=CyberPunk');
  });

  describe('UI Rendering', () => {
    it('should render all 20 avatar images in the grid', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const images = compiled.querySelectorAll('.avatar-grid img');
      
      expect(images.length).toBe(20);
      expect(images[0].getAttribute('alt')).toBe('CyberPunk');
    });

    it('should keep the confirm button disabled if no avatar is selected', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const confirmBtn = compiled.querySelector('.btn-confirm') as HTMLButtonElement;
      
      expect(confirmBtn.disabled).toBe(true);
    });
  });

  describe('Interaction & Logic', () => {
    it('should set selectedSeed when an avatar option is clicked', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      const firstAvatarOption = compiled.querySelector('.avatar-option') as HTMLElement;

      firstAvatarOption.click();
      fixture.detectChanges();

      expect(component.selectedSeed).toBe('CyberPunk');
      expect(firstAvatarOption.classList.contains('selected')).toBe(true);
    });

    it('should emit avatarSelected and trigger cancel when confirm is called with a selection', () => {
      const avatarSpy = vi.spyOn(component.avatarSelected, 'emit');
      const closeSpy = vi.spyOn(component.close, 'emit');
      
      component.selectedSeed = 'NeoTokyo';
      
      component.confirm();

      expect(avatarSpy).toHaveBeenCalledWith('NeoTokyo');
      expect(closeSpy).toHaveBeenCalled();
      expect(component.selectedSeed).toBe('');
    });

    it('should not emit avatarSelected if confirm is called without a selection', () => {
      const avatarSpy = vi.spyOn(component.avatarSelected, 'emit');
      component.selectedSeed = '';

      component.confirm();

      expect(avatarSpy).not.toHaveBeenCalled();
    });

    it('should clear selectedSeed and emit close when cancel is called', () => {
      const closeSpy = vi.spyOn(component.close, 'emit');
      component.selectedSeed = 'GalaxyKid';

      component.cancel();

      expect(component.selectedSeed).toBe('');
      expect(closeSpy).toHaveBeenCalled();
    });

    it('should call cancel when clicking on the modal overlay', () => {
      const cancelSpy = vi.spyOn(component, 'cancel');
      const compiled = fixture.nativeElement as HTMLElement;
      const overlay = compiled.querySelector('.modal-overlay') as HTMLElement;

      overlay.click();

      expect(cancelSpy).toHaveBeenCalled();
    });
  });
});
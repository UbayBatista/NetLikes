import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

const AVATAR_SEEDS = [
  'CyberPunk',
  'PopCorn',
  'NeoTokyo',
  'GalaxyKid',
  'PixelHero',
  'RetroWave',
  'MoonWalker',
  'CinemaGhost',
  'ElectricFox',
  'AstroCat',
  'NightDrive',
  'DragonByte',
  'GhostFrame',
  'VelvetStorm',
  'CosmoDuck',
  'SolarFlare',
  'DarkComet',
  'BubbleVibe',
  'TurboWolf',
  'CherryNova'
];

@Component({
  selector: 'app-avatar-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './avatar-modal.html',
  styleUrl: './avatar-modal.css'
})
export class AvatarModal {
  @Output() close = new EventEmitter<void>();
  @Output() avatarSelected = new EventEmitter<string>();

  avatars = AVATAR_SEEDS.map(seed => ({
    seed,
    url: `https://api.dicebear.com/9.x/fun-emoji/svg?seed=${seed}`
  }));

  selectedSeed: string = '';

  selectAvatar(seed: string) {
    this.selectedSeed = seed;
  }

  confirm() {
    if (this.selectedSeed) {
      this.avatarSelected.emit(this.selectedSeed);
      this.cancel();
    }
  }

  cancel() {
    this.selectedSeed = '';
    this.close.emit();
  }
}
import { Component } from '@angular/core';
import { BadgesWindow } from '../../components/badges-window/badges-window';

@Component({
  selector: 'app-badges',
  standalone: true,
  imports: [ BadgesWindow ],
  templateUrl: './badges.html',
  styleUrl: './badges.css',
})
export class Badges {
  
}

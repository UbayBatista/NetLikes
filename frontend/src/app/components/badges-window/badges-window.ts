import { Component, inject, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-badges-window',
  standalone: true,
  templateUrl: './badges-window.html',
  styleUrl: './badges-window.css',
})
export class BadgesWindow implements OnInit {
  badgesUrl: SafeResourceUrl | null = null;
  private sanitizer = inject(DomSanitizer);

  ngOnInit() {
    const targetPath = '/badges';
    const ssoUrl = `https://netlikes.duckdns.org/session/sso?return_path=${encodeURIComponent(targetPath)}`;
    
    this.badgesUrl = this.sanitizer.bypassSecurityTrustResourceUrl(ssoUrl);
  }
}

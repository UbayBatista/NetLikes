import { Component, signal, inject, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { AsyncPipe } from '@angular/common';
import { Footer } from "./components/footer/footer";
import { Header } from "./components/header/header";
import { filter, take, skip } from 'rxjs/operators';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Footer, Header, AsyncPipe],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('NetLikes');
  public authService = inject(AuthService);
  private router = inject(Router);
  public showLayout = signal(true);

  ngOnInit() {
    this.authService.getCurrentUser().pipe(
        skip(1),
        take(1)
    ).subscribe(user => {
        if (!user) {
            this.router.navigate(['/login']);
        }
    });

    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      const noLayoutRoutes = ['/login', '/register'];
      const currentUrl = event.urlAfterRedirects.split('?')[0];
      this.showLayout.set(!noLayoutRoutes.includes(currentUrl));
    });
  }
}

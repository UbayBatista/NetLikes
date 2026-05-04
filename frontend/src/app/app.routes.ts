import { Routes } from '@angular/router';
import { Welcome } from './pages/welcome/welcome';
import { Home } from './pages/home/home';
import { Catalog } from './pages/catalog/catalog';
import { Forum } from './pages/forum/forum';
import { ProfileComplete } from './pages/profile/profile-body';
import { Social } from './pages/social/social';
import { FilmDetail } from './pages/film-detail/film-detail';
import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';
import { ForumSsoComponent } from './forum-sso/forum-sso';

export const routes: Routes = [
  { path: 'login', component: Welcome, canActivate: [guestGuard] },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: 'home', component: Home },
      { path: 'catalog', component: Catalog },
      { path: 'forum', component: Forum },
      { path: 'profile/:username', component: ProfileComplete },
      { path: 'social', component: Social },
      { path: 'film-details/:id', component: FilmDetail },
      { path: 'forum-sso', component: ForumSsoComponent }
    ]
  },
  { path: '**', redirectTo: 'login'}
];

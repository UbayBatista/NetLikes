import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Genre } from '../../components/genre/genre';
import { Film } from '../../components/film/film';
import { GenreGroup } from '../../models/film.models';
import { Recommendations } from '../../services/recommendations';
import { RecommendService } from '../../services/recommend.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrl: './home.css',
  imports: [Genre, Film]
})
export class Home implements OnInit {
  tabActive: string = 'paraTi';

  forYouFilms: GenreGroup[] = [];
  users_films: any[] = []; 
  
  constructor(
    private cdr: ChangeDetectorRef, 
    private recommendationsService: Recommendations,
    private recFollowedService: RecommendService
  ) {}

  ngOnInit() {
    this.loadFilms();
  }

  loadFilms() {
    this.recFollowedService.getRecommendedFilmsWithCount().subscribe({
      next: (data) => {
        this.users_films = data.map(item => ({
          id: item.film.id,
          title: item.film.title,
          posterPath: item.film.posterPath,
          recommendations: item.count
        }));
        this.cdr.detectChanges();
      }
    });

    this.recommendationsService.getRecommendations().subscribe({
      next: (data: GenreGroup[]) => {
        this.forYouFilms = data;
        this.cdr.detectChanges();
      }
    });
  }

  switchTab(tab: string) {
    this.tabActive = tab;
  }
}
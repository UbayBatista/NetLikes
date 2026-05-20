import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Home } from './home';
import { FilmService } from '../../services/film.service';
import { Recommendations } from '../../services/recommendations';
import { RecommendationFollowedService } from '../../services/recommend.service';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('Home Component', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;
  
  let recommendationsServiceMock: any;
  let recFollowedServiceMock: any;

  beforeEach(async () => {
    recommendationsServiceMock = {
      getRecommendations: vi.fn().mockReturnValue(of([]))
    };

    recFollowedServiceMock = {
      getRecommendedFilmsWithCount: vi.fn().mockReturnValue(of([
        { film: { id: 10, title: 'Inception', posterPath: '/path' }, count: 5 }
      ]))
    };

    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [
        { provide: FilmService, useValue: {} },
        { provide: Recommendations, useValue: recommendationsServiceMock },
        { provide: RecommendationFollowedService, useValue: recFollowedServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
  });

  describe('US 10.1: Display Recommendations From Users', () => {
    it('should fetch and format user recommendations on init', () => {
      fixture.detectChanges();

      expect(recFollowedServiceMock.getRecommendedFilmsWithCount).toHaveBeenCalled();
      
      expect(component.users_films.length).toBe(1);
      expect(component.users_films[0].title).toBe('Inception');
      expect(component.users_films[0].recommendations).toBe(5);
    });

    it('should switch between tabs', () => {
      expect(component.tabActive).toBe('paraTi');

      component.switchTab('usuarios');
      expect(component.tabActive).toBe('usuarios');
    });
  });
});
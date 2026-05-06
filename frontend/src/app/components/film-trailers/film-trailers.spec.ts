import { describe, it, expect, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SimpleChange } from '@angular/core';

import { FilmTrailers } from './film-trailers';

describe('FilmTrailers Component', () => {
  let component: FilmTrailers;
  let fixture: ComponentFixture<FilmTrailers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FilmTrailers],
    }).compileComponents();

    fixture = TestBed.createComponent(FilmTrailers);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create the component successfully', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('Rendering Logic', () => {
    it('should render the trailers section and iframes if the videos array has items', () => {
      component.videos = ['abc', '123'];
      component.ngOnChanges({
        videos: new SimpleChange(null, component.videos, true)
      });
      fixture.detectChanges();

      const compiled = fixture.nativeElement;
      expect(compiled.querySelector('.trailers-section')).toBeTruthy();
      expect(compiled.querySelectorAll('iframe').length).toBe(2);
    });

    it('should not render anything if the videos array is empty', () => {
      component.videos = [];
      component.ngOnChanges({
        videos: new SimpleChange(null, [], true)
      });
      fixture.detectChanges();

      const compiled = fixture.nativeElement;
      expect(compiled.querySelector('.trailers-section')).toBeFalsy();
      expect(compiled.querySelector('h3')).toBeFalsy();
    });
  });
});
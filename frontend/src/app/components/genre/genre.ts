import { Component, ViewChild, ElementRef,ChangeDetectorRef, Input } from '@angular/core';
import { Film } from '../film/film';
import { CommonModule } from '@angular/common';
import { GenreGroup } from '../../models/film.models';

@Component({
  selector: 'app-genre',
  standalone: true,
  imports: [Film, CommonModule],
  templateUrl: './genre.html',
  styleUrl: './genre.css',
})
export class Genre {

  @Input() genre!: GenreGroup;

  canScrollLeft = false;
  canScrollRight = true;

  constructor(private cdr: ChangeDetectorRef) {}

  @ViewChild('scrollContainer') scrollContainer!: ElementRef<HTMLDivElement>;

  scrollLeft() {
  const el = this.scrollContainer.nativeElement;

  el.scrollBy({
    left: -300,
    behavior: 'smooth'
  });

  setTimeout(() => this.updateScrollButtons(), 300);
}

  scrollRight() {
    const el = this.scrollContainer.nativeElement;

    el.scrollBy({
      left: 300,
      behavior: 'smooth'
    });

    setTimeout(() => this.updateScrollButtons(), 300);
  }

  updateScrollButtons() {
    const el = this.scrollContainer.nativeElement;

    this.canScrollLeft = el.scrollLeft > 0;
    this.canScrollRight = el.scrollLeft + el.clientWidth < el.scrollWidth;

    this.cdr.detectChanges();
  }

  ngAfterViewInit() {
    const el = this.scrollContainer.nativeElement;

    this.updateScrollButtons();

    el.addEventListener('scroll', () => this.updateScrollButtons(), { passive: true });
    el.addEventListener('wheel', () => this.updateScrollButtons(), { passive: true });
    el.addEventListener('touchmove', () => this.updateScrollButtons(), { passive: true });
  }
}


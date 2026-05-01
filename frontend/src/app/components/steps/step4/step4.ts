import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { GenreService } from '../../../services/genre.service';
import { Genre } from '../../../models/genre.models';
import { CommonModule } from '@angular/common';
import { signal } from '@angular/core';

@Component({
  selector: 'app-step4',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './step4.html',
  styleUrls: ['../steps.css', './step4.css']
})
export class Step4 implements OnInit{
  @Output() toEnd = new EventEmitter<any>();
  @Output() toPrev = new EventEmitter<void>();

  generos = signal<Genre[]>([]);

  constructor(private genreService: GenreService) {}

  ngOnInit() {
    this.genreService.getAllGenres().subscribe(data => {
      this.generos.set(data.map(g => ({ ...g, selected: false })));
    });
  }

  get totalSelected(): number {
    return this.generos().filter(g => g.selected).length;
  }

  toggleGenero(index: number) {
    const current = this.generos();
    current[index].selected = !current[index].selected;
    this.generos.set([...current]);
  }

  handleEnd() {
    const seleccionIds = this.generos()
    .filter(g => g.selected)
    .map(g => g.id); 

    this.toEnd.emit(seleccionIds);
  }
}
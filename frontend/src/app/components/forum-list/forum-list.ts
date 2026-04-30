import { Component, Output, EventEmitter, signal, OnInit, ChangeDetectorRef} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SearchBarComponent } from '../search-bar/search-bar';
import { SubscriptionService } from '../../services/subscription';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forum-list',
  standalone: true,
  imports: [FormsModule, SearchBarComponent],
  templateUrl: './forum-list.html',
  styleUrl: './forum-list.css'
})
export class ForumList implements OnInit{

  @Output() clickedForum = new EventEmitter<{title: string, forumId: number}>(); 
  constructor(private subscriptionService: SubscriptionService, 
    private cdr: ChangeDetectorRef,
    private authService: AuthService) {}

  search = signal('');

  filmsForum: any[] = [];

  searchText = '';

  handleSearch(text: string) {
      this.searchText = text.toLowerCase();
  }

  get filtereForums(){
      const searchLow = this.searchText.toLowerCase();
      return this.filmsForum.filter(forum => 
          forum.title.toLowerCase().includes(searchLow)
      );
  } 

  selectForum(index: number) {
    this.filtereForums.forEach(p => p.active = false);
    this.filtereForums[index].active = true;
    
    this.clickedForum.emit({ title: this.filtereForums[index].title, forumId: this.filtereForums[index].forumTopicId});
    console.log('Cambiando al foro de:', this.filtereForums[index].title, "con ide: ", this.filtereForums[index].forumTopicId);
  }

  ngOnInit() {
    this.authService.getCurrentUser().subscribe(user => { 

      if (!user || !user.email) {
        alert('¡Debes iniciar sesión para poder suscribirte a un foro!');
        return; 
      }
      console.log('Usuario detectado:', user.email, 'Enviando petición a Spring Boot...')

      this.subscriptionService.getUserSubscription(user.email).subscribe({
        next: (data) => {
          // "data" es el array que nos devuelve Spring Boot con todo el árbol (Suscripcion -> Foro -> Pelicula)
          
          this.filmsForum = data.map((sub, index) => {
            return {
              title: sub.forum.film.title,      
              active: index === 0,         
              
              forumTopicId: sub.forum.forumId,
              filmId: sub.forum.film.id
            };
          });

          this.cdr.detectChanges()

          console.log('Foros cargados desde la base de datos:', this.filmsForum);
        },
        error: (err) => {
          console.error('Error al obtener los foros:', err);
        }
      });
    });
  }


}
import { Component, Output, EventEmitter, signal, computed, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SearchBarComponent } from '../search-bar/search-bar';
import { SubscriptionService } from '../../services/subscription.service';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-forum-list',
  standalone: true,
  imports: [FormsModule, SearchBarComponent],
  templateUrl: './forum-list.html',
  styleUrl: './forum-list.css'
})
export class ForumList implements OnInit{

  @Output() clickedForum = new EventEmitter<{title: string, topicId: number}>(); 
  
  private subscriptionService = inject(SubscriptionService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);
  private route = inject(ActivatedRoute);

  filmsForum = signal<any[]>([]);
  searchText = signal('');

  filteredForums = computed(() => {
    const searchLow = (this.searchText() || '').toLowerCase();
    return this.filmsForum().filter(forum => 
      (forum.title || '').toLowerCase().includes(searchLow)
    );
  });

  handleSearch(text: string) {
      this.searchText.set(text.toLowerCase());
  }

  selectForum(index: number) {
    const currentForums = this.filmsForum();
    currentForums.forEach(p => p.active = false);
    
    const selected = this.filteredForums()[index];
    if (selected) {
      selected.active = true;
      this.clickedForum.emit({ 
        title: selected.title, 
        topicId: selected.forumTopicId
      });
      console.log('Cambiando al foro de:', selected.title, "con ID: ", selected.forumTopicId);
    }
    
    this.filmsForum.set([...currentForums]);
    this.cdr.detectChanges(); 
  }

  ngOnInit() {
    this.authService.getCurrentUser().subscribe(user => { 

      // if (!user || !user.email) {
      //   alert('¡Debes iniciar sesión para poder suscribirte a un foro!');
      //   return; 
      // }
      // console.log('Usuario detectado:', user.email, 'Enviando petición a Spring Boot...')

      // this.subscriptionService.getUserSubscription(user.email).subscribe({
      //   next: (data) => {
      //     // "data" es el array que nos devuelve Spring Boot con todo el árbol (Suscripcion -> Foro -> Pelicula)
          
      //     this.filmsForum = data.map((sub, index) => {
      //       return {
      //         title: sub.forum.film.title,      
      //         active: index === 0,         
              
      //         forumTopicId: sub.forum.forumId,
      //         filmId: sub.forum.film.id
      //       };
      //     });

      //     this.cdr.detectChanges()

      //     console.log('Foros cargados desde la base de datos:', this.filmsForum);

      if (!user || !user.email) return; 

      console.log('Usuario detectado:', user.email, 'Pidiendo foros a Spring Boot...');

      this.subscriptionService.getUserSubscriptions(user.email).subscribe({
        next: (data) => {
          console.log('Datos puros del backend:', data);

          try {
            const mappedForums = data.map((sub, index) => {
              return {
                title: sub.forum?.film?.title || 'Foro sin título',      
                active: false, 
                forumTopicId: sub.forum?.discourseTopicId,
                filmId: sub.forum?.film?.id
              };
            });

            this.filmsForum.set(mappedForums);

            if (mappedForums.length > 0) {
              const autoSelectFilmId = this.route.snapshot.queryParamMap.get('filmId');
              
              if (autoSelectFilmId) {
                const indexDestino = mappedForums.findIndex(f => f.filmId === Number(autoSelectFilmId));
                
                if (indexDestino !== -1) {
                  this.selectForum(indexDestino);
                } else {
                  this.selectForum(0);
                }
              } else {

                this.selectForum(0); 
              }
            }
            
            this.cdr.detectChanges();
            console.log('Foros cargados listos para pantalla:', this.filmsForum());
          } catch(e) {
            console.error('Error al mapear el JSON devuelto por Spring:', e);
          }
        },
        error: (err) => {
          console.error('Error al obtener los foros:', err);
        }
      });
    });
  }
} 

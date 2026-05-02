import { Component, Output, EventEmitter, signal, Input, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MessageBubble } from '../message-bubble/message-bubble';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-chat-window',
  standalone: true,
  imports: [FormsModule, MessageBubble],
  templateUrl: './chat-window.html',
  styleUrl: './chat-window.css'
})
export class ChatWindow {
  @Output() return = new EventEmitter<void>();

  ForumTitle = '';
  forumId: number | null = null;
  saveUrl: SafeResourceUrl | null = null;
  activeForum = false;

  constructor(
    private sanitizer: DomSanitizer
  ) {}

  @Input() set selectedForumTitle(filmName: string) {
    this.ForumTitle = filmName;
  }

  @Input() set selectedForumId(value: number | null ){
      this.forumId = value

      if (value !== null) {
          this.activateForum();
      } else {
          this.saveUrl = null; 
          this.activeForum = false;
      }

  }

  activateForum() {

    if (this.activeForum && this.forumId !== null) {
        this.chargeForum(this.forumId);
        return;
    }

    if (localStorage.getItem('foro_sesion_activa') === 'true') {
      this.activeForum = true;
      this.chargeForum(this.forumId!);
      return;
    }

    const ssoUrl = 'https://netlikes.duckdns.org/session/sso';
    const width = 600;
    const height = 600;
    const left = (window.screen.width / 2) - (width / 2);
    const top = (window.screen.height / 2) - (height / 2);

    // Abrimos el SSO en una ventana nueva. 
    // Al ser una ventana propia, el navegador SI DEJA guardar la cookie.
    const popup = window.open(ssoUrl, 'ForoLogin', `width=${width},height=${height},left=${left},top=${top}`);

    const timer = setInterval(() => {
      if (popup?.closed) {
        clearInterval(timer);
        this.activeForum = true; // Mostramos el iframe
        if (this.forumId !== null) {
          this.chargeForum(this.forumId);
        }
      }
      localStorage.setItem('foro_sesion_activa', 'true');
    }, 1000);
  }

  chargeForum(forumId: number) {
      const originalUrl = `https://netlikes.duckdns.org/t/${forumId}`;
      console.log("Angular está intentando meter en el iframe exactamente esta URL:", originalUrl);
      this.saveUrl = this.sanitizer.bypassSecurityTrustResourceUrl(originalUrl);
  }

  goBack() {
    this.return.emit();
  }


  // ngOnChanges(changes: SimpleChanges) {
  //   if (changes['selectedForumId'] && this.forumId) {
  //     this.chargeForum(this.forumId);
  //   }
  // }

  // messages = signal([
  //   { text: '¡Hola a todos! ¿Cuál es vuestra escena favorita?', isMine: false, user: 'User123' },
  //   { text: 'A mí me encanta cuando Katniss se ofrece como tributo en lugar de su hermana', isMine: true, user: 'Yo' },
  //   { text: 'Totalmente de acuerdo', isMine: false, user: 'Cinefilo99' },
  //   { text: 'Pues a mí me gusta la escena de las bayas', isMine: false, user: 'User123' },
  //   { text: 'Siii, gracias a esa se inicia la rebelión', isMine: false, user: 'Cinefilo99' },
  //   { text: 'A mí me gustaron las entrevistas a los tributos', isMine: false, user: 'User987' },
  //   { text: 'Ojalá hubieran añadido todas, así conoceríamos mejor a los participantes', isMine: true, user: 'Yo' }
  // ]);

  // AdjustHeight(textarea: HTMLTextAreaElement) {
  //   textarea.style.height = 'auto';
  //   textarea.style.height = textarea.scrollHeight + 'px';
  // }

  // sendMessage(textarea: HTMLTextAreaElement) {
  //   const text = this.newMessage().trim();
  //   if (text) {
  //     this.messages.update(prev => [...prev, {
  //       text: text,
  //       isMine: true,
  //       user: 'Yo',
  //       hour: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  //     }]);
  //   }
  //   this.newMessage.set('');
  //   textarea.style.height = 'auto';
  //   console.log('Mensaje enviado correctamente');
  // }

}
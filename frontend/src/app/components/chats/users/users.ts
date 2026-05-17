import { Component,Input, Output, EventEmitter, signal, computed, ChangeDetectorRef, inject} from "@angular/core";
import { FormsModule } from '@angular/forms';
import { SearchBarComponent } from "../../search-bar/search-bar";

@Component({
    selector: "app-social-chats-users",
    imports: [FormsModule, SearchBarComponent],
    standalone: true,
    templateUrl: "./users.html",
    styleUrl: "./users.css"
})
export class Users{
   
    @Output() clickedUser = new EventEmitter<{user: string, chatId: number}>(); 

    private cdr = inject(ChangeDetectorRef);

    friends = signal<any[]>([]);
    searchText = signal('');

    private incomingChatId: number | null = null;
    private incomingChatName: string = '';

    @Input() set newChatId(id: number | null) {
        this.incomingChatId = id;
        this.procesarNuevoChat();
    }

    @Input() set newChatName(name: string) {
        this.incomingChatName = name;
        this.procesarNuevoChat();
    }

    private procesarNuevoChat() {
        if (!this.incomingChatName || !this.incomingChatId) return;

        const list = this.friends();
        const exists = list.find(user => user.name === this.incomingChatName);

        list.forEach(p => p.active = false);

        if (!exists) {
            const nuevoAmigo = {
                name: this.incomingChatName,
                chatId: this.incomingChatId, 
                active: true 
            };
            
            this.friends.set([nuevoAmigo, ...list]);
        } else {
            exists.active = true;
            exists.chatId = this.incomingChatId;
            this.friends.set([...list]);
        }
        
        this.cdr.detectChanges();
    }

    filteredUsers = computed(() => {
        const searchLow = (this.searchText() || '').toLowerCase();
        return this.friends().filter(user => 
        (user.name || '').toLowerCase().includes(searchLow)
        );
    });

    handleSearch(text: string) {
        this.searchText.set(text.toLowerCase());
    }


    selectUser(index: number) {
        const currentUser = this.friends();
        currentUser.forEach(p => p.active = false);

        const selected = this.filteredUsers()[index];
        if (selected) {
        selected.active = true;
        this.clickedUser.emit({ 
            user: selected.name,
            chatId: selected.forumTopicId
        });
        console.log('Cambiando al chat de:', selected.name, "con ID: ", selected.chatID);
        }
        
        this.friends.set([...currentUser]);
        this.cdr.detectChanges(); 
    }



}

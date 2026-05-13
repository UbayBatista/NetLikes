import { Component, Output, EventEmitter, signal, computed, ChangeDetectorRef, inject} from "@angular/core";
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
   
    @Output() clickedUser = new EventEmitter<{user: string}>(); 

    private cdr = inject(ChangeDetectorRef);

    friends = signal<any[]>([]);
    searchText = signal('');


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
            //chatId: selected.forumTopicId
        });
        console.log('Cambiando al chat de:', selected.name, "con ID: ", selected.chatID);
        }
        
        this.friends.set([...currentUser]);
        this.cdr.detectChanges(); 
    }



}

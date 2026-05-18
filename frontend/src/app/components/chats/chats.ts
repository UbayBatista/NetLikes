import { ChangeDetectorRef, Component, inject, Input } from "@angular/core";
import { Users } from "./users/users";
import { Menssages } from "./menssages/menssages";
import { ActivatedRoute } from '@angular/router';
import { AuthService } from "../../services/auth.service";
import { HttpClient } from "@angular/common/http";

@Component({
    selector: "app-social-chats",
    standalone: true,
    imports: [Users, Menssages],
    templateUrl: "./chats.html",
    styleUrl: "./chats.css"
})
export class Chats{

    userFriend: string = '';
    selectedChat: boolean = false;
    chatId: number | null = null;
    private authService = inject(AuthService);
    private http = inject(HttpClient);

    constructor(private route: ActivatedRoute, private cdr: ChangeDetectorRef) {}

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            if (params['chatId'] && params['chatWith']) {
                this.chatId = Number(params['chatId']);
                this.userFriend = params['chatWith'];
                this.selectedChat = true;
            }
        });
    }

    seeChat(event: { user: string, chatId: number }) {
        this.userFriend = event.user;
        this.selectedChat = false;
        this.chatId = null;

        if (event.chatId) {
            this.chatId = event.chatId;
            this.selectedChat = true;
            
        } else {
            console.log(`Pidiendo ID a Java para chatear con ${this.userFriend}...`);
            
            this.authService.getCurrentUser().subscribe(user => { 
                if (!user) return;
                
                const myUser = user.userName;
                
                this.http.get<number>(`https://api-db.duckdns.org/users/chat/id?myUser=${myUser}&userFriend=${this.userFriend}`)
                    .subscribe({
                        next: (nuevoChatId) => {
                            console.log(`¡ID recibido! Abriendo el chat ${nuevoChatId}`);
                            this.chatId = nuevoChatId;
                            this.selectedChat = true;
                            this.cdr.detectChanges();
                        },
                        error: (err) => {
                            console.error("No se pudo iniciar el chat.", err);
                        }
                    });
            });
        }

    }

    returnToList() {
        this.selectedChat = false;
    }
}

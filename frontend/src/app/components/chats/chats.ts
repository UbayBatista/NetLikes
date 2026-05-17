import { Component, Input } from "@angular/core";
import { Users } from "./users/users";
import { Menssages } from "./menssages/menssages";
import { ActivatedRoute } from '@angular/router';

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

    constructor(private route: ActivatedRoute) {}

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
        this.selectedChat = true;
        this.chatId = event.chatId;
    }

    returnToList() {
        this.selectedChat = false;
    }
}

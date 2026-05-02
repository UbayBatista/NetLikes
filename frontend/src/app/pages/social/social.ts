import { Component } from "@angular/core";
import { SearchUser } from "../../components/search-user/search-user";
import { Chats } from "../../components/chats/chats";

@Component({
    selector: "app-social",
    standalone: true,
    imports: [SearchUser, Chats],
    templateUrl: "./social.html",
    styleUrl: "./social.css"
})
export class Social{

    selectedMode: string="Buscar Usuario";

    changeMode(newMode: string){
        this.selectedMode = newMode;
    }

}
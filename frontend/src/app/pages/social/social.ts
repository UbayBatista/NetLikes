import { Component } from "@angular/core";
import { SearchUser } from "../../components/search-user/search-user";
import { Chats } from "../../components/chats/chats";
import { ActivatedRoute } from "@angular/router";

@Component({
    selector: "app-social",
    standalone: true,
    imports: [SearchUser, Chats],
    templateUrl: "./social.html",
    styleUrl: "./social.css"
})
export class Social{

    selectedMode: string="Buscar Usuario";

    constructor(private route: ActivatedRoute) {}

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            if (params['mode'] === 'Chats' || params['chatId']) {
                this.selectedMode = 'Chats';
            }
        });
    }

    changeMode(newMode: string){
        this.selectedMode = newMode;
    }


}
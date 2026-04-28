import { Component, OnInit, Input, ChangeDetectorRef,ViewChild, ElementRef } from "@angular/core";
import {ProfileBody} from "../../components/profile-components/profile-components";
import { ProfileHeader } from "../../components/profile-header/profile-header";
import { ActivatedRoute } from '@angular/router'; 
import { Film } from "../../components/film/film";
import { SocialModal } from "../../components/social-modal/social-modal";
import { AuthService } from '../../services/auth.service';
import { Observable } from 'rxjs';
import { User } from '../../models/user.models';
import { AsyncPipe } from '@angular/common';
import { Router } from "@angular/router";

@Component({
    selector:"app-profile-complete",
    standalone: true,
    imports: [ProfileBody, ProfileHeader, Film, SocialModal, AsyncPipe],
    templateUrl: "./profile-body.html",
    styleUrl: "./profile-body.css"
})
export class ProfileComplete implements OnInit{
    currentUser$: Observable<User | null>;
    profileName = '';
    profilePicture = 'https://media.gettyimages.com/id/962792726/es/foto/kiev-ukraine-cristiano-ronaldo-of-real-madrid-poses-with-the-uefa-champions-league-trophy.jpg?s=612x612&w=gi&k=20&c=iGuCfZEUXyVagRfgPF765GB9CHIsyTplWQisj_AUC2U='; 
    textBio = 'Hola soy nuevo por aquí.';
    itsMe = false;
    isEditing = false;
    isSocialModalOpen = false;
    socialType: 'Seguidores' | 'Seguidos' = 'Seguidores';
    socialData: any[] = [];
    
    constructor(private route: ActivatedRoute, private cdr: ChangeDetectorRef, private authService: AuthService, private router: Router) {
        this.currentUser$ = this.authService.getCurrentUser();
    }

    ngOnInit() {
        
        this.route.params.subscribe(params => {
            this.profileName = params['username']; 
            
            if (this.profileName === "my profile") {
                this.itsMe = true;
            } else {
                this.itsMe = false;
            }
        });
    }
    
    showSocial(type: 'Seguidores' | 'Seguidos') {
        this.socialType = type;
        this.isSocialModalOpen = true;

        if (type === 'Seguidores') {
            this.socialData = [
                { name: 'Luis Suárez', avatar: 'https://elasticbeanstalk-us-east-1-911267631614.s3.amazonaws.com/imagenes/jugadores/SUAREZ%20LUIS%20(2).jpg', status: 'Seguir también' },
                { name: 'Benzema', avatar: 'https://revsportz.in/wp-content/uploads/2023/06/Benzema.jpeg', status: 'Pendiente' },
                { name: 'La Roca', avatar: 'https://wallpaperaccess.com/full/1264961.jpg', status: 'Seguir también' },
                { name: 'Cristiano Ronaldo', avatar: 'https://i.pinimg.com/736x/f6/28/85/f628856f2e45836d5850d959a1ed5c75.jpg', status: '' },
                { name: 'Messi', avatar: 'https://cdn2.telediario.mx/uploads/media/2022/12/18/lionel-messi-besa-copa-recibir.jpg', status: 'Seguir también' },
                { name: 'Zinedine Zidane', avatar: 'https://assets.realmadrid.com/is/image/realmadrid/1330186270595?$Mobile$&fit=wrap&wid=312', status: '' },
                { name: 'Kylian Mbappé', avatar: 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/9c/Picture_with_Mbapp%C3%A9_%28cropped%29_%28cropped%29.jpg/250px-Picture_with_Mbapp%C3%A9_%28cropped%29_%28cropped%29.jpg', status: 'Pendiente' },
                { name: 'Andrés Iniesta', avatar: 'https://imageio.forbes.com/specials-images/imageserve/5ecead3f0ca011000726427e/0x0.jpg?format=jpg&crop=1669,1671,x0,y79,safe&height=416&width=416&fit=bounds', status: 'Seguir también' },
                { name: 'Xavi Hernández', avatar: 'https://fcb-abj-pre.s3.amazonaws.com/img/jugadors/949_xavi.jpg', status: '' },
                { name: 'Carles Puyol', avatar: 'https://fcb-abj-pre.s3.amazonaws.com/img/jugadors/704_puyol.jpg', status: 'Seguir también' }
            ];
        } else {
            this.socialData = [
                { name: 'Cristiano Ronaldo', avatar: 'https://i.pinimg.com/736x/f6/28/85/f628856f2e45836d5850d959a1ed5c75.jpg'},
                { name: 'Zinedine Zidane', avatar: 'https://assets.realmadrid.com/is/image/realmadrid/1330186270595?$Mobile$&fit=wrap&wid=312'},
                { name: 'Xavi Hernández', avatar: 'https://fcb-abj-pre.s3.amazonaws.com/img/jugadors/949_xavi.jpg'},
                { name: 'Luka Modric', avatar: 'https://upload.wikimedia.org/wikipedia/commons/1/1b/Ofrenda_de_la_Liga_y_la_Champions-57-L.Mill%C3%A1n_%2852109310843%29_%28Luka_Modri%C4%87%29.jpg' },
                { name: 'Toni Kroos', avatar: 'https://s.hs-data.com/gfx/person/l/3198.jpg?fallback=male' },
                { name: 'Vinícius Jr', avatar: 'https://cdn.britannica.com/45/273345-050-7B263FB8/Vinicius-Junior-Real-Madrid-football-soccer-player-UEFA-league-knockout--play-off-against-Manchester-City-2024-25.jpg' },
                { name: 'Erling Haaland', avatar: 'https://upload.wikimedia.org/wikipedia/commons/7/71/Erling_Haaland_June_2025.jpg' },
                { name: 'Kevin De Bruyne', avatar: 'https://img.a.transfermarkt.technology/portrait/big/88755-1713391485.jpg?lm=1' },
                { name: 'Robert Lewandowski', avatar: 'https://upload.wikimedia.org/wikipedia/commons/2/26/2019147183134_2019-05-27_Fussball_1.FC_Kaiserslautern_vs_FC_Bayern_M%C3%BCnchen_-_Sven_-_1D_X_MK_II_-_0228_-_B70I8527_%28cropped%29.jpg' }
            ];
        }
    }

    @Input() filmsView: any[] = [
        { title: 'Avengers: Endgame', image: 'https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg' },
        { title: 'Avengers: Infinity War', image: 'https://image.tmdb.org/t/p/w500/7WsyChQLEftFiDOVTGkv3hFpyyt.jpg' },
        { title: 'The Avengers', image: 'https://image.tmdb.org/t/p/w500/aMETsaNNcDc6g5ZatQtVbySnSaA.jpg' },
        { title: 'Deadpool 2', image: 'https://image.tmdb.org/t/p/w500/qjiPP4FhTV3UAGa1Dbf2qEqTvu5.jpg' },
        { title: 'Shazam!', image: 'https://image.tmdb.org/t/p/w500/yUOJHa9XmB1H0iYodG9Kb3YCc9T.jpg' },
        { title: 'Spectre (007)', image: 'https://image.tmdb.org/t/p/w500/mSvpKOWbyFtLro9BjfEGqUw5dXE.jpg' },
        { title: 'Fight Club', image: 'https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg' }
    ];

    @Input() filmsLater: any[] = [
        { title: 'Inception', image: 'https://image.tmdb.org/t/p/w500/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg' },
        { title: 'The Dark Knight', image: 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg' },
        { title: 'Venom', image: 'https://image.tmdb.org/t/p/w500/2uNW4WbgBXL25BAbXGLnLqX71Sw.jpg' },
        { title: 'Mad Max: Fury Road', image: 'https://image.tmdb.org/t/p/w500/8tZYtuWezp8JbcsvHYO0O46tFbo.jpg' }
    ];

    @Input() filmsViewOther: any[] = [
        { title: 'The Godfather', image: 'https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg' },
        { title: 'The Shawshank Redemption', image: 'https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg' },
        { title: 'Fight Club', image: 'https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg' },
        { title: 'Forrest Gump', image: 'https://image.tmdb.org/t/p/w500/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg' },
        { title: 'Titanic', image: 'https://image.tmdb.org/t/p/w500/9xjZS2rlVxm8SFx8kPC3aIGCOYQ.jpg' },
        { title: 'Joker', image: 'https://image.tmdb.org/t/p/w500/udDclJoHjfjb8Ekgsd4FDteOkCU.jpg' },
        { title: 'Parasite', image: 'https://image.tmdb.org/t/p/w500/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg' },
        { title: 'The Lord of the Rings', image: 'https://image.tmdb.org/t/p/w500/rCzpDGLbOoPwLjy3OAm5NUPOTrC.jpg' }
    ];

    @Input() filmsLaterOther: any[] = [
        { title: 'Spider-Man: Into the Spider-Verse', image: 'https://image.tmdb.org/t/p/w500/iiZZdoQBEYBv6id8su7ImL0oCbD.jpg' },
        { title: 'Up', image: 'https://image.tmdb.org/t/p/w500/vpbaStTMt8qqXaEgnOR2EE4DNJk.jpg' },
        { title: 'Avengers: Endgame', image: 'https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg' },
        { title: 'Avengers: Infinity War', image: 'https://image.tmdb.org/t/p/w500/7WsyChQLEftFiDOVTGkv3hFpyyt.jpg' },
        { title: 'The Avengers', image: 'https://image.tmdb.org/t/p/w500/aMETsaNNcDc6g5ZatQtVbySnSaA.jpg' },
        { title: 'Deadpool 2', image: 'https://image.tmdb.org/t/p/w500/qjiPP4FhTV3UAGa1Dbf2qEqTvu5.jpg' },
        { title: 'Shazam!', image: 'https://image.tmdb.org/t/p/w500/yUOJHa9XmB1H0iYodG9Kb3YCc9T.jpg' }
    ];

    canScrollLeft = false;
    canScrollRight = true;


    @ViewChild('scrollContainer') scrollContainer!: ElementRef<HTMLDivElement>;

    scrollLeft() {
        const el = this.scrollContainer.nativeElement;

        el.scrollBy({
            left: -300,
            behavior: 'smooth'
        });

    setTimeout(() => this.updateScrollButtons(), 300);
    }

    scrollRight() {
        const el = this.scrollContainer.nativeElement;

        el.scrollBy({
        left: 300,
        behavior: 'smooth'
        });

        setTimeout(() => this.updateScrollButtons(), 300);
    }

    updateScrollButtons() {
        const el = this.scrollContainer.nativeElement;

        this.canScrollLeft = el.scrollLeft > 0;
        this.canScrollRight = el.scrollLeft + el.clientWidth < el.scrollWidth;

        this.cdr.detectChanges();
    }

    ngAfterViewInit() {
        const el = this.scrollContainer.nativeElement;

        this.updateScrollButtons();

        el.addEventListener('scroll', () => this.updateScrollButtons(), { passive: true });
        el.addEventListener('wheel', () => this.updateScrollButtons(), { passive: true });
        el.addEventListener('touchmove', () => this.updateScrollButtons(), { passive: true });
    }

    Edit(){
        this.isEditing = !this.isEditing;
    }

    logout(){
        this.authService.logout();
        this.router.navigate(['/']);
    }

}
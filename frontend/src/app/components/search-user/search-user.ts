import { ChangeDetectorRef, Component, Input, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { SearchBarComponent } from "../search-bar/search-bar";
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.models'; 
import { AuthService } from '../../services/auth.service';

@Component({
    selector: "app-social-search-user",
    standalone: true,
    imports: [ SearchBarComponent ],
    templateUrl: "./search-user.html",
    styleUrl: "./search-user.css"
})
export class SearchUser implements OnInit {
    @Input() usuario: string = "Messi"; 
    
    initialUsers: User[] = []; 
    searchResults: User[] = [];
    isSearching: boolean = false;

    currentUserEmail: string = '';
    
    private searchSubject = new Subject<string>();

    constructor(
        private router: Router, 
        private userService: UserService,
        private autService: AuthService,
        private cdr: ChangeDetectorRef
    ) {}


    get activeList() {
        return this.isSearching ? this.searchResults : this.initialUsers;
    }

    ngOnInit() {
        this.autService.getCurrentUser().subscribe({
            next: (user) => {
                this.currentUserEmail = user ? user.email : ''; 

                this.loadUsers();
            },
            error: (err) => console.error('Error obteniendo el usuario actual:', err)
        });

        this.searchSubject.pipe(
            debounceTime(300), 
            distinctUntilChanged(),
            switchMap((query) => {

                if (!query || query.trim() === '') {
                    this.isSearching = false; 
                    this.searchResults = [];
                    return of([]); 
                }
                
                this.isSearching = true;
                
                return this.userService.searchBy(query).pipe(
                    catchError(err => {
                        console.error('Error buscando usuarios', err);
                        return of([]); 
                    })
                );
            })
        ).subscribe(results => {
            if (this.isSearching) {
                this.searchResults = results;
                this.cdr.detectChanges();
            }
        });
    }

    loadUsers() {
        this.userService.getUsers(this.currentUserEmail).subscribe({
            next: (data) => {
                this.initialUsers = data; 
                this.cdr.detectChanges();
            },
            error: (error) => {
                console.error('Error fetching users:', error);
            }
        });
    }

    goToProfile(userName: string) {
        this.router.navigate(['/profile', userName]);
    }

    handleSearch(text: string) {
        this.searchSubject.next(text);
    }
}
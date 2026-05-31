import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private readonly dbUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  getChatId(myUser: string, userFriend: string): Observable<number> {
    return this.http.get<number>(
      `${this.dbUrl}/users/chat/id?myUser=${myUser}&userFriend=${userFriend}`
    );
  }
}

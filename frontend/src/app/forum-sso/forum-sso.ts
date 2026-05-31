import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-foro-sso',
  templateUrl: './forum-sso.html''
})
export class ForumSsoComponent implements OnInit {

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const sso = params['sso'];
      const sig = params['sig'];
      
      const userString = localStorage.getItem('user');
      if (sso && sig && userString) {
        const user = JSON.parse(userString);
        const userAvatar = user.userPicture ? user.userPicture : user.userName;

        const payload = {
          sso: sso,
          sig: sig,
          email: user.email,
          username: user.userName,
          name: user.userName,
          avatar_url: "&avatar_url=https://api.dicebear.com/9.x/fun-emoji/png?seed=" + userAvatar
        };

        this.http.post('https://api-db.duckdns.org/auth/sso/process', payload)
          .subscribe({
            next: (response: any) => {
              window.location.href = response.redirectUrl;
            }
          });
      }
    });
  }
}
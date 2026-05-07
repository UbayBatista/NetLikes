import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-foro-sso',
  template: '<p style="text-align:center; padding: 20px;">Iniciando sesión en el foro de forma segura...</p>'
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
        
        const payload = {
          sso: sso,
          sig: sig,
          email: user.email,
          username: user.username || user.email.split('@')[0]
        };

        this.http.post('https://api-db.duckdns.org/auth/sso/process', payload)
          .subscribe({
            next: (response: any) => {
              window.location.href = response.redirectUrl;
            },
            error: (err) => console.error("Error en el SSO:", err)
          });
      }
    });
  }
}
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-foro-sso',
  template: '<p style="text-align:center; padding: 20px;">Iniciando sesión en el foro de forma segura...</p>'
})
export class ForoSsoComponent implements OnInit {

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit() {
    // 1. Capturamos los parámetros que nos manda Discourse por la URL
    this.route.queryParams.subscribe(params => {
      const sso = params['sso'];
      const sig = params['sig'];
      
      // 2. Sacamos a nuestro usuario del localStorage
      const userString = localStorage.getItem('user');
      if (sso && sig && userString) {
        const user = JSON.parse(userString);
        
        // 3. Le enviamos TODO a Spring Boot
        const payload = {
          sso: sso,
          sig: sig,
          email: user.email,
          username: user.username || user.email.split('@')[0]
        };

        // Cambia esta URL por la de tu backend real
        this.http.post('https://api-db.duckdns.org/users/sso/process', payload)
          .subscribe({
            next: (response: any) => {
              // 4. Spring Boot nos da la URL final, ¡redirigimos el iframe hacia allá!
              window.location.href = response.redirectUrl;
            },
            error: (err) => console.error("Error en el SSO:", err)
          });
      }
    });
  }
}
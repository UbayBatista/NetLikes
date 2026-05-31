import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch(() => {
    document.body.innerHTML = `
      <h2>Ha ocurrido un error al iniciar la aplicación.</h2>
      <p>Por favor, inténtalo más tarde.</p>
    `;
  });


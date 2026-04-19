import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Auth } from '../auth/services/auth';

@Component({
  selector: 'app-intranet-redirect',
  standalone: true,
  template: `<div style="display:flex;align-items:center;justify-content:center;height:100vh;font-family:'Montserrat',sans-serif;color:#6b7280;">
    <span>🔄 Redirigiendo...</span>
  </div>`,
})
export class IntranetRedirect implements OnInit {
  constructor(private auth: Auth, private router: Router) {}

  ngOnInit() {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    if (this.auth.isAdmin()) {
      this.router.navigate(['/intranet/admin']);
    } else {
      this.router.navigate(['/intranet/agricultor']);
    }
  }
}

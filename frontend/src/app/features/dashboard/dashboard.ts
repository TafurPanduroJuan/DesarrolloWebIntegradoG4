import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Auth, Usuario } from '../../features/auth/services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  usuario: Usuario | null = null;

  constructor(private auth: Auth, private router: Router) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuarioActual();
    if (!this.usuario) {
      this.router.navigate(['/login']);
    }
  }

  cerrarSesion() {
    this.auth.logout();
  }
}
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Auth, UsuarioSesion } from '../../../features/auth/services/auth';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
})
export class Navbar {
  get usuarioActual(): UsuarioSesion | null {
    return this.auth.getUsuarioActual();
  }

  constructor(private auth: Auth) {}

  cerrarSesion() {
    this.auth.logout();
  }
}
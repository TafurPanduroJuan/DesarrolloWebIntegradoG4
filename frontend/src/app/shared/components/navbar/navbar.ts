import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Auth, Usuario } from '../../../features/auth/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
})
export class Navbar {
  get usuarioActual(): Usuario | null {
    return this.auth.getUsuarioActual();
  }

  constructor(private auth: Auth) {}

  cerrarSesion() {
    this.auth.logout();
  }
}
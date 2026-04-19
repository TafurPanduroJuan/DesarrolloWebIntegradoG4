import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

export type RolUsuario = 'agricultor' | 'admin';

export interface Usuario {
  id: string;
  nombre: string;
  email: string;
  password: string;
  rol: RolUsuario;
}

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly USERS_KEY = 'agrolink_usuarios';
  private readonly SESSION_KEY = 'agrolink_sesion';

  constructor(private router: Router) {
    this.seedAdmin();
  }

  /** Crea el usuario admin por defecto si no existe */
  private seedAdmin(): void {
    const usuarios = this.getUsuarios();
    const adminExiste = usuarios.find((u) => u.email === 'admin@agrolink.pe');
    if (!adminExiste) {
      const admin: Usuario = {
        id: 'admin-001',
        nombre: 'Administrador',
        email: 'admin@agrolink.pe',
        password: 'admin123',
        rol: 'admin',
      };
      usuarios.push(admin);
      localStorage.setItem(this.USERS_KEY, JSON.stringify(usuarios));
    }
  }

  private getUsuarios(): Usuario[] {
    return JSON.parse(localStorage.getItem(this.USERS_KEY) || '[]');
  }

  register(nombre: string, email: string, password: string): { ok: boolean; mensaje: string } {
    const usuarios = this.getUsuarios();
    const existe = usuarios.find((u) => u.email === email);
    if (existe) return { ok: false, mensaje: 'Este correo ya está registrado.' };

    const nuevo: Usuario = {
      id: crypto.randomUUID(),
      nombre,
      email,
      password,
      rol: 'agricultor',
    };
    usuarios.push(nuevo);
    localStorage.setItem(this.USERS_KEY, JSON.stringify(usuarios));
    return { ok: true, mensaje: 'Cuenta creada exitosamente.' };
  }

  login(email: string, password: string): { ok: boolean; mensaje: string } {
    const usuarios = this.getUsuarios();
    const usuario = usuarios.find((u) => u.email === email && u.password === password);
    if (!usuario) return { ok: false, mensaje: 'Correo o contraseña incorrectos.' };

    localStorage.setItem(this.SESSION_KEY, JSON.stringify(usuario));
    return { ok: true, mensaje: 'Bienvenido, ' + usuario.nombre };
  }

  logout(): void {
    localStorage.removeItem(this.SESSION_KEY);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.SESSION_KEY);
  }

  isAdmin(): boolean {
    const u = this.getUsuarioActual();
    return u?.rol === 'admin';
  }

  getUsuarioActual(): Usuario | null {
    return JSON.parse(localStorage.getItem(this.SESSION_KEY) || 'null');
  }
}
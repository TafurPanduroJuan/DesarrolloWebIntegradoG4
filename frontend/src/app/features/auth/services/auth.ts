import { environment } from '../../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export type RolUsuario = 'AGRICULTOR' | 'COMPRADOR' | 'ADMINISTRADOR';

export interface UsuarioSesion {
  id: number;
  nombre: string;
  email: string;
  rol: RolUsuario;
}

export interface JwtResponse {
  token: string;
  tipo: string;
  id: number;
  nombre: string;
  email: string;
  rol: RolUsuario;
}

export interface RegisterRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  rol: RolUsuario;
  region?: string;
  productoresPrincipales?: string;
  ruc?: string;
  direccionComercial?: string;
  tipoComprador?: string;
}

export interface ProductoAgrolink {
  id: string;
  agricultorId: string;
  agricultorNombre: string;
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: string;
  unidad: string;
  stock: number;
  estado: 'pendiente' | 'aprobado' | 'rechazado';
  fecha: string;
}

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly API_URL = `/auth`;
  private readonly TOKEN_KEY = 'agrolink_token';
  private readonly SESSION_KEY = 'agrolink_sesion';
  private readonly PRODUCTOS_KEY = 'agrolink_productos';

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.API_URL}/login`, { email, password }).pipe(
      tap((res) => this.guardarSesion(res))
    );
  }

  register(data: RegisterRequest): Observable<any> {
    return this.http.post(`${this.API_URL}/register`, data);
  }

  private guardarSesion(res: JwtResponse): void {
    localStorage.setItem(this.TOKEN_KEY, res.token);
    const sesion: UsuarioSesion = {
      id: res.id,
      nombre: res.nombre,
      email: res.email,
      rol: res.rol,
    };
    localStorage.setItem(this.SESSION_KEY, JSON.stringify(sesion));
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.SESSION_KEY);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getUsuarioActual(): UsuarioSesion | null {
    return JSON.parse(localStorage.getItem(this.SESSION_KEY) || 'null');
  }

  isAdmin(): boolean {
    return this.getUsuarioActual()?.rol === 'ADMINISTRADOR';
  }

  hasRole(rol: RolUsuario): boolean {
    return this.getUsuarioActual()?.rol === rol;
  }

  // Productos siguen en mock por ahora (no hay backend para esto aÃºn)
  getProductos(): ProductoAgrolink[] {
    return JSON.parse(localStorage.getItem(this.PRODUCTOS_KEY) || '[]');
  }

  getProductosPorAgricultor(agricultorId: string): ProductoAgrolink[] {
    return this.getProductos().filter(p => p.agricultorId === agricultorId);
  }

  subirProducto(producto: Omit<ProductoAgrolink, 'id' | 'estado' | 'fecha' | 'agricultorId' | 'agricultorNombre'>): void {
    const usuario = this.getUsuarioActual();
    if (!usuario) return;
    const productos = this.getProductos();
    productos.push({
      ...producto,
      id: crypto.randomUUID(),
      agricultorId: String(usuario.id),
      agricultorNombre: usuario.nombre,
      estado: 'pendiente',
      fecha: new Date().toISOString().split('T')[0]
    });
    localStorage.setItem(this.PRODUCTOS_KEY, JSON.stringify(productos));
  }

  actualizarEstadoProducto(id: string, nuevoEstado: 'aprobado' | 'rechazado'): void {
    const productos = this.getProductos();
    const p = productos.find(x => x.id === id);
    if (p) {
      p.estado = nuevoEstado;
      localStorage.setItem(this.PRODUCTOS_KEY, JSON.stringify(productos));
    }
  }
}
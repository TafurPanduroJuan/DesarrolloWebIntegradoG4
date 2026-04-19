import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

export type RolUsuario = 'comprador' | 'agricultor' | 'admin';

export interface Usuario {
  id: string;
  nombre: string;
  email: string;
  password: string;
  rol: RolUsuario;
}

export interface SolicitudAgricultor {
  id: string;
  nombre: string;
  correo: string;
  telefono: string;
  mensaje: string;
  estado: 'pendiente' | 'aprobado' | 'rechazado';
  fecha: string;
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
  private readonly USERS_KEY = 'agrolink_usuarios';
  private readonly SESSION_KEY = 'agrolink_sesion';
  private readonly SOLICITUDES_KEY = 'agrolink_solicitudes_agr';
  private readonly PRODUCTOS_KEY = 'agrolink_productos';

  constructor(private router: Router) {
    this.seedAdmin();
  }

  // ==========================================
  // USUARIOS Y SESIÓN
  // ==========================================

  private seedAdmin(): void {
    const usuarios = this.getUsuarios();
    if (!usuarios.find((u) => u.email === 'admin@agrolink.pe')) {
      usuarios.push({
        id: 'admin-001',
        nombre: 'Administrador',
        email: 'admin@agrolink.pe',
        password: 'admin123',
        rol: 'admin',
      });
      localStorage.setItem(this.USERS_KEY, JSON.stringify(usuarios));
    }
  }

  getUsuarios(): Usuario[] {
    return JSON.parse(localStorage.getItem(this.USERS_KEY) || '[]');
  }

  register(nombre: string, email: string, password: string): { ok: boolean; mensaje: string } {
    const usuarios = this.getUsuarios();
    if (usuarios.find((u) => u.email === email)) {
      return { ok: false, mensaje: 'Este correo ya está registrado.' };
    }

    const nuevo: Usuario = {
      id: crypto.randomUUID(),
      nombre,
      email,
      password,
      rol: 'comprador', // Por defecto es comprador
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

  // ==========================================
  // SOLICITUDES DE AGRICULTORES (Trabaja con nosotros)
  // ==========================================

  getSolicitudes(): SolicitudAgricultor[] {
    return JSON.parse(localStorage.getItem(this.SOLICITUDES_KEY) || '[]');
  }

  enviarSolicitud(solicitud: Omit<SolicitudAgricultor, 'id' | 'estado' | 'fecha'>): void {
    const solicitudes = this.getSolicitudes();
    solicitudes.push({
      ...solicitud,
      id: crypto.randomUUID(),
      estado: 'pendiente',
      fecha: new Date().toISOString().split('T')[0]
    });
    localStorage.setItem(this.SOLICITUDES_KEY, JSON.stringify(solicitudes));
  }

  actualizarEstadoSolicitud(id: string, nuevoEstado: 'aprobado' | 'rechazado'): void {
    const solicitudes = this.getSolicitudes();
    const index = solicitudes.findIndex(s => s.id === id);
    if (index !== -1) {
      solicitudes[index].estado = nuevoEstado;
      localStorage.setItem(this.SOLICITUDES_KEY, JSON.stringify(solicitudes));

      // Si se aprueba, convertimos al usuario en agricultor (si ya estaba registrado con ese correo)
      if (nuevoEstado === 'aprobado') {
        const correo = solicitudes[index].correo;
        const usuarios = this.getUsuarios();
        const userIndex = usuarios.findIndex(u => u.email === correo);
        if (userIndex !== -1) {
          usuarios[userIndex].rol = 'agricultor';
          localStorage.setItem(this.USERS_KEY, JSON.stringify(usuarios));
        } else {
          // Si no existía, le creamos una cuenta automáticamente
          usuarios.push({
            id: crypto.randomUUID(),
            nombre: solicitudes[index].nombre,
            email: correo,
            password: 'agrolink' + solicitudes[index].telefono.slice(-4), // Password por defecto
            rol: 'agricultor'
          });
          localStorage.setItem(this.USERS_KEY, JSON.stringify(usuarios));
        }
      }
    }
  }

  // ==========================================
  // PRODUCTOS (Intranet)
  // ==========================================

  getProductos(): ProductoAgrolink[] {
    return JSON.parse(localStorage.getItem(this.PRODUCTOS_KEY) || '[]');
  }

  subirProducto(producto: Omit<ProductoAgrolink, 'id' | 'estado' | 'fecha' | 'agricultorId' | 'agricultorNombre'>): void {
    const usuario = this.getUsuarioActual();
    if (!usuario || usuario.rol !== 'agricultor') return;

    const productos = this.getProductos();
    productos.push({
      ...producto,
      id: crypto.randomUUID(),
      agricultorId: usuario.id,
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

  getProductosPorAgricultor(agricultorId: string): ProductoAgrolink[] {
    return this.getProductos().filter(p => p.agricultorId === agricultorId);
  }
}
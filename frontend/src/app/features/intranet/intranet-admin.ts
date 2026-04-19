import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Auth, Usuario, SolicitudAgricultor, ProductoAgrolink } from '../auth/services/auth';

@Component({
  selector: 'app-intranet-admin',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './intranet-admin.html',
  styleUrl: './intranet-admin.css',
})
export class IntranetAdmin implements OnInit {
  usuario: Usuario | null = null;
  seccionActiva = signal<string>('resumen');

  usuarios: Usuario[] = [];
  solicitudes: SolicitudAgricultor[] = [];
  productos: ProductoAgrolink[] = [];

  constructor(private auth: Auth, private router: Router) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuarioActual();
    if (!this.usuario || this.usuario.rol !== 'admin') {
      this.router.navigate(['/intranet']);
      return;
    }
    this.cargarDatos();
  }

  cargarDatos() {
    this.usuarios = this.auth.getUsuarios();
    this.solicitudes = this.auth.getSolicitudes();
    this.productos = this.auth.getProductos();
  }

  irA(seccion: string) { this.seccionActiva.set(seccion); }
  cerrarSesion() { this.auth.logout(); }

  // --- SOLICITUDES AGRICULTORES ---
  get solicitudesPendientes() { return this.solicitudes.filter(s => s.estado === 'pendiente'); }
  
  aprobarSolicitud(id: string) {
    this.auth.actualizarEstadoSolicitud(id, 'aprobado');
    this.cargarDatos();
  }
  
  rechazarSolicitud(id: string) {
    this.auth.actualizarEstadoSolicitud(id, 'rechazado');
    this.cargarDatos();
  }

  // --- PRODUCTOS ---
  get productosPendientes() { return this.productos.filter(p => p.estado === 'pendiente'); }

  aprobarProducto(id: string) {
    this.auth.actualizarEstadoProducto(id, 'aprobado');
    this.cargarDatos();
  }

  rechazarProducto(id: string) {
    this.auth.actualizarEstadoProducto(id, 'rechazado');
    this.cargarDatos();
  }

  // --- KPIs ---
  get totalAgricultores() { return this.usuarios.filter(u => u.rol === 'agricultor').length; }
  get totalCompradores() { return this.usuarios.filter(u => u.rol === 'comprador').length; }
  get ingresosTotales() { return 'S/ 0 (Simulado)'; } // Ventas futuras

  estadoBadgeClass(estado: string): string {
    const map: Record<string, string> = {
      'aprobado': 'badge-entregado',
      'rechazado': 'badge-cancelado',
      'pendiente': 'badge-pendiente',
    };
    return map[estado] || 'badge-default';
  }
}

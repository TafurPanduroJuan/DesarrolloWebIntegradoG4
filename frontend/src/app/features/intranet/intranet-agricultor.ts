import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth, Usuario, ProductoAgrolink } from '../auth/services/auth';

@Component({
  selector: 'app-intranet-agricultor',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './intranet-agricultor.html',
  styleUrl: './intranet-agricultor.css',
})
export class IntranetAgricultor implements OnInit {
  usuario: Usuario | null = null;
  seccionActiva = signal<string>('resumen');

  misProductos: ProductoAgrolink[] = [];

  // Formulario nuevo producto
  formProducto = {
    nombre: '',
    descripcion: '',
    precio: 0,
    categoria: '',
    unidad: 'kg',
    stock: 0
  };

  mensajeExito = '';

  constructor(private auth: Auth, private router: Router) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuarioActual();
    // Doble validación, por si entra directo a la URL
    if (!this.usuario || this.usuario.rol !== 'agricultor') {
      this.router.navigate(['/']);
      return;
    }
    this.cargarDatos();
  }

  cargarDatos() {
    if (this.usuario) {
      this.misProductos = this.auth.getProductosPorAgricultor(this.usuario.id);
    }
  }

  irA(seccion: string) { this.seccionActiva.set(seccion); }
  cerrarSesion() { this.auth.logout(); }

  subirProducto() {
    this.auth.subirProducto({ ...this.formProducto });
    
    // Resetear form
    this.formProducto = {
      nombre: '', descripcion: '', precio: 0, categoria: '', unidad: 'kg', stock: 0
    };
    
    this.mensajeExito = 'Producto enviado a revisión exitosamente.';
    setTimeout(() => this.mensajeExito = '', 5000);
    
    this.cargarDatos();
    this.irA('productos'); // Cambiar a la vista de la tabla
  }

  // --- KPIs ---
  get totalProductos() { return this.misProductos.length; }
  get productosAprobados() { return this.misProductos.filter(p => p.estado === 'aprobado').length; }
  get productosPendientes() { return this.misProductos.filter(p => p.estado === 'pendiente').length; }
  get productosRechazados() { return this.misProductos.filter(p => p.estado === 'rechazado').length; }

  estadoBadgeClass(estado: string): string {
    const map: Record<string, string> = {
      'aprobado': 'badge-entregado',
      'rechazado': 'badge-cancelado',
      'pendiente': 'badge-pendiente',
    };
    return map[estado] || 'badge-default';
  }
}

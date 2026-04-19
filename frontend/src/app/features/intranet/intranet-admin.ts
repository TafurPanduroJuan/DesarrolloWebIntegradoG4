import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Auth, Usuario } from '../auth/services/auth';

interface AgricultorRegistrado {
  id: string;
  nombre: string;
  email: string;
  region: string;
  productos: string;
  estado: 'Activo' | 'Inactivo' | 'Pendiente';
  fechaRegistro: string;
}

interface PedidoAdmin {
  id: string;
  agricultor: string;
  cliente: string;
  producto: string;
  monto: string;
  estado: 'Pendiente' | 'En camino' | 'Entregado' | 'Cancelado';
  fecha: string;
}

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

  agricultores: AgricultorRegistrado[] = [
    { id: 'AGR-001', nombre: 'Carlos Mamani', email: 'carlos@mail.com', region: 'Cusco', productos: 'Papas, Maíz', estado: 'Activo', fechaRegistro: '2025-01-10' },
    { id: 'AGR-002', nombre: 'Rosa Quispe', email: 'rosa@mail.com', region: 'Puno', productos: 'Quinua, Habas', estado: 'Activo', fechaRegistro: '2025-01-22' },
    { id: 'AGR-003', nombre: 'Juan Flores', email: 'juan@mail.com', region: 'Arequipa', productos: 'Cebollas, Ajos', estado: 'Pendiente', fechaRegistro: '2025-02-05' },
    { id: 'AGR-004', nombre: 'María Condori', email: 'maria@mail.com', region: 'Ayacucho', productos: 'Tomates, Pimientos', estado: 'Activo', fechaRegistro: '2025-02-14' },
    { id: 'AGR-005', nombre: 'Pedro Huanca', email: 'pedro@mail.com', region: 'Junín', productos: 'Papas Nativas', estado: 'Inactivo', fechaRegistro: '2025-03-01' },
  ];

  pedidos: PedidoAdmin[] = [
    { id: 'P-2025-001', agricultor: 'Carlos Mamani', cliente: 'Supermercado Metro', producto: 'Papas', monto: 'S/ 3,800', estado: 'En camino', fecha: '2025-04-18' },
    { id: 'P-2025-002', agricultor: 'Rosa Quispe', cliente: 'Restaurante La Mar', producto: 'Quinua', monto: 'S/ 1,200', estado: 'Pendiente', fecha: '2025-04-17' },
    { id: 'P-2025-003', agricultor: 'María Condori', cliente: 'Plaza Vea', producto: 'Tomates', monto: 'S/ 2,500', estado: 'Entregado', fecha: '2025-04-10' },
    { id: 'P-2025-004', agricultor: 'Juan Flores', cliente: 'Colegio San Felipe', producto: 'Cebollas', monto: 'S/ 640', estado: 'Cancelado', fecha: '2025-04-08' },
    { id: 'P-2025-005', agricultor: 'Carlos Mamani', cliente: 'Hotel Sheraton', producto: 'Maíz', monto: 'S/ 1,900', estado: 'Entregado', fecha: '2025-04-05' },
  ];

  constructor(private auth: Auth, private router: Router) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuarioActual();
    if (!this.usuario || this.usuario.rol !== 'admin') {
      this.router.navigate(['/intranet']);
    }
  }

  irA(seccion: string) { this.seccionActiva.set(seccion); }

  cerrarSesion() { this.auth.logout(); }

  get totalAgricultores() { return this.agricultores.length; }
  get agricultoresActivos() { return this.agricultores.filter(a => a.estado === 'Activo').length; }
  get pedidosPendientes() { return this.pedidos.filter(p => p.estado === 'Pendiente').length; }
  get ingresosTotales() { return 'S/ 10,040'; }

  estadoBadgeClass(estado: string): string {
    const map: Record<string, string> = {
      'Activo': 'badge-entregado',
      'Inactivo': 'badge-cancelado',
      'Pendiente': 'badge-pendiente',
      'En camino': 'badge-camino',
      'Entregado': 'badge-entregado',
      'Cancelado': 'badge-cancelado',
    };
    return map[estado] || 'badge-default';
  }
}

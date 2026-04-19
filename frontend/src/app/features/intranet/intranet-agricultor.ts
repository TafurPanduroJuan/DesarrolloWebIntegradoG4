import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Auth, Usuario } from '../auth/services/auth';

interface Cosecha {
  id: string;
  producto: string;
  cantidad: string;
  estado: 'En crecimiento' | 'Lista para cosechar' | 'Cosechada';
  fecha: string;
  hectareas: number;
  icono: string;
}

interface Pedido {
  id: string;
  cliente: string;
  producto: string;
  cantidad: string;
  monto: string;
  estado: 'Pendiente' | 'En camino' | 'Entregado' | 'Cancelado';
  fecha: string;
}

@Component({
  selector: 'app-intranet-agricultor',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './intranet-agricultor.html',
  styleUrl: './intranet-agricultor.css',
})
export class IntranetAgricultor implements OnInit {
  usuario: Usuario | null = null;
  seccionActiva = signal<string>('resumen');

  cosechas: Cosecha[] = [
    { id: 'C001', producto: 'Tomates Cherry', cantidad: '1,200 kg', estado: 'Lista para cosechar', fecha: '2025-04-20', hectareas: 2.5, icono: '🍅' },
    { id: 'C002', producto: 'Lechugas Orgánicas', cantidad: '450 kg', estado: 'En crecimiento', fecha: '2025-05-10', hectareas: 1.2, icono: '🥬' },
    { id: 'C003', producto: 'Papas Nativas', cantidad: '3,000 kg', estado: 'Cosechada', fecha: '2025-03-15', hectareas: 4.0, icono: '🥔' },
    { id: 'C004', producto: 'Maíz Morado', cantidad: '800 kg', estado: 'En crecimiento', fecha: '2025-06-01', hectareas: 1.8, icono: '🌽' },
  ];

  pedidos: Pedido[] = [
    { id: 'P-2025-001', cliente: 'Supermercado Metro', producto: 'Tomates Cherry', cantidad: '500 kg', monto: 'S/ 2,500', estado: 'En camino', fecha: '2025-04-18' },
    { id: 'P-2025-002', cliente: 'Restaurante La Mar', producto: 'Lechugas Orgánicas', cantidad: '80 kg', monto: 'S/ 320', estado: 'Pendiente', fecha: '2025-04-17' },
    { id: 'P-2025-003', cliente: 'Plaza Vea', producto: 'Papas Nativas', cantidad: '1,000 kg', monto: 'S/ 3,800', estado: 'Entregado', fecha: '2025-03-20' },
    { id: 'P-2025-004', cliente: 'Colegio San Felipe', producto: 'Maíz Morado', cantidad: '100 kg', monto: 'S/ 450', estado: 'Cancelado', fecha: '2025-04-10' },
  ];

  constructor(private auth: Auth, private router: Router) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuarioActual();
    if (!this.usuario) this.router.navigate(['/login']);
  }

  irA(seccion: string) {
    this.seccionActiva.set(seccion);
  }

  cerrarSesion() {
    this.auth.logout();
  }

  get totalCosechas() { return this.cosechas.length; }
  get cosechasListas() { return this.cosechas.filter(c => c.estado === 'Lista para cosechar').length; }
  get pedidosPendientes() { return this.pedidos.filter(p => p.estado === 'Pendiente').length; }
  get ingresosMes() { return 'S/ 6,270'; }

  estadoBadgeClass(estado: string): string {
    const map: Record<string, string> = {
      'En crecimiento': 'badge-creciendo',
      'Lista para cosechar': 'badge-lista',
      'Cosechada': 'badge-cosechada',
      'Pendiente': 'badge-pendiente',
      'En camino': 'badge-camino',
      'Entregado': 'badge-entregado',
      'Cancelado': 'badge-cancelado',
    };
    return map[estado] || 'badge-default';
  }
}

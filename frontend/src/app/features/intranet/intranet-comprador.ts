import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth, UsuarioSesion } from '../auth/services/auth';
import { PedidoService, PedidoResponse, EstadoPedido } from './pedido.service';

@Component({
  selector: 'app-intranet-comprador',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './intranet-comprador.html',
  styleUrl: './intranet-comprador.css',
})
export class IntranetComprador implements OnInit {
  usuario: UsuarioSesion | null = null;
  pedidos: PedidoResponse[] = [];
  pedidoSeleccionado: PedidoResponse | null = null;
  cargando = false;
  error = '';
  filtroEstado = 'TODOS';

  constructor(
    private auth: Auth,
    private pedidoService: PedidoService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuario = this.auth.getUsuarioActual();
    if (!this.usuario) {
      this.router.navigate(['/login']);
      return;
    }
    if (this.usuario.rol !== 'COMPRADOR') {
      this.router.navigate(['/intranet']);
      return;
    }
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    if (!this.usuario) return;
    this.cargando = true;
    this.error = '';

    this.pedidoService.listarPorComprador(this.usuario.id).subscribe({
      next: (data) => {
        // Ordenar por fechaPedido descendente (más recientes primero)
        this.pedidos = data.sort((a, b) => new Date(b.fechaPedido).getTime() - new Date(a.fechaPedido).getTime());
        this.cargando = false;
        
        // Si hay un detalle abierto, actualizar sus datos también
        if (this.pedidoSeleccionado) {
          const actualizado = this.pedidos.find(p => p.id === this.pedidoSeleccionado!.id);
          if (actualizado) {
            this.pedidoSeleccionado = actualizado;
          }
        }
      },
      error: () => {
        this.error = 'No se pudieron cargar los pedidos. Verifica la conexión con el servidor.';
        this.cargando = false;
      }
    });
  }

  seleccionarPedido(pedido: PedidoResponse): void {
    this.pedidoSeleccionado = pedido;
  }

  cerrarDetalle(): void {
    this.pedidoSeleccionado = null;
  }

  cancelarPedido(pedidoId: number): void {
    if (!confirm('¿Estás seguro de que deseas cancelar este pedido? El stock solicitado se devolverá al catálogo.')) {
      return;
    }
    this.cargando = true;
    this.pedidoService.cambiarEstado(pedidoId, 'CANCELADO', 'Pedido cancelado por el comprador.').subscribe({
      next: () => {
        this.cargarPedidos();
      },
      error: (err) => {
        this.cargando = false;
        alert(err.error?.error || 'No se pudo cancelar el pedido.');
      }
    });
  }

  pedidosFiltrados(): PedidoResponse[] {
    if (this.filtroEstado === 'TODOS') {
      return this.pedidos;
    }
    return this.pedidos.filter(p => p.estado === this.filtroEstado);
  }

  setFiltro(estado: string): void {
    this.filtroEstado = estado;
  }

  getEstadoLabel(estado: any): string {
    const labels: Record<string, string> = {
      PENDIENTE: 'Pendiente',
      CONFIRMADO: 'Confirmado',
      PREPARADO: 'Preparado',
      DESPACHADO: 'Despachado',
      ENTREGADO: 'Entregado',
      CANCELADO: 'Cancelado',
      RECHAZADO: 'Rechazado'
    };
    return labels[estado] || estado;
  }

  getEstadoClase(estado: any): string {
    return 'badge-estado-' + String(estado).toLowerCase();
  }

  cerrarSesion(): void {
    this.auth.logout();
  }

  // Métodos de estadísticas para el dashboard superior
  getTotalPedidos(): number {
    return this.pedidos.length;
  }

  getPedidosPendientes(): number {
    return this.pedidos.filter(p => p.estado === 'PENDIENTE').length;
  }

  getPedidosConfirmados(): number {
    return this.pedidos.filter(p => p.estado === 'CONFIRMADO' || p.estado === 'PREPARADO' || p.estado === 'DESPACHADO').length;
  }

  getPedidosEntregados(): number {
    return this.pedidos.filter(p => p.estado === 'ENTREGADO').length;
  }
}

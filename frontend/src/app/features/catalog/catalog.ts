import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { Auth } from '../auth/services/auth';
import { PedidoService, PedidoResponse } from '../intranet/pedido.service';

import {
  LoteService,
  LoteCatalogo,
  FiltroCatalogo,
} from '../intranet/lote.service';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './catalog.html',
  styleUrl: './catalog.css',
})
export class Catalog implements OnInit {

  // ── Estado del catálogo ───────────────────────────────────────
  lotes: LoteCatalogo[] = [];
  loteSeleccionado: LoteCatalogo | null = null;
  cargando = false;
  error: string | null = null;

  // ── Filtros activos (RF05 / RF25) ────────────────────────────
  filtros: FiltroCatalogo = {};

  busquedaTexto  = '';
  categoriaActiva = 'Todos';
  calidadActiva   = 'Todos';
  precioMin: number | null = null;
  precioMax: number | null = null;
  ubicacionTexto  = '';

  categorias = ['Todos', 'FRUTAS', 'VERDURAS', 'TUBERCULOS', 'CEREALES', 'LEGUMBRES', 'HORTALIZAS', 'OTROS'];
  calidades  = ['Todos', 'PRIMERA', 'SEGUNDA', 'TERCERA'];

  // Etiquetas legibles para la UI
  readonly categoriaLabel: Record<string, string> = {
    FRUTAS: 'Frutas', VERDURAS: 'Verduras', TUBERCULOS: 'Tubérculos',
    CEREALES: 'Cereales', LEGUMBRES: 'Legumbres', HORTALIZAS: 'Hortalizas', OTROS: 'Otros'
  };
  readonly calidadLabel: Record<string, string> = {
    PRIMERA: '1.ª Calidad', SEGUNDA: '2.ª Calidad', TERCERA: '3.ª Calidad'
  };

  // ── RF07: Formulario de pedido en modal (paso 2) ──
  modalPaso: 'detalle' | 'pedido' | 'exito' = 'detalle';
  pedidoCantidad = 1;
  pedidoFecha = '';
  pedidoNotas = '';
  pedidoError = '';
  pedidoCargando = false;
  pedidoCreado: PedidoResponse | null = null;

  constructor(
    private loteService: LoteService,
    private auth: Auth,
    private pedidoService: PedidoService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarCatalogo();
  }

  // ── RF05 / RF25: Carga con filtros ───────────────────────────
  cargarCatalogo(): void {
    this.cargando = true;
    this.error = null;

    const filtros: FiltroCatalogo = {
      busqueda:  this.busquedaTexto.trim()  || undefined,
      categoria: this.categoriaActiva !== 'Todos' ? this.categoriaActiva : undefined,
      calidad:   this.calidadActiva   !== 'Todos' ? this.calidadActiva   : undefined,
      ubicacion: this.ubicacionTexto.trim() || undefined,
      precioMin: this.precioMin ?? undefined,
      precioMax: this.precioMax ?? undefined,
    };

    this.loteService.buscarCatalogo(filtros).subscribe({
      next: (data) => {
        this.lotes    = data;
        this.cargando = false;
      },
      error: () => {
        this.error    = 'No se pudo cargar el catálogo. Verifica que el servidor esté activo.';
        this.cargando = false;
      }
    });
  }

  // ── Filtros ───────────────────────────────────────────────────
  setCategoria(cat: string): void {
    this.categoriaActiva = cat;
    this.cargarCatalogo();
  }

  setCalidad(cal: string): void {
    this.calidadActiva = cal;
    this.cargarCatalogo();
  }

  aplicarFiltros(): void {
    this.cargarCatalogo();
  }

  limpiarFiltros(): void {
    this.busquedaTexto   = '';
    this.categoriaActiva = 'Todos';
    this.calidadActiva   = 'Todos';
    this.precioMin       = null;
    this.precioMax       = null;
    this.ubicacionTexto  = '';
    this.cargarCatalogo();
  }

  // ── Modal ─────────────────────────────────────────────────────
  abrirModal(lote: LoteCatalogo): void {
    this.loteSeleccionado = lote;
    this.modalPaso = 'detalle';
    this.pedidoCantidad = 1;
    this.pedidoFecha = '';
    this.pedidoNotas = '';
    this.pedidoError = '';
    this.pedidoCreado = null;
    document.body.style.overflow = 'hidden';
  }

  cerrarModal(): void {
    this.loteSeleccionado = null;
    document.body.style.overflow = '';
  }

  // ── Flujo de Pedidos (RF07) ──
  iniciarPedido(): void {
    if (!this.auth.isLoggedIn()) {
      this.cerrarModal();
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/catalog' } });
      return;
    }
    this.modalPaso = 'pedido';
    // Poner por defecto fecha de entrega estimada del lote si está en el formato correcto
    if (this.loteSeleccionado?.fechaEntregaEstimada) {
      this.pedidoFecha = this.loteSeleccionado.fechaEntregaEstimada;
    } else {
      const mañana = new Date();
      mañana.setDate(mañana.getDate() + 1);
      this.pedidoFecha = mañana.toISOString().split('T')[0];
    }
  }

  // RF19: stock total disponible del mismo producto sumando todos los lotes
  // ACTIVOS del catálogo (no solo el lote seleccionado). Permite que el
  // comprador pida más de lo que tiene un solo lote y deje que el backend
  // reparta el pedido entre varios agricultores.
  stockTotalProducto(lote: LoteCatalogo): number {
    return this.lotes
      .filter(l => l.nombreProducto === lote.nombreProducto && this.estaDisponible(l))
      .reduce((sum, l) => sum + l.stockDisponible, 0);
  }

  enviarPedido(): void {
    if (!this.loteSeleccionado) return;
    this.pedidoError = '';

    if (this.pedidoCantidad <= 0) {
      this.pedidoError = 'La cantidad debe ser mayor a 0.';
      return;
    }

    const stockTotal = this.stockTotalProducto(this.loteSeleccionado);
    if (this.pedidoCantidad > stockTotal) {
      this.pedidoError = `La cantidad supera el stock disponible entre todos los productores (${stockTotal} ${this.loteSeleccionado.unidadMedida}).`;
      return;
    }

    if (!this.pedidoFecha) {
      this.pedidoError = 'Debe especificar una fecha de entrega.';
      return;
    }

    const usuario = this.auth.getUsuarioActual();
    if (!usuario) {
      this.pedidoError = 'Sesión no válida. Por favor, inicia sesión de nuevo.';
      return;
    }

    this.pedidoCargando = true;
    const req = {
      compradorId: usuario.id,
      loteId: this.loteSeleccionado.loteId,
      cantidadSolicitada: this.pedidoCantidad,
      fechaEntregaDeseada: this.pedidoFecha,
      notasEspeciales: this.pedidoNotas
    };

    this.pedidoService.crearPedido(req).subscribe({
      next: (res) => {
        this.pedidoCargando = false;
        this.pedidoCreado = res;
        this.modalPaso = 'exito';
        // Recargar catálogo para actualizar el stock visible
        this.cargarCatalogo();
      },
      error: (err) => {
        this.pedidoCargando = false;
        this.pedidoError = err.error?.error || 'Error al procesar el pedido. Intente nuevamente.';
      }
    });
  }

  irAMisPedidos(): void {
    this.cerrarModal();
    this.router.navigate(['/intranet/comprador']);
  }

  // ── Helpers UI ────────────────────────────────────────────────
  estaDisponible(lote: LoteCatalogo): boolean {
    return lote.stockDisponible > 0 && lote.estado === 'ACTIVO';
  }

  imagenSrc(lote: LoteCatalogo): string {
    return lote.imagenUrl?.trim() || this.imagenFallback(lote.loteId);
  }

  private imagenFallback(loteId: number): string {
    return 'assets/img/p' + ((loteId % 6) + 1) + '.jpg';
  }

  onImgError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/img/p1.jpg';
  }

  labelCategoria(cat?: string): string {
    return cat ? (this.categoriaLabel[cat] ?? cat) : '—';
  }

  labelCalidad(cal?: string): string {
    return cal ? (this.calidadLabel[cal] ?? cal) : '—';
  }
}

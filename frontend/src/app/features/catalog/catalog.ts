import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

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

  constructor(private loteService: LoteService) {}

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
    document.body.style.overflow = 'hidden';
  }

  cerrarModal(): void {
    this.loteSeleccionado = null;
    document.body.style.overflow = '';
  }

  // ── Helpers UI ────────────────────────────────────────────────
  estaDisponible(lote: LoteCatalogo): boolean {
    return lote.stockDisponible > 0 && lote.estado === 'ACTIVO';
  }

  labelCategoria(cat?: string): string {
    return cat ? (this.categoriaLabel[cat] ?? cat) : '—';
  }

  labelCalidad(cal?: string): string {
    return cal ? (this.calidadLabel[cal] ?? cal) : '—';
  }
}

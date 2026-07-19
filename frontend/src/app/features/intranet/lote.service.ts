import { environment } from '../../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type CalidadLote = 'PRIMERA' | 'SEGUNDA' | 'TERCERA';
export type EstadoLoteComercial = 'ACTIVO' | 'INACTIVO' | 'AGOTADO';
export type TipoMovimiento = 'ENTRADA' | 'SALIDA' | 'AJUSTE';

// RF05 / RF25 — Respuesta del catálogo enriquecida con datos del Cultivo
export interface LoteCatalogo {
  loteId: number;
  cultivoId: number;
  nombreProducto: string;
  variedad?: string;
  categoria?: string;
  ubicacion?: string;
  agricultorId?: number;
  calidad: CalidadLote;
  precioUnitario: number;
  unidadMedida: string;
  stockDisponible: number;
  fechaEntregaEstimada: string;
  fechaCosecha?: string;
  condicionesEntrega?: string;
  estado: EstadoLoteComercial;
  imagenUrl?: string;
}

// Parámetros de búsqueda del catálogo (todos opcionales)
export interface FiltroCatalogo {
  busqueda?: string;
  categoria?: string;
  calidad?: string;
  precioMin?: number;
  precioMax?: number;
  ubicacion?: string;
  fechaDesde?: string;
  fechaHasta?: string;
}

export interface LoteComercial {
  id: number;
  cultivoId: number;
  cantidadKg: number;
  calidad: CalidadLote;
  precioUnitario: number;
  unidadMedida: string;
  stockDisponible: number;
  fechaCosecha?: string;
  fechaEntregaEstimada: string;
  condicionesEntrega?: string;
  publicado: boolean;
  fechaPublicacion?: string;
  estado: EstadoLoteComercial;
  imagenUrl?: string;
}

export interface LotePublicacionRequest {
  cultivoId: number;
  cantidadKg: number;
  calidad: CalidadLote;
  precioUnitario: number;
  unidadMedida: string;
  fechaEntregaEstimada: string;
  condicionesEntrega?: string;
  imagenUrl?: string;
}

export interface AjusteStockRequest {
  tipo: TipoMovimiento;
  cantidad: number;
  motivo: string;
}

export interface MovimientoStock {
  id: number;
  loteId: number;
  tipo: TipoMovimiento;
  cantidad: number;
  motivo: string;
  fechaMovimiento: string;
}

@Injectable({ providedIn: 'root' })
export class LoteService {
  private readonly API = environment.apiUrl + '/lotes';
  private readonly STOCK_API = environment.apiUrl + '/movimientos-stock';

  constructor(private http: HttpClient) {}

  // ── RF05 / RF25: Catálogo con filtros opcionales ─────────────
  buscarCatalogo(filtros: FiltroCatalogo = {}): Observable<LoteCatalogo[]> {
    let params = new HttpParams();
    if (filtros.busqueda)   params = params.set('busqueda',   filtros.busqueda);
    if (filtros.categoria)  params = params.set('categoria',  filtros.categoria);
    if (filtros.calidad)    params = params.set('calidad',    filtros.calidad);
    if (filtros.precioMin != null) params = params.set('precioMin', filtros.precioMin.toString());
    if (filtros.precioMax != null) params = params.set('precioMax', filtros.precioMax.toString());
    if (filtros.ubicacion)  params = params.set('ubicacion',  filtros.ubicacion);
    if (filtros.fechaDesde) params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params = params.set('fechaHasta', filtros.fechaHasta);
    return this.http.get<LoteCatalogo[]>(`${this.API}/publicados/buscar`, { params });
  }

  // ── RF04: Publicar lote ──────────────────────────────────────
  publicarLote(req: LotePublicacionRequest): Observable<LoteComercial> {
    return this.http.post<LoteComercial>(`${this.API}/publicar`, req);
  }

  // ── Consultas ────────────────────────────────────────────────
  getLotesPublicados(): Observable<LoteComercial[]> {
    return this.http.get<LoteComercial[]>(`${this.API}/publicados`);
  }

  getLotesPorCultivo(cultivoId: number): Observable<LoteComercial[]> {
    return this.http.get<LoteComercial[]>(`${this.API}/cultivo/${cultivoId}`);
  }

  getTodosLotes(): Observable<LoteComercial[]> {
    return this.http.get<LoteComercial[]>(this.API);
  }

  // ── RF09: Control de stock ────────────────────────────────────
  ajustarStock(loteId: number, req: AjusteStockRequest): Observable<LoteComercial> {
    return this.http.patch<LoteComercial>(`${this.API}/${loteId}/stock`, req);
  }

  // confirmarPedido()/cancelarPedido() de lote se quitaron: el descuento y
  // devolución de stock ahora se gestiona siempre desde PedidoService en el
  // backend (ver PedidoService), nunca directamente contra el lote.

  getMovimientosPorLote(loteId: number): Observable<MovimientoStock[]> {
    return this.http.get<MovimientoStock[]>(`${this.STOCK_API}/lote/${loteId}`);
  }

  actualizarPrecio(loteId: number, precio: number, motivo: string): Observable<LoteComercial> {
    return this.http.patch<LoteComercial>(`${this.API}/${loteId}/precio`, { precio, motivo });
  }

  getPrecioHistorial(loteId: number): Observable<HistorialPrecio[]> {
    return this.http.get<HistorialPrecio[]>(`${this.API}/${loteId}/precio-historial`);
  }
}

export interface HistorialPrecio {
  id: number;
  loteId: number;
  precioAnterior: number;
  precioNuevo: number;
  fechaCambio: string;
  usuarioResponsableEmail: string;
  motivo: string;
}

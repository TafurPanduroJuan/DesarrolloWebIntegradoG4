import { environment } from '../../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface VentaPorProducto {
  nombreProducto: string;
  categoria: string;
  cantidadVendidaKg: number;
  montoTotal: number;
  numeroPedidos: number;
}

export interface VentaPorAgricultor {
  agricultorId: number;
  nombreAgricultor: string;
  numeroPedidos: number;
  montoTotal: number;
}

export interface CambioPrecio {
  id: number;
  loteId: number;
  precioAnterior: number;
  precioNuevo: number;
  fechaCambio: string;
  usuarioResponsableEmail: string;
  motivo: string;
}

export interface ReporteVentas {
  desde: string | null;
  hasta: string | null;
  alcance: 'GLOBAL' | 'AGRICULTOR' | 'COMPRADOR';
  totalPedidos: number;
  totalIngresos: number;
  pedidosPorEstado: Record<string, number>;
  ventasPorProducto: VentaPorProducto[];
  ventasPorAgricultor: VentaPorAgricultor[];
  cambiosPrecio: CambioPrecio[];
}

export interface FiltrosReporte {
  desde?: string;
  hasta?: string;
  agricultorId?: number;
}

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly API = environment.apiUrl + '/reportes';

  constructor(private http: HttpClient) {}

  private construirParams(filtros: FiltrosReporte): HttpParams {
    let params = new HttpParams();
    if (filtros.desde) params = params.set('desde', filtros.desde);
    if (filtros.hasta) params = params.set('hasta', filtros.hasta);
    if (filtros.agricultorId) params = params.set('agricultorId', filtros.agricultorId);
    return params;
  }

  getReporteVentas(filtros: FiltrosReporte = {}): Observable<ReporteVentas> {
    return this.http.get<ReporteVentas>(`${this.API}/ventas`, { params: this.construirParams(filtros) });
  }

  exportarPdf(filtros: FiltrosReporte = {}): Observable<Blob> {
    return this.http.get(`${this.API}/ventas/exportar/pdf`, {
      params: this.construirParams(filtros),
      responseType: 'blob',
    });
  }

  exportarExcel(filtros: FiltrosReporte = {}): Observable<Blob> {
    return this.http.get(`${this.API}/ventas/exportar/excel`, {
      params: this.construirParams(filtros),
      responseType: 'blob',
    });
  }

  /** Dispara la descarga de un blob con el nombre de archivo dado. */
  descargarBlob(blob: Blob, nombreArchivo: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombreArchivo;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}

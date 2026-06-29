import { environment } from '../../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type EstadoPedido = 'PENDIENTE' | 'CONFIRMADO' | 'PREPARADO' | 'DESPACHADO' | 'ENTREGADO' | 'CANCELADO' | 'RECHAZADO';

export interface PedidoRequest {
  compradorId: number;
  loteId: number;
  cantidadSolicitada: number;
  fechaEntregaDeseada: string;
  notasEspeciales?: string;
}

export interface DetallePedidoResp {
  id: number;
  loteId: number;
  agricultorId: number;
  cantidadSolicitada: number;
  precioUnitario: number;
  subtotal: number;
  estadoDetalle: string;
  nombreProducto: string;
  unidadMedida: string;
}

export interface HistorialEstado {
  id: number;
  estadoAnterior: string | null;
  estadoNuevo: string;
  observacion: string;
  fechaCambio: string;
}

export interface PedidoResponse {
  id: number;
  compradorId: number;
  estado: EstadoPedido;
  notasEspeciales?: string;
  fechaPedido: string;
  fechaEntregaEstimada: string;
  totalEstimado: number;
  detalles: DetallePedidoResp[];
  historial: HistorialEstado[];
}

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly API = `${environment.apiUrl}/pedidos`;

  constructor(private http: HttpClient) {}

  crearPedido(req: PedidoRequest): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(this.API, req);
  }

  obtenerPedido(id: number): Observable<PedidoResponse> {
    return this.http.get<PedidoResponse>(`${this.API}/${id}`);
  }

  listarPorComprador(compradorId: number): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(`${this.API}/comprador/${compradorId}`);
  }

  listarPorAgricultor(agricultorId: number): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(`${this.API}/agricultor/${agricultorId}`);
  }

  confirmarPedido(pedidoId: number, agricultorId: number): Observable<PedidoResponse> {
    return this.http.patch<PedidoResponse>(
      `${this.API}/${pedidoId}/confirmar?agricultorId=${agricultorId}`, {}
    );
  }

  rechazarPedido(pedidoId: number, agricultorId: number, motivo: string): Observable<PedidoResponse> {
    return this.http.patch<PedidoResponse>(
      `${this.API}/${pedidoId}/rechazar`, { agricultorId, motivo }
    );
  }

  cambiarEstado(pedidoId: number, nuevoEstado: EstadoPedido, observacion?: string): Observable<PedidoResponse> {
    return this.http.patch<PedidoResponse>(
      `${this.API}/${pedidoId}/estado`, { nuevoEstado, observacion }
    );
  }

  obtenerHistorial(pedidoId: number): Observable<HistorialEstado[]> {
    return this.http.get<HistorialEstado[]>(`${this.API}/${pedidoId}/historial`);
  }
}

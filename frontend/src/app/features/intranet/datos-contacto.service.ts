import { environment } from '../../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// RF11 — Datos de contacto de la contraparte de un pedido,
// visibles solo cuando el backend autoriza la coordinación.
export interface ContactoPedido {
  usuarioId: number;
  nombreUsuario: string;
  rolUsuario: 'AGRICULTOR' | 'COMPRADOR';
  direccion?: string;
  referencia?: string;
  emailContacto?: string;
  telefonoAdicional?: string;
}

@Injectable({ providedIn: 'root' })
export class DatosContactoService {
  private readonly API = `${environment.apiUrl}/datos-contacto`;

  constructor(private http: HttpClient) {}

  // RF11: el backend valida que el solicitante sea parte del pedido
  // y que el pedido esté confirmado o en una etapa de coordinación autorizada.
  obtenerContactoPorPedido(pedidoId: number, solicitanteId: number): Observable<ContactoPedido> {
    return this.http.get<ContactoPedido>(
      `${this.API}/pedido/${pedidoId}?solicitanteId=${solicitanteId}`
    );
  }
}
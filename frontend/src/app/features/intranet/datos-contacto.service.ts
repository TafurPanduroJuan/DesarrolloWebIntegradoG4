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

  // RF11: el solicitante ya no se manda desde el cliente; el backend lo toma
  // del JWT y valida que sea parte del pedido y que esté en una etapa de
  // coordinación autorizada.
  obtenerContactoPorPedido(pedidoId: number): Observable<ContactoPedido> {
    return this.http.get<ContactoPedido>(`${this.API}/pedido/${pedidoId}`);
  }
}
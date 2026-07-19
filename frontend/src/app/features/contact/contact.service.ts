import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SolicitudContactoRequest {
  nombre: string;
  correo: string;
  telefono?: string;
  tipo?: string;
  mensaje: string;
}

export interface SolicitudContactoResponse extends SolicitudContactoRequest {
  id: number;
  fecha: string;
  atendida: boolean;
}

@Injectable({ providedIn: 'root' })
export class ContactService {
  private readonly API = `${environment.apiUrl}/solicitudes-contacto`;

  constructor(private http: HttpClient) {}

  enviar(solicitud: SolicitudContactoRequest): Observable<SolicitudContactoResponse> {
    return this.http.post<SolicitudContactoResponse>(this.API, solicitud);
  }
}

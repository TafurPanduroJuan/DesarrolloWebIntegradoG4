import { environment } from '../../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Notificacion {
  id: number;
  usuarioId: number;
  mensaje: string;
  tipo: string;
  fecha: string;
  leido: boolean;
  idReferencia?: number;
}

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private readonly API = environment.apiUrl + '/notificaciones';

  constructor(private http: HttpClient) {}

  getNotificaciones(): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(this.API);
  }

  getNoLeidasCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.API}/no-leidas/count`);
  }

  marcarLeida(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/${id}/leer`, {});
  }

  marcarTodasLeidas(): Observable<void> {
    return this.http.patch<void>(`${this.API}/leer-todas`, {});
  }
}

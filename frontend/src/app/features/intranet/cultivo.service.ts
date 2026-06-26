import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type EstadoCultivo = 'SEMBRADO' | 'CRECIMIENTO' | 'COSECHA' | 'FINALIZADO' | 'PERDIDA';
export type TipoEvento = 'SIEMBRA' | 'FERTILIZACION' | 'RIEGO' | 'CONTROL_PLAGAS' | 'COSECHA' | 'PERDIDA_PARCIAL';

export interface Cultivo {
  id: number;
  agricultorId: number;
  nombreProducto: string;
  variedad: string;
  categoria: string;
  nombreLote: string;
  areaHa: number;
  ubicacion: string;
  descripcion: string;
  fechaSiembra: string;
  fechaCosechaEstimada: string;
  estado: EstadoCultivo;
  etapaProductiva: string;
  observacionSeguimiento: string;
  fechaUltimoSeguimiento: string;
  fechaCreacion: string;
}

export interface SeguimientoRequest {
  estado: EstadoCultivo;
  etapaProductiva?: string;
  observacion?: string;
}

export interface EventoProduccionRequest {
  tipo: TipoEvento;
  descripcion: string;
  fecha: string;
  cultivoId: number;
}

export interface EventoProduccion {
  id: number;
  tipo: TipoEvento;
  descripcion: string;
  fecha: string;
  cultivoId: number;
}

@Injectable({ providedIn: 'root' })
export class CultivoService {
  private readonly API = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getMisCultivos(): Observable<Cultivo[]> {
    return this.http.get<Cultivo[]>(`${this.API}/cultivos/mis-cultivos`);
  }

  getCultivosPorAgricultor(agricultorId: number): Observable<Cultivo[]> {
    return this.http.get<Cultivo[]>(`${this.API}/cultivos/agricultor/${agricultorId}`);
  }

  guardarCultivo(datos: Partial<Cultivo>): Observable<Cultivo> {
    return this.http.post<Cultivo>(`${this.API}/cultivos`, datos);
  }

  actualizarCultivo(cultivoId: number, datos: Partial<Cultivo>): Observable<Cultivo> {
    return this.http.put<Cultivo>(`${this.API}/cultivos/${cultivoId}`, datos);
  }

  actualizarSeguimiento(cultivoId: number, req: SeguimientoRequest): Observable<Cultivo> {
    return this.http.patch<Cultivo>(`${this.API}/cultivos/${cultivoId}/seguimiento`, req);
  }

  agregarEvento(req: EventoProduccionRequest): Observable<EventoProduccion> {
    return this.http.post<EventoProduccion>(`${this.API}/eventos-produccion`, req);
  }

  getEventosPorCultivo(cultivoId: number): Observable<EventoProduccion[]> {
    return this.http.get<EventoProduccion[]>(`${this.API}/eventos-produccion/cultivo/${cultivoId}`);
  }

  eliminarCultivo(cultivoId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/cultivos/${cultivoId}`);
  }
}
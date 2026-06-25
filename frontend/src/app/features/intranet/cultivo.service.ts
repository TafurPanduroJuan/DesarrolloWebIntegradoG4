import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type EstadoLote   = 'activo' | 'en_descanso' | 'en_preparacion' | 'inactivo';
export type EtapaCultivo = 'sembrado' | 'crecimiento' | 'cosecha' | 'finalizado';
export type TipoEvento   = 'siembra' | 'fertilizacion' | 'riego' | 'control_plagas' | 'cosecha' | 'perdida_parcial';

export interface SeguimientoEntry {
  id: string;
  etapa: EtapaCultivo;
  observacion: string;
  fecha: string;
}

export interface EventoProduccion {
  id: string;
  tipo: TipoEvento;
  observacion: string;
  fecha: string;
}

export interface Cultivo {
  id: string;
  agricultorId: string;
  producto: string;
  variedad: string;
  nombreLote: string;
  area: number;
  ubicacion: string;
  fechaSiembra: string;
  estadoLote: EstadoLote;
  etapaActual: EtapaCultivo;
  seguimiento: SeguimientoEntry[];
  eventos: EventoProduccion[];
  fechaCreacion: string;
}

@Injectable({ providedIn: 'root' })
export class CultivoService {
  private readonly API = 'http://localhost:8080/api/cultivos';

  constructor(private http: HttpClient) {}

  getCultivosPorAgricultor(agricultorId: string): Observable<Cultivo[]> {
    return this.http.get<Cultivo[]>(`${this.API}/agricultor/${agricultorId}`);
  }

  guardarCultivo(datos: Omit<Cultivo, 'id' | 'agricultorId' | 'fechaCreacion' | 'seguimiento' | 'eventos'>): Observable<Cultivo> {
    return this.http.post<Cultivo>(this.API, datos);
  }

  actualizarLoteCultivo(cultivoId: string, lote: Pick<Cultivo, 'nombreLote' | 'area' | 'ubicacion' | 'estadoLote'>): Observable<Cultivo> {
    return this.http.put<Cultivo>(`${this.API}/${cultivoId}`, lote);
  }

  agregarSeguimiento(cultivoId: string, entry: Omit<SeguimientoEntry, 'id'>): Observable<Cultivo> {
    return this.http.patch<Cultivo>(`${this.API}/${cultivoId}/seguimiento`, entry);
  }

  agregarEvento(cultivoId: string, evento: Omit<EventoProduccion, 'id'>): Observable<Cultivo> {
    return this.http.patch<Cultivo>(`${this.API}/${cultivoId}/seguimiento`, evento);
  }
}
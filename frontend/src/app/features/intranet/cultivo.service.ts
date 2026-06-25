import { Injectable } from '@angular/core';
import { Auth } from '../auth/services/auth';

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
  private readonly CULTIVOS_KEY = 'agrolink_cultivos';

  constructor(private auth: Auth) {}

  getCultivos(): Cultivo[] {
    return JSON.parse(localStorage.getItem(this.CULTIVOS_KEY) || '[]');
  }

  getCultivosPorAgricultor(agricultorId: string): Cultivo[] {
    return this.getCultivos().filter(c => c.agricultorId === agricultorId);
  }

  guardarCultivo(datos: Omit<Cultivo, 'id' | 'agricultorId' | 'fechaCreacion' | 'seguimiento' | 'eventos'>): void {
    const usuario = this.auth.getUsuarioActual();
    if (!usuario) return;
    const cultivos = this.getCultivos();
    cultivos.push({
      ...datos,
      id: crypto.randomUUID(),
      agricultorId: String(usuario.id),
      fechaCreacion: new Date().toISOString().split('T')[0],
      seguimiento: [],
      eventos: [],
    });
    localStorage.setItem(this.CULTIVOS_KEY, JSON.stringify(cultivos));
  }

  actualizarLoteCultivo(cultivoId: string, lote: Pick<Cultivo, 'nombreLote' | 'area' | 'ubicacion' | 'estadoLote'>): void {
    const cultivos = this.getCultivos();
    const c = cultivos.find(x => x.id === cultivoId);
    if (c) {
      c.nombreLote = lote.nombreLote;
      c.area       = lote.area;
      c.ubicacion  = lote.ubicacion;
      c.estadoLote = lote.estadoLote;
      localStorage.setItem(this.CULTIVOS_KEY, JSON.stringify(cultivos));
    }
  }

  agregarSeguimiento(cultivoId: string, entry: Omit<SeguimientoEntry, 'id'>): void {
    const cultivos = this.getCultivos();
    const c = cultivos.find(x => x.id === cultivoId);
    if (c) {
      c.etapaActual = entry.etapa;
      c.seguimiento.push({ ...entry, id: crypto.randomUUID() });
      localStorage.setItem(this.CULTIVOS_KEY, JSON.stringify(cultivos));
    }
  }

  agregarEvento(cultivoId: string, evento: Omit<EventoProduccion, 'id'>): void {
    const cultivos = this.getCultivos();
    const c = cultivos.find(x => x.id === cultivoId);
    if (c) {
      c.eventos.push({ ...evento, id: crypto.randomUUID() });
      localStorage.setItem(this.CULTIVOS_KEY, JSON.stringify(cultivos));
    }
  }
}

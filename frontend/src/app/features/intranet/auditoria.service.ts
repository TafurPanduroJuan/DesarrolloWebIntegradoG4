import { environment } from '../../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Auditoria {
  id: number;
  accion: string;
  descripcion: string;
  fecha: string;
  usuarioEmail: string;
  ipAddress: string;
}

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly API = environment.apiUrl + '/auditorias';

  constructor(private http: HttpClient) {}

  getAuditorias(): Observable<Auditoria[]> {
    return this.http.get<Auditoria[]>(this.API);
  }
}

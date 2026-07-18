import { environment } from '../../../environments/environment';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BackupInfo {
  nombre: string;
  tamanioBytes: number;
  fecha: string;
}

export interface ResultadoBackup {
  archivo: string;
  mensaje: string;
}

@Injectable({ providedIn: 'root' })
export class BackupService {
  private readonly API = environment.apiUrl + '/backups';

  constructor(private http: HttpClient) {}

  listar(): Observable<BackupInfo[]> {
    return this.http.get<BackupInfo[]>(this.API);
  }

  ejecutarAhora(): Observable<ResultadoBackup> {
    return this.http.post<ResultadoBackup>(`${this.API}/ejecutar`, {});
  }

  descargar(nombreArchivo: string): Observable<Blob> {
    return this.http.get(`${this.API}/${nombreArchivo}/descargar`, { responseType: 'blob' });
  }
}

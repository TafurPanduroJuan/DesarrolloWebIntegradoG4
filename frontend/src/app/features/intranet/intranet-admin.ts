import { environment } from '../../../environments/environment';
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Auth, UsuarioSesion } from '../auth/services/auth';

export interface UsuarioAdmin {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  rol: string;
  activo: boolean;
  estadoValidacion?: string;
}

@Component({
  selector: 'app-intranet-admin',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './intranet-admin.html',
  styleUrl: './intranet-admin.css',
})
export class IntranetAdmin implements OnInit {
  usuario: UsuarioSesion | null = null;
  seccionActiva = signal<string>('resumen');

  usuarios: UsuarioAdmin[] = [];

  private readonly API = environment.apiUrl;

  constructor(
    private auth: Auth,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuarioActual();
    if (!this.usuario || this.usuario.rol !== 'ADMINISTRADOR') {
      this.router.navigate(['/intranet']);
      return;
    }
    this.cargarDatos();
  }

  cargarDatos() {
    this.http.get<UsuarioAdmin[]>(`${this.API}/usuarios`).subscribe({
      next: (data) => this.usuarios = data,
      error: () => this.usuarios = []
    });
  }

  irA(seccion: string) { this.seccionActiva.set(seccion); }
  cerrarSesion() { this.auth.logout(); }

  estado(activo: boolean): string {
    return activo ? 'Activo' : 'Inactivo';
  }

  aprobarAgricultor(id: number) {
    this.http.patch(`${this.API}/usuarios/${id}/validacion`, {
      estadoValidacion: 'APROBADO'
    }).subscribe(() => this.cargarDatos());
  }

  rechazarAgricultor(id: number) {
    const motivo = prompt('Motivo del rechazo:');
    this.http.patch(`${this.API}/usuarios/${id}/validacion`, {
      estadoValidacion: 'RECHAZADO',
      motivoObservacion: motivo || 'Sin motivo'
    }).subscribe(() => this.cargarDatos());
  }

  toggleUsuario(id: number, activo: boolean) {
    this.http.patch(`${this.API}/usuarios/${id}/estado`, { activo: !activo })
      .subscribe(() => this.cargarDatos());
  }

  get solicitudesPendientes() {
    return this.usuarios.filter(u => u.rol === 'AGRICULTOR' && u.estadoValidacion === 'PENDIENTE');
  }
  get totalAgricultores() { return this.usuarios.filter(u => u.rol === 'AGRICULTOR').length; }
  get totalCompradores() { return this.usuarios.filter(u => u.rol === 'COMPRADOR').length; }
}
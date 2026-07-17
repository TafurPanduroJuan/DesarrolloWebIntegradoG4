import { environment } from '../../../environments/environment';
import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Auth, UsuarioSesion } from '../auth/services/auth';
import { AuditoriaService, Auditoria } from './auditoria.service';

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
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './intranet-admin.html',
  styleUrl: './intranet-admin.css',
})
export class IntranetAdmin implements OnInit {
  usuario: UsuarioSesion | null = null;
  seccionActiva = signal<string>('resumen');

  usuarios: UsuarioAdmin[] = [];
  auditorias: Auditoria[] = [];
  cargandoAuditorias = false;

  // Modal de Rechazo (RF-15)
  showRechazoModal = false;
  motivoRechazo = '';
  usuarioRechazoId: number | null = null;
  errorRechazoModal = '';

  private readonly API = environment.apiUrl;

  constructor(
    private auth: Auth,
    private router: Router,
    private http: HttpClient,
    private auditoriaService: AuditoriaService
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

  irA(seccion: string) {
    this.seccionActiva.set(seccion);
    if (seccion === 'auditoria') {
      this.cargarAuditorias();
    }
  }

  cargarAuditorias() {
    this.cargandoAuditorias = true;
    this.auditoriaService.getAuditorias().subscribe({
      next: (data) => {
        this.auditorias = data;
        this.cargandoAuditorias = false;
      },
      error: () => {
        this.auditorias = [];
        this.cargandoAuditorias = false;
      }
    });
  }

  cerrarSesion() { this.auth.logout(); }

  estado(activo: boolean): string {
    return activo ? 'Activo' : 'Inactivo';
  }

  friendlyAccion(accion: string): string {
    const mapping: Record<string, string> = {
      'CREACION_USUARIO': 'Registro de Usuario',
      'CONFIRMACION_PEDIDO': 'Pedido Confirmado',
      'CAMBIO_STOCK': 'Cambio de Stock',
      'CONTROL_ACCESO': 'Acceso de Cuenta',
      'CAMBIO_PRECIO': 'Cambio de Precio',
      'VALIDACION_CUENTA': 'Validación de Cuenta'
    };
    return mapping[accion] || accion;
  }

  aprobarAgricultor(id: number) {
    this.http.patch(`${this.API}/usuarios/${id}/validacion`, {
      estadoValidacion: 'APROBADO'
    }).subscribe(() => this.cargarDatos());
  }

  rechazarAgricultor(id: number) {
    this.abrirModalRechazo(id);
  }

  abrirModalRechazo(id: number) {
    this.usuarioRechazoId = id;
    this.motivoRechazo = '';
    this.errorRechazoModal = '';
    this.showRechazoModal = true;
  }

  cerrarModalRechazo() {
    this.showRechazoModal = false;
    this.usuarioRechazoId = null;
    this.motivoRechazo = '';
    this.errorRechazoModal = '';
  }

  confirmarRechazo() {
    if (!this.usuarioRechazoId) return;
    if (!this.motivoRechazo.trim()) {
      this.errorRechazoModal = 'El motivo del rechazo es obligatorio';
      return;
    }
    
    this.http.patch(`${this.API}/usuarios/${this.usuarioRechazoId}/validacion`, {
      estadoValidacion: 'RECHAZADO',
      motivoObservacion: this.motivoRechazo
    }).subscribe({
      next: () => {
        this.cargarDatos();
        this.cerrarModalRechazo();
      },
      error: () => {
        this.errorRechazoModal = 'Error al registrar el rechazo de la cuenta.';
      }
    });
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
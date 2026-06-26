import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth, UsuarioSesion, ProductoAgrolink } from '../auth/services/auth';
import { CultivoService, Cultivo, EstadoCultivo, TipoEvento } from './cultivo.service';

@Component({
  selector: 'app-intranet-agricultor',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './intranet-agricultor.html',
  styleUrl: './intranet-agricultor.css',
})
export class IntranetAgricultor implements OnInit {
  usuario: UsuarioSesion | null = null;
  seccionActiva = signal<string>('resumen');

  // PRODUCTOS
  misProductos: ProductoAgrolink[] = [];
  formProducto = { nombre: '', descripcion: '', precio: 0, categoria: '', unidad: 'kg', stock: 0 };
  mensajeExito = '';

  // CULTIVOS
  misCultivos: Cultivo[] = [];
  panelActivo: string | null = null;
  cultivoSeleccionado: Cultivo | null = null;

  formCultivo = {
    nombreProducto: '',
    variedad: '',
    categoria: '',
    nombreLote: '',
    areaHa: 0,
    ubicacion: '',
    descripcion: '',
    fechaSiembra: '',
    fechaCosechaEstimada: '',
    estado: 'SEMBRADO' as EstadoCultivo,
    etapaProductiva: '',
  };
  mensajeExitoCultivo = '';

  formLote = {
    nombreLote: '',
    areaHa: 0,
    ubicacion: '',
    descripcion: '',
  };
  mensajeExitoLote = '';

  formSeguimiento = {
    estado: 'SEMBRADO' as EstadoCultivo,
    etapaProductiva: '',
    observacion: '',
  };
  mensajeExitoSeguimiento = '';

  formEvento = {
    tipo: 'SIEMBRA' as TipoEvento,
    descripcion: '',
    fecha: '',
  };
  mensajeExitoEvento = '';

  constructor(private auth: Auth, private cultivos: CultivoService, private router: Router) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuarioActual();
    if (!this.usuario || this.usuario.rol !== 'AGRICULTOR') {
      this.router.navigate(['/']);
      return;
    }
    this.cargarDatos();
  }

  cargarDatos() {
    if (this.usuario) {
      this.misProductos = this.auth.getProductosPorAgricultor(String(this.usuario.id));
      this.cultivos.getMisCultivos().subscribe({
        next: (data) => this.misCultivos = data,
        error: () => this.misCultivos = []
      });
    }
  }

  irA(seccion: string) {
    this.seccionActiva.set(seccion);
    this.panelActivo = null;
    this.cultivoSeleccionado = null;
  }

  cerrarSesion() { this.auth.logout(); }

  // PRODUCTOS
  subirProducto() {
    this.auth.subirProducto({ ...this.formProducto });
    this.formProducto = { nombre: '', descripcion: '', precio: 0, categoria: '', unidad: 'kg', stock: 0 };
    this.mensajeExito = '¡Producto enviado a revisión exitosamente!';
    setTimeout(() => this.mensajeExito = '', 5000);
    this.cargarDatos();
    this.irA('productos');
  }

  get totalProductos()      { return this.misProductos.length; }
  get productosAprobados()  { return this.misProductos.filter(p => p.estado === 'aprobado').length; }
  get productosPendientes() { return this.misProductos.filter(p => p.estado === 'pendiente').length; }
  get productosRechazados() { return this.misProductos.filter(p => p.estado === 'rechazado').length; }

  estadoBadgeClass(estado: string): string {
    const map: Record<string, string> = {
      'aprobado':  'badge-entregado',
      'rechazado': 'badge-cancelado',
      'pendiente': 'badge-pendiente',
    };
    return map[estado] || 'badge-default';
  }

  // CULTIVOS - RF02
  guardarCultivo() {
    this.cultivos.guardarCultivo({ ...this.formCultivo }).subscribe({
      next: () => {
        this.formCultivo = {
          nombreProducto: '', variedad: '', categoria: '', nombreLote: '',
          areaHa: 0, ubicacion: '', descripcion: '', fechaSiembra: '',
          fechaCosechaEstimada: '', estado: 'SEMBRADO', etapaProductiva: '',
        };
        this.mensajeExitoCultivo = '¡Cultivo registrado exitosamente!';
        setTimeout(() => this.mensajeExitoCultivo = '', 5000);
        this.cargarDatos();
        this.irA('cultivos');
      },
      error: () => this.mensajeExitoCultivo = 'Error al registrar cultivo.'
    });
  }

  // LOTES - RF16
  abrirEditarLote(cultivo: Cultivo) {
    this.cultivoSeleccionado = cultivo;
    this.formLote = {
      nombreLote: cultivo.nombreLote,
      areaHa:     cultivo.areaHa,
      ubicacion:  cultivo.ubicacion,
      descripcion: cultivo.descripcion,
    };
    this.panelActivo = 'editar-lote';
  }

  guardarLote() {
    if (!this.cultivoSeleccionado) return;
    this.cultivos.actualizarCultivo(this.cultivoSeleccionado.id, { ...this.formLote }).subscribe({
      next: () => {
        this.mensajeExitoLote = '¡Lote actualizado correctamente!';
        setTimeout(() => this.mensajeExitoLote = '', 4000);
        this.cargarDatos();
      },
      error: () => this.mensajeExitoLote = 'Error al actualizar lote.'
    });
  }

  // SEGUIMIENTO - RF03
  abrirSeguimiento(cultivo: Cultivo) {
    this.cultivoSeleccionado = cultivo;
    this.formSeguimiento = {
      estado: cultivo.estado,
      etapaProductiva: cultivo.etapaProductiva || '',
      observacion: '',
    };
    this.panelActivo = 'seguimiento';
  }

  guardarSeguimiento() {
    if (!this.cultivoSeleccionado) return;
    this.cultivos.actualizarSeguimiento(this.cultivoSeleccionado.id, { ...this.formSeguimiento }).subscribe({
      next: () => {
        this.formSeguimiento = { estado: this.formSeguimiento.estado, etapaProductiva: '', observacion: '' };
        this.mensajeExitoSeguimiento = '¡Seguimiento registrado!';
        setTimeout(() => this.mensajeExitoSeguimiento = '', 4000);
        this.cargarDatos();
      },
      error: () => this.mensajeExitoSeguimiento = 'Error al registrar seguimiento.'
    });
  }

  // EVENTOS - RF17
  abrirEventos(cultivo: Cultivo) {
    this.cultivoSeleccionado = cultivo;
    this.formEvento = {
      tipo: 'SIEMBRA',
      descripcion: '',
      fecha: new Date().toISOString().split('T')[0],
    };
    this.panelActivo = 'eventos';
  }

  guardarEvento() {
    if (!this.cultivoSeleccionado) return;
    this.cultivos.agregarEvento({
      ...this.formEvento,
      cultivoId: this.cultivoSeleccionado.id
    }).subscribe({
      next: () => {
        this.formEvento = { tipo: this.formEvento.tipo, descripcion: '', fecha: new Date().toISOString().split('T')[0] };
        this.mensajeExitoEvento = '¡Evento registrado!';
        setTimeout(() => this.mensajeExitoEvento = '', 4000);
      },
      error: () => this.mensajeExitoEvento = 'Error al registrar evento.'
    });
  }

  cerrarPanel() {
    this.panelActivo = null;
    this.cultivoSeleccionado = null;
  }

  // KPIs
  get totalCultivos()   { return this.misCultivos.length; }
  get cultivosActivos() { return this.misCultivos.filter(c => c.estado === 'SEMBRADO' || c.estado === 'CRECIMIENTO').length; }

  estadoCultivoLabel(estado: EstadoCultivo): string {
    const map: Record<EstadoCultivo, string> = {
      'SEMBRADO':    'Sembrado',
      'CRECIMIENTO': 'Crecimiento',
      'COSECHA':     'Cosecha',
      'FINALIZADO':  'Finalizado',
      'PERDIDA':     'Pérdida',
    };
    return map[estado] || estado;
  }

  estadoCultivoBadgeClass(estado: EstadoCultivo): string {
    const map: Record<EstadoCultivo, string> = {
      'SEMBRADO':    'badge-etapa-sembrado',
      'CRECIMIENTO': 'badge-etapa-crecimiento',
      'COSECHA':     'badge-etapa-cosecha',
      'FINALIZADO':  'badge-etapa-finalizado',
      'PERDIDA':     'badge-cancelado',
    };
    return map[estado] || '';
  }

  tipoEventoLabel(tipo: TipoEvento): string {
    const map: Record<TipoEvento, string> = {
      'SIEMBRA':         '🌱 Siembra',
      'FERTILIZACION':   '🧪 Fertilización',
      'RIEGO':           '💧 Riego',
      'CONTROL_PLAGAS':  '🐛 Control de Plagas',
      'COSECHA':         '🌾 Cosecha',
      'PERDIDA_PARCIAL': '⚠️ Pérdida Parcial',
    };
    return map[tipo] || tipo;
  }
}
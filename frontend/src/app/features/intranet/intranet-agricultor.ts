import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth, UsuarioSesion, ProductoAgrolink } from '../auth/services/auth';
import { CultivoService, Cultivo, SeguimientoEntry, EventoProduccion, EstadoLote, EtapaCultivo, TipoEvento } from './cultivo.service';

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

  // ──────────────────────────────────────────
  // PRODUCTOS (código existente sin cambios)
  // ──────────────────────────────────────────
  misProductos: ProductoAgrolink[] = [];

  formProducto = {
    nombre: '',
    descripcion: '',
    precio: 0,
    categoria: '',
    unidad: 'kg',
    stock: 0
  };

  mensajeExito = '';

  // ──────────────────────────────────────────
  // CULTIVOS (RF02 · RF03 · RF16 · RF17)
  // ──────────────────────────────────────────
  misCultivos: Cultivo[] = [];

  // Vista de detalle activa: 'seguimiento' | 'eventos' | 'editar-lote' | null
  panelActivo: string | null = null;
  cultivoSeleccionado: Cultivo | null = null;

  // Formulario: Nuevo cultivo (RF02 + RF16)
  formCultivo = {
    producto: '',
    variedad: '',
    fechaSiembra: '',
    etapaActual: 'sembrado' as EtapaCultivo,
    nombreLote: '',
    area: 0,
    ubicacion: '',
    estadoLote: 'activo' as EstadoLote,
  };
  mensajeExitoCultivo = '';

  // Formulario: Editar lote (RF16)
  formLote = {
    nombreLote: '',
    area: 0,
    ubicacion: '',
    estadoLote: 'activo' as EstadoLote,
  };
  mensajeExitoLote = '';

  // Formulario: Seguimiento (RF03)
  formSeguimiento = {
    etapa: 'sembrado' as EtapaCultivo,
    observacion: '',
    fecha: '',
  };
  mensajeExitoSeguimiento = '';

  // Formulario: Evento (RF17)
  formEvento = {
    tipo: 'siembra' as TipoEvento,
    observacion: '',
    fecha: '',
  };
  mensajeExitoEvento = '';

  constructor(private auth: Auth, private cultivos: CultivoService, private router: Router) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuarioActual();
    // Doble validación, por si entra directo a la URL
    if (!this.usuario || this.usuario.rol !== 'AGRICULTOR') {
      this.router.navigate(['/']);
      return;
    }
    this.cargarDatos();
  }

  cargarDatos() {
    if (this.usuario) {
      this.misProductos = this.auth.getProductosPorAgricultor(String(this.usuario.id));
      this.cultivos.getCultivosPorAgricultor(String(this.usuario.id)).subscribe({
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

  // ──────────────────────────────────────────
  // PRODUCTOS — Lógica existente sin cambios
  // ──────────────────────────────────────────
  subirProducto() {
    this.auth.subirProducto({ ...this.formProducto });
    this.formProducto = { nombre: '', descripcion: '', precio: 0, categoria: '', unidad: 'kg', stock: 0 };
    this.mensajeExito = 'Producto enviado a revisión exitosamente.';
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

  // ──────────────────────────────────────────
  // CULTIVOS — RF02: Registrar cultivo
  // ──────────────────────────────────────────
  guardarCultivo() {
    this.cultivos.guardarCultivo({ ...this.formCultivo }).subscribe({
      next: () => {
        this.formCultivo = {
          producto: '', variedad: '', fechaSiembra: '', etapaActual: 'sembrado',
          nombreLote: '', area: 0, ubicacion: '', estadoLote: 'activo',
        };
        this.mensajeExitoCultivo = '¡Cultivo registrado exitosamente!';
        setTimeout(() => this.mensajeExitoCultivo = '', 5000);
        this.cargarDatos();
        this.irA('cultivos');
      },
      error: () => this.mensajeExitoCultivo = 'Error al registrar cultivo.'
    });
  }

  // ──────────────────────────────────────────
  // LOTES — RF16: Editar lote
  // ──────────────────────────────────────────
  abrirEditarLote(cultivo: Cultivo) {
    this.cultivoSeleccionado = cultivo;
    this.formLote = {
      nombreLote: cultivo.nombreLote,
      area:       cultivo.area,
      ubicacion:  cultivo.ubicacion,
      estadoLote: cultivo.estadoLote,
    };
    this.panelActivo = 'editar-lote';
  }

  guardarLote() {
    if (!this.cultivoSeleccionado) return;
    this.cultivos.actualizarLoteCultivo(this.cultivoSeleccionado.id, { ...this.formLote }).subscribe({
      next: () => {
        this.mensajeExitoLote = '¡Lote actualizado correctamente!';
        setTimeout(() => this.mensajeExitoLote = '', 4000);
        this.cargarDatos();
        this.cultivoSeleccionado = this.misCultivos.find(c => c.id === this.cultivoSeleccionado!.id) || null;
      },
      error: () => this.mensajeExitoLote = 'Error al actualizar lote.'
    });
  }

  // ──────────────────────────────────────────
  // SEGUIMIENTO — RF03
  // ──────────────────────────────────────────
  abrirSeguimiento(cultivo: Cultivo) {
    this.cultivoSeleccionado = cultivo;
    this.formSeguimiento = {
      etapa:       cultivo.etapaActual,
      observacion: '',
      fecha:       new Date().toISOString().split('T')[0],
    };
    this.panelActivo = 'seguimiento';
  }

  guardarSeguimiento() {
    if (!this.cultivoSeleccionado) return;
    this.cultivos.agregarSeguimiento(this.cultivoSeleccionado.id, { ...this.formSeguimiento }).subscribe({
      next: () => {
        this.formSeguimiento = { etapa: this.formSeguimiento.etapa, observacion: '', fecha: new Date().toISOString().split('T')[0] };
        this.mensajeExitoSeguimiento = '¡Seguimiento registrado!';
        setTimeout(() => this.mensajeExitoSeguimiento = '', 4000);
        this.cargarDatos();
        this.cultivoSeleccionado = this.misCultivos.find(c => c.id === this.cultivoSeleccionado!.id) || null;
      },
      error: () => this.mensajeExitoSeguimiento = 'Error al registrar seguimiento.'
    });
  }

  // ──────────────────────────────────────────
  // EVENTOS — RF17
  // ──────────────────────────────────────────
  abrirEventos(cultivo: Cultivo) {
    this.cultivoSeleccionado = cultivo;
    this.formEvento = {
      tipo:        'siembra',
      observacion: '',
      fecha:       new Date().toISOString().split('T')[0],
    };
    this.panelActivo = 'eventos';
  }

  guardarEvento() {
    if (!this.cultivoSeleccionado) return;
    this.cultivos.agregarEvento(this.cultivoSeleccionado.id, { ...this.formEvento }).subscribe({
      next: () => {
        this.formEvento = { tipo: this.formEvento.tipo, observacion: '', fecha: new Date().toISOString().split('T')[0] };
        this.mensajeExitoEvento = '¡Evento registrado!';
        setTimeout(() => this.mensajeExitoEvento = '', 4000);
        this.cargarDatos();
        this.cultivoSeleccionado = this.misCultivos.find(c => c.id === this.cultivoSeleccionado!.id) || null;
      },
      error: () => this.mensajeExitoEvento = 'Error al registrar evento.'
    });
  }

  cerrarPanel() {
    this.panelActivo = null;
    this.cultivoSeleccionado = null;
  }

  // ──────────────────────────────────────────
  // KPIs Cultivos
  // ──────────────────────────────────────────
  get totalCultivos()   { return this.misCultivos.length; }
  get cultivosActivos() { return this.misCultivos.filter(c => c.estadoLote === 'activo').length; }

  etapaBadgeClass(etapa: EtapaCultivo): string {
    const map: Record<EtapaCultivo, string> = {
      'sembrado':   'badge-etapa-sembrado',
      'crecimiento':'badge-etapa-crecimiento',
      'cosecha':    'badge-etapa-cosecha',
      'finalizado': 'badge-etapa-finalizado',
    };
    return map[etapa] || '';
  }

  estadoLoteBadgeClass(estado: EstadoLote): string {
    const map: Record<EstadoLote, string> = {
      'activo':          'badge-lote-activo',
      'en_descanso':     'badge-lote-descanso',
      'en_preparacion':  'badge-lote-preparacion',
      'inactivo':        'badge-lote-inactivo',
    };
    return map[estado] || '';
  }

  estadoLoteLabel(estado: EstadoLote): string {
    const map: Record<EstadoLote, string> = {
      'activo':         'Activo',
      'en_descanso':    'En Descanso',
      'en_preparacion': 'En Preparación',
      'inactivo':       'Inactivo',
    };
    return map[estado] || estado;
  }

  etapaLabel(etapa: EtapaCultivo): string {
    const map: Record<EtapaCultivo, string> = {
      'sembrado':    'Sembrado',
      'crecimiento': 'Crecimiento',
      'cosecha':     'Cosecha',
      'finalizado':  'Finalizado',
    };
    return map[etapa] || etapa;
  }

  tipoEventoLabel(tipo: TipoEvento): string {
    const map: Record<TipoEvento, string> = {
      'siembra':         '🌱 Siembra',
      'fertilizacion':   '🧪 Fertilización',
      'riego':           '💧 Riego',
      'control_plagas':  '🐛 Control de Plagas',
      'cosecha':         '🌾 Cosecha',
      'perdida_parcial': '⚠️ Pérdida Parcial',
    };
    return map[tipo] || tipo;
  }
}

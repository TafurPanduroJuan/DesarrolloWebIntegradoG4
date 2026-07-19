import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth, UsuarioSesion } from '../auth/services/auth';
import {
  CultivoService,
  Cultivo,
  EstadoCultivo,
  TipoEvento,
  SeguimientoRequest,
  EventoProduccionRequest,
  EventoProduccion
} from './cultivo.service';
import {
  LoteService,
  LoteComercial,
  LotePublicacionRequest,
  AjusteStockRequest,
  MovimientoStock,
  CalidadLote,
  TipoMovimiento,
  HistorialPrecio
} from './lote.service';
import { PedidoService, PedidoResponse, EstadoPedido, DetallePedidoResp } from './pedido.service';
import { DatosContactoService, ContactoPedido } from './datos-contacto.service';
import { NotificacionService, Notificacion } from './notificacion.service';

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
  // CULTIVOS (RF02 · RF03 · RF16 · RF17)
  // ──────────────────────────────────────────
  misCultivos: Cultivo[] = [];
  cultivoSeleccionado: Cultivo | null = null;
  panelActivo: string | null = null;

  // Eventos cargados para el cultivo seleccionado
  eventosCultivo: EventoProduccion[] = [];

  // Formulario: Nuevo cultivo (RF02)
  formCultivo: {
    nombreProducto: string;
    variedad: string;
    categoria: string;
    fechaSiembra: string;
    estado: EstadoCultivo;
    nombreLote: string;
    areaHa: number;
    ubicacion: string;
    descripcion: string;
  } = {
    nombreProducto: '',
    variedad: '',
    categoria: '',
    fechaSiembra: '',
    estado: 'SEMBRADO',
    nombreLote: '',
    areaHa: 0,
    ubicacion: '',
    descripcion: ''
  };
  mensajeExitoCultivo = '';

  // Opciones de categoría para el formulario (deben coincidir con CategoriaProductoEnum del backend)
  categoriasCultivo: string[] = ['FRUTAS', 'VERDURAS', 'TUBERCULOS', 'CEREALES', 'LEGUMBRES', 'HORTALIZAS', 'OTROS'];
  readonly categoriaCultivoLabel: Record<string, string> = {
    FRUTAS: 'Frutas', VERDURAS: 'Verduras', TUBERCULOS: 'Tubérculos',
    CEREALES: 'Cereales', LEGUMBRES: 'Legumbres', HORTALIZAS: 'Hortalizas', OTROS: 'Otros'
  };

  // Formulario: Seguimiento (RF03)
  formSeguimiento: SeguimientoRequest = {
    estado: 'SEMBRADO',
    etapaProductiva: '',
    observacion: ''
  };
  mensajeExitoSeguimiento = '';

  // Formulario: Evento (RF17)
  formEvento: {
    tipo: TipoEvento;
    descripcion: string;
    fecha: string;
  } = {
    tipo: 'SIEMBRA',
    descripcion: '',
    fecha: new Date().toISOString().split('T')[0]
  };
  mensajeExitoEvento = '';

  estadosCultivo: EstadoCultivo[] = ['SEMBRADO', 'CRECIMIENTO', 'COSECHA', 'FINALIZADO', 'PERDIDA'];
  tiposEvento: TipoEvento[] = ['SIEMBRA', 'FERTILIZACION', 'RIEGO', 'CONTROL_PLAGAS', 'COSECHA', 'PERDIDA_PARCIAL'];

  // ──────────────────────────────────────────
  // LOTES COMERCIALES (RF04 · RF09)
  // ──────────────────────────────────────────
  misLotes: LoteComercial[] = [];
  loteSeleccionado: LoteComercial | null = null;
  panelLoteActivo: string | null = null;

  formLoteComercial: LotePublicacionRequest = {
    cultivoId: 0,
    cantidadKg: 0,
    calidad: 'PRIMERA',
    precioUnitario: 0,
    unidadMedida: 'kg',
    fechaEntregaEstimada: '',
    condicionesEntrega: '',
    imagenUrl: ''
  };
  mensajeExitoLoteComercial = '';
  errorLoteComercial = '';
  errorImagenLote = '';
  private readonly MAX_IMAGEN_BYTES = 3 * 1024 * 1024; // 3 MB
  private readonly TIPOS_IMAGEN_PERMITIDOS = ['image/jpeg', 'image/jpg', 'image/png'];


  // ──────────────────────────────────────────
  // RF16: Editar datos del cultivo/lote
  // ──────────────────────────────────────────
  formLote: {
    nombreLote: string;
    areaHa: number;
    ubicacion: string;
    estado: EstadoCultivo;
  } = { nombreLote: '', areaHa: 0, ubicacion: '', estado: 'SEMBRADO' };
  mensajeExitoLote = '';

  formAjusteStock: AjusteStockRequest = { tipo: 'ENTRADA', cantidad: 0, motivo: '' };
  mensajeExitoAjuste = '';
  errorAjuste = '';

  historialMovimientos: MovimientoStock[] = [];
  cargandoHistorial = false;

  calidadOpciones: CalidadLote[] = ['PRIMERA', 'SEGUNDA', 'TERCERA'];
  movimientoOpciones: TipoMovimiento[] = ['ENTRADA', 'SALIDA', 'AJUSTE'];

  // RF-20: Campos para historial de precios y cambio de precio
  formCambioPrecio = { precio: 0, motivo: '' };
  historialPrecios: HistorialPrecio[] = [];
  cargandoPrecioHistorial = false;
  mensajeExitoPrecio = '';
  errorPrecio = '';

  // ── RF18: Gestión de pedidos recibidos ──
  pedidosRecibidos: PedidoResponse[] = [];
  pedidoDetalle: PedidoResponse | null = null;
  motivoRechazo: string = '';

  // ── RF11: Contacto del comprador, visible solo si el pedido está confirmado ──
  contactoComprador: ContactoPedido | null = null;
  cargandoContacto = false;
  errorContacto = '';

  // ── RF-22: Bloque de alertas del resumen (notificaciones de pedidos parciales/nuevos y stock bajo) ──
  notificaciones: Notificacion[] = [];
  private readonly UMBRAL_STOCK_BAJO_PORCENTAJE = 0.15; // 15% del stock original publicado

  constructor(
    private auth: Auth,
    private cultivoService: CultivoService,
    private lotes: LoteService,
    private router: Router,
    private pedidoService: PedidoService,
    private datosContactoService: DatosContactoService,
    private notificacionService: NotificacionService
  ) {}

  ngOnInit() {
    this.usuario = this.auth.getUsuarioActual();
    if (!this.usuario || this.usuario.rol !== 'AGRICULTOR') {
      this.router.navigate(['/']);
      return;
    }
    this.cargarDatos();
  }

  cargarDatos() {
    if (!this.usuario) return;
    this.cultivoService.getCultivosPorAgricultor(this.usuario.id).subscribe({
      next: (data) => { this.misCultivos = data; },
      error: () => { this.misCultivos = []; }
    });
    this.cargarLotes();
    this.cargarPedidosRecibidos();
    this.cargarNotificaciones();
  }

  // ── RF-22: notificaciones para el bloque de alertas del resumen ──
  cargarNotificaciones(): void {
    this.notificacionService.getNotificaciones().subscribe({
      next: (data) => { this.notificaciones = data; },
      error: () => { this.notificaciones = []; }
    });
  }

  // Notificaciones de pedidos nuevos/parciales aún no leídas, para destacarlas en el resumen
  get alertasPedidosParciales(): Notificacion[] {
    return this.notificaciones.filter(n => !n.leido && (n.tipo === 'PEDIDO_PARCIAL' || n.tipo === 'NUEVO_PEDIDO'));
  }

  // Lotes con poco stock restante respecto a lo publicado originalmente (alerta temprana de reabastecimiento)
  get lotesConStockBajo(): LoteComercial[] {
    return this.misLotes.filter(l =>
      l.estado === 'ACTIVO' &&
      l.stockDisponible > 0 &&
      l.cantidadKg > 0 &&
      (l.stockDisponible / l.cantidadKg) <= this.UMBRAL_STOCK_BAJO_PORCENTAJE
    );
  }

  cargarLotes() {
    this.lotes.getTodosLotes().subscribe({
      next: (data) => this.misLotes = data,
      error: () => this.misLotes = []
    });
  }

  irA(seccion: string) {
    this.seccionActiva.set(seccion);
    this.panelActivo = null;
    this.cultivoSeleccionado = null;
  }

  cerrarSesion() { this.auth.logout(); }

  // ──────────────────────────────────────────
  // CULTIVOS — RF02: Registrar cultivo
  // ──────────────────────────────────────────
  guardarCultivo() {
    if (!this.usuario) return;
    const payload: Partial<Cultivo> = {
      ...this.formCultivo,
      agricultorId: this.usuario.id
    };
    this.cultivoService.guardarCultivo(payload).subscribe({
      next: () => {
        this.formCultivo = {
          nombreProducto: '', variedad: '', categoria: '', fechaSiembra: '', estado: 'SEMBRADO',
          nombreLote: '', areaHa: 0, ubicacion: '', descripcion: ''
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
  // SEGUIMIENTO — RF03
  // ──────────────────────────────────────────
  abrirSeguimiento(cultivo: Cultivo) {
    this.cultivoSeleccionado = cultivo;
    this.formSeguimiento = {
      estado: cultivo.estado,
      etapaProductiva: cultivo.etapaProductiva,
      observacion: cultivo.observacionSeguimiento || ''
    };
    this.panelActivo = 'seguimiento';
  }

  guardarSeguimiento() {
    if (!this.cultivoSeleccionado) return;
    this.cultivoService.actualizarSeguimiento(this.cultivoSeleccionado.id, { ...this.formSeguimiento }).subscribe({
      next: (actualizado) => {
        this.mensajeExitoSeguimiento = '¡Seguimiento registrado!';
        setTimeout(() => this.mensajeExitoSeguimiento = '', 4000);
        this.cargarDatos();
        this.cultivoSeleccionado = { ...actualizado };
      },
      error: () => this.mensajeExitoSeguimiento = 'Error al registrar seguimiento.'
    });
  }

  // ──────────────────────────────────────────
  // EVENTOS — RF17
  // ──────────────────────────────────────────
  abrirEventos(cultivo: Cultivo) {
    this.cultivoSeleccionado = cultivo;
    this.eventosCultivo = [];
    this.formEvento = {
      tipo: 'SIEMBRA',
      descripcion: '',
      fecha: new Date().toISOString().split('T')[0]
    };
    this.panelActivo = 'eventos';
    this.cultivoService.getEventosPorCultivo(cultivo.id).subscribe({
      next: (data) => this.eventosCultivo = data,
      error: () => this.eventosCultivo = []
    });
  }

  guardarEvento() {
    if (!this.cultivoSeleccionado) return;
    const req: EventoProduccionRequest = {
      ...this.formEvento,
      cultivoId: this.cultivoSeleccionado.id
    };
    this.cultivoService.agregarEvento(req).subscribe({
      next: (nuevo) => {
        this.eventosCultivo = [nuevo, ...this.eventosCultivo];
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
    this.eventosCultivo = [];
  }

  // ──────────────────────────────────────────
  // KPIs Cultivos
  // ──────────────────────────────────────────
  get totalCultivos()   { return this.misCultivos.length; }
  get cultivosActivos() { return this.misCultivos.filter(c => c.estado === 'SEMBRADO' || c.estado === 'CRECIMIENTO').length; }

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

  // ──────────────────────────────────────────
  // RF04: Selección de imagen del lote (JPG/PNG desde el ordenador)
  // ──────────────────────────────────────────
  onImagenSeleccionada(event: Event): void {
    this.errorImagenLote = '';
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files[0];
    if (!file) return;

    if (!this.TIPOS_IMAGEN_PERMITIDOS.includes(file.type)) {
      this.errorImagenLote = 'Solo se permiten imágenes en formato JPG o PNG.';
      input.value = '';
      return;
    }
    if (file.size > this.MAX_IMAGEN_BYTES) {
      this.errorImagenLote = 'La imagen no debe superar los 3 MB.';
      input.value = '';
      return;
    }

    const lector = new FileReader();
    lector.onload = () => {
      this.formLoteComercial.imagenUrl = lector.result as string;
    };
    lector.onerror = () => {
      this.errorImagenLote = 'No se pudo leer la imagen. Intenta con otro archivo.';
    };
    lector.readAsDataURL(file);
  }

  quitarImagenLote(): void {
    this.formLoteComercial.imagenUrl = '';
    this.errorImagenLote = '';
  }

  // ──────────────────────────────────────────
  // RF04: Publicar lote comercial
  // ──────────────────────────────────────────
  publicarLoteComercial() {
    this.errorLoteComercial = '';
    if (!this.formLoteComercial.cultivoId) {
      this.errorLoteComercial = 'Selecciona un cultivo.';
      return;
    }
    this.lotes.publicarLote(this.formLoteComercial).subscribe({
      next: (lote) => {
        this.mensajeExitoLoteComercial = `✅ Lote publicado exitosamente (ID: ${lote.id})`;
        setTimeout(() => this.mensajeExitoLoteComercial = '', 5000);
        this.formLoteComercial = {
          cultivoId: 0, cantidadKg: 0, calidad: 'PRIMERA',
          precioUnitario: 0, unidadMedida: 'kg',
          fechaEntregaEstimada: '', condicionesEntrega: '', imagenUrl: ''
        };
        this.errorImagenLote = '';
        this.cargarLotes();
        this.irA('lotes');
      },
      error: (err) => {
        this.errorLoteComercial = err.error?.error || 'Error al publicar el lote.';
      }
    });
  }

  // ──────────────────────────────────────────
  // RF09: Ajuste manual de stock
  // ──────────────────────────────────────────
  abrirAjusteStock(lote: LoteComercial) {
    this.loteSeleccionado = lote;
    this.formAjusteStock = { tipo: 'ENTRADA', cantidad: 0, motivo: '' };
    this.mensajeExitoAjuste = '';
    this.errorAjuste = '';
    this.panelLoteActivo = 'ajuste-stock';
  }

  guardarAjusteStock() {
    if (!this.loteSeleccionado) return;
    this.errorAjuste = '';
    this.lotes.ajustarStock(this.loteSeleccionado.id, this.formAjusteStock).subscribe({
      next: (loteActualizado) => {
        this.mensajeExitoAjuste = `✅ Stock actualizado. Nuevo stock: ${loteActualizado.stockDisponible} ${loteActualizado.unidadMedida}`;
        setTimeout(() => {
          this.mensajeExitoAjuste = '';
          this.cerrarPanelLote();
        }, 3000);
        this.cargarLotes();
      },
      error: (err) => {
        this.errorAjuste = err.error?.error || 'Error al ajustar el stock.';
      }
    });
  }

  verHistorialStock(lote: LoteComercial) {
    this.loteSeleccionado = lote;
    this.historialMovimientos = [];
    this.cargandoHistorial = true;
    this.panelLoteActivo = 'historial-stock';
    this.lotes.getMovimientosPorLote(lote.id).subscribe({
      next: (data) => { this.historialMovimientos = data; this.cargandoHistorial = false; },
      error: () => { this.cargandoHistorial = false; }
    });
  }

  abrirCambioPrecio(lote: LoteComercial) {
    this.loteSeleccionado = lote;
    this.formCambioPrecio = { precio: lote.precioUnitario, motivo: '' };
    this.mensajeExitoPrecio = '';
    this.errorPrecio = '';
    this.panelLoteActivo = 'cambiar-precio';
  }

  guardarCambioPrecio() {
    if (!this.loteSeleccionado) return;
    this.errorPrecio = '';
    this.mensajeExitoPrecio = '';
    if (this.formCambioPrecio.precio <= 0) {
      this.errorPrecio = 'El precio debe ser mayor a 0.';
      return;
    }
    if (!this.formCambioPrecio.motivo.trim()) {
      this.errorPrecio = 'El motivo del cambio es obligatorio.';
      return;
    }
    this.lotes.actualizarPrecio(this.loteSeleccionado.id, this.formCambioPrecio.precio, this.formCambioPrecio.motivo).subscribe({
      next: (loteActualizado) => {
        this.mensajeExitoPrecio = `✅ Precio actualizado exitosamente.`;
        setTimeout(() => {
          this.mensajeExitoPrecio = '';
          this.cerrarPanelLote();
        }, 2000);
        this.cargarLotes();
      },
      error: (err) => {
        this.errorPrecio = err.error?.error || 'Error al actualizar el precio.';
      }
    });
  }

  verHistorialPrecio(lote: LoteComercial) {
    this.loteSeleccionado = lote;
    this.historialPrecios = [];
    this.cargandoPrecioHistorial = true;
    this.panelLoteActivo = 'historial-precio';
    this.lotes.getPrecioHistorial(lote.id).subscribe({
      next: (data) => {
        this.historialPrecios = data;
        this.cargandoPrecioHistorial = false;
      },
      error: () => {
        this.cargandoPrecioHistorial = false;
      }
    });
  }

  cerrarPanelLote() {
    this.panelLoteActivo = null;
    this.loteSeleccionado = null;
  }

  // ── KPIs Lotes ──────────────────────────────────────────────
  get totalLotes()       { return this.misLotes.length; }
  get lotesPublicados()  { return this.misLotes.filter(l => l.publicado).length; }
  get lotesAgotados()    { return this.misLotes.filter(l => l.estado === 'AGOTADO').length; }

  estadoLoteComercialClass(estado: string): string {
    const map: Record<string, string> = {
      'ACTIVO':   'badge-entregado',
      'AGOTADO':  'badge-cancelado',
      'INACTIVO': 'badge-pendiente',
    };
    return map[estado] || 'badge-default';
  }

  calidadLabel(calidad: CalidadLote): string {
    const map: Record<CalidadLote, string> = {
      'PRIMERA': '⭐⭐⭐ Primera',
      'SEGUNDA': '⭐⭐ Segunda',
      'TERCERA': '⭐ Tercera',
    };
    return map[calidad] || calidad;
  }

  tipoMovimientoLabel(tipo: TipoMovimiento): string {
    const map: Record<TipoMovimiento, string> = {
      'ENTRADA': '📦 Entrada',
      'SALIDA':  '📤 Salida',
      'AJUSTE':  '🔧 Ajuste',
    };
    return map[tipo] || tipo;
  }

  tipoMovimientoClass(tipo: TipoMovimiento): string {
    const map: Record<TipoMovimiento, string> = {
      'ENTRADA': 'badge-entregado',
      'SALIDA':  'badge-cancelado',
      'AJUSTE':  'badge-pendiente',
    };
    return map[tipo] || '';
  }
  // ──────────────────────────────────────────
  // RF16: Editar lote agrícola
  // ──────────────────────────────────────────
  abrirEditarLote(cultivo: Cultivo) {
    this.cultivoSeleccionado = cultivo;
    this.formLote = {
      nombreLote: cultivo.nombreLote,
      areaHa:     cultivo.areaHa,
      ubicacion:  cultivo.ubicacion,
      estado:     cultivo.estado,
    };
    this.panelActivo = 'editar-lote';
  }

  guardarLote() {
    if (!this.cultivoSeleccionado) return;
    const payload: Partial<Cultivo> = { ...this.formLote };
    this.cultivoService.actualizarCultivo(this.cultivoSeleccionado.id, payload).subscribe({
      next: (actualizado) => {
        this.mensajeExitoLote = '¡Cultivo actualizado correctamente!';
        setTimeout(() => this.mensajeExitoLote = '', 4000);
        this.cultivoSeleccionado = { ...actualizado };
        this.cargarDatos();
      },
      error: () => this.mensajeExitoLote = 'Error al actualizar cultivo.'
    });
  }

  // ── Métodos de Pedidos (RF18) ──
  get pedidosPendientes(): number {
    return this.pedidosRecibidos.filter(p => p.estado === 'PENDIENTE').length;
  }

  cargarPedidosRecibidos(): void {
    if (!this.usuario) return;
    this.pedidoService.listarPorAgricultor(this.usuario.id).subscribe({
      next: (data) => {
        this.pedidosRecibidos = data.sort((a, b) => new Date(b.fechaPedido).getTime() - new Date(a.fechaPedido).getTime());
      },
      error: () => {
        this.pedidosRecibidos = [];
      }
    });
  }

  confirmarPedido(pedidoId: number): void {
    if (!this.usuario) return;
    this.pedidoService.confirmarPedido(pedidoId).subscribe({
      next: () => {
        this.cargarPedidosRecibidos();
        if (this.pedidoDetalle?.id === pedidoId) {
          this.verDetallePedido(pedidoId);
        }
        alert('¡Pedido confirmado exitosamente!');
      },
      error: (err) => alert(err.error?.error || 'Error al confirmar el pedido.')
    });
  }

  rechazarPedido(pedidoId: number): void {
    if (!this.usuario) return;
    if (!this.motivoRechazo.trim()) {
      alert('Por favor ingrese un motivo de rechazo.');
      return;
    }
    this.pedidoService.rechazarPedido(pedidoId, this.motivoRechazo).subscribe({
      next: () => {
        this.motivoRechazo = '';
        this.cargarPedidosRecibidos();
        if (this.pedidoDetalle?.id === pedidoId) {
          this.verDetallePedido(pedidoId);
        }
        alert('Pedido rechazado.');
      },
      error: (err) => alert(err.error?.error || 'Error al rechazar el pedido.')
    });
  }

  avanzarEstado(pedidoId: number, nuevoEstado: string): void {
    this.pedidoService.cambiarEstado(pedidoId, nuevoEstado as any, `Pedido actualizado a ${nuevoEstado.toLowerCase()} por el agricultor.`).subscribe({
      next: () => {
        this.cargarPedidosRecibidos();
        if (this.pedidoDetalle?.id === pedidoId) {
          this.verDetallePedido(pedidoId);
        }
      },
      error: (err) => alert(err.error?.error || 'Error al actualizar el pedido.')
    });
  }

  verDetallePedido(pedidoId: number): void {
    this.pedidoService.obtenerPedido(pedidoId).subscribe({
      next: (data) => {
        this.pedidoDetalle = data;
        this.cargarContactoComprador(data);
      }
    });
  }

  // ── RF11: solo se intenta mostrar el contacto si el pedido ya está
  // confirmado por el propio agricultor (o etapas posteriores); el
  // backend vuelve a validar esta regla de todos modos.
  cargarContactoComprador(pedido: PedidoResponse): void {
    this.contactoComprador = null;
    this.errorContacto = '';

    const estadosAutorizados: EstadoPedido[] = ['CONFIRMADO', 'PREPARADO', 'DESPACHADO', 'ENTREGADO'];
    if (!this.usuario || !estadosAutorizados.includes(pedido.estado)) {
      return;
    }

    this.cargandoContacto = true;
    this.datosContactoService.obtenerContactoPorPedido(pedido.id).subscribe({
      next: (data) => {
        this.contactoComprador = data;
        this.cargandoContacto = false;
      },
      error: (err) => {
        this.errorContacto = err.error?.error || 'No se pudo obtener el contacto del comprador.';
        this.cargandoContacto = false;
      }
    });
  }

  cerrarDetallePedido(): void {
    this.pedidoDetalle = null;
    this.contactoComprador = null;
    this.errorContacto = '';
  }

  getPedidoEstadoLabel(estado: string): string {
    const labels: Record<string, string> = {
      PENDIENTE: 'Pendiente',
      CONFIRMADO: 'Confirmado',
      PREPARADO: 'Preparado',
      DESPACHADO: 'Despachado',
      ENTREGADO: 'Entregado',
      CANCELADO: 'Cancelado',
      RECHAZADO: 'Rechazado'
    };
    return labels[estado] || estado;
  }

  getPedidoEstadoClase(estado: string): string {
    return 'badge-estado-' + estado.toLowerCase();
  }

  // ── RF-19: helpers para pedidos divididos entre varios lotes/agricultores ──
  miParteDelPedido(pedido: PedidoResponse): DetallePedidoResp | undefined {
    if (!this.usuario || !pedido.detalles) return undefined;
    return pedido.detalles.find(d => d.agricultorId === this.usuario!.id) || pedido.detalles[0];
  }

  otrasPartesDelPedido(pedido: PedidoResponse): DetallePedidoResp[] {
    if (!this.usuario || !pedido.detalles) return [];
    return pedido.detalles.filter(d => d.agricultorId !== this.usuario!.id);
  }

  tieneMiPartePendiente(pedido: PedidoResponse): boolean {
    if (!this.usuario || !pedido.detalles) return false;
    return pedido.detalles.some(d => d.agricultorId === this.usuario!.id && d.estadoDetalle === 'PENDIENTE');
  }
}
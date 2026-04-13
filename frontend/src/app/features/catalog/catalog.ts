import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface Vendedor {
  nombre: string;
  tipo: 'Persona Natural' | 'Empresa';
  ruc?: string;
  dni?: string;
  region: string;
  telefono: string;
  descripcion: string;
  miembroDesde: string;
}

interface Producto {
  nombre: string;
  descripcion: string;
  detalleCompleto: string;
  precio: string;
  categoria: string;
  imagen: string;
  disponible: boolean;
  unidad: string;
  stock: string;
  fechaCosecha: string;
  condicionesEntrega: string;
  vendedor: Vendedor;
}

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './catalog.html',
  styleUrl: './catalog.css',
})
export class Catalog {
  categoriaActiva: string = 'Todos';
  productoSeleccionado: Producto | null = null;

  categorias: string[] = ['Todos', 'Cereales', 'Frutas', 'Tubérculos', 'Bebidas', 'Verduras'];

  productos: Producto[] = [
    {
      nombre: 'Trigo Premium',
      descripcion: 'Granos seleccionados de alta calidad, cosechados en los valles andinos.',
      detalleCompleto: 'Trigo de variedad harinera cultivado en los valles del Mantaro a 3,200 msnm. Ideal para panaderías, pastelerías y producción de harina de alta calidad. Libre de pesticidas y certificado por SENASA.',
      precio: 'S/. 25',
      categoria: 'Cereales',
      imagen: 'assets/img/p1.jpg',
      disponible: true,
      unidad: 'por kg',
      stock: '2,400 kg disponibles',
      fechaCosecha: 'Marzo 2026',
      condicionesEntrega: 'Entrega en sacos de 50 kg. Recojo en almacén o despacho a Lima (flete adicional).',
      vendedor: {
        nombre: 'Samuel Orosco Rodríguez',
        tipo: 'Persona Natural',
        dni: '43821056',
        region: 'Junín – Valle del Mantaro',
        telefono: '+51 987 654 321',
        descripcion: 'Agricultor con 15 años de experiencia en cultivo de cereales andinos. Miembro activo de la cooperativa agraria San Jerónimo.',
        miembroDesde: 'Enero 2024'
      }
    },
    {
      nombre: 'Café Orgánico',
      descripcion: 'Cultivado en la sierra con procesos sostenibles. Aroma intenso y sabor balanceado.',
      detalleCompleto: 'Café arábica de altura, cultivado sobre los 1,800 msnm en la región de Villa Rica. Procesado en húmedo con secado al sol. Notas de chocolate, caramelo y frutos rojos. Certificación orgánica vigente.',
      precio: 'S/. 40',
      categoria: 'Bebidas',
      imagen: 'assets/img/p2.jpg',
      disponible: true,
      unidad: 'por 250g',
      stock: '850 unidades disponibles',
      fechaCosecha: 'Febrero 2026',
      condicionesEntrega: 'Empaque al vacío. Envío nacional por courier. Pedido mínimo 5 unidades.',
      vendedor: {
        nombre: 'Agroindustrias Villa Rica S.A.C.',
        tipo: 'Empresa',
        ruc: '20601234567',
        region: 'Pasco – Villa Rica',
        telefono: '+51 063 456 789',
        descripcion: 'Empresa exportadora de café especialidad con más de 200 familias productoras asociadas. Certificados UTZ, Rainforest Alliance y Orgánico USDA.',
        miembroDesde: 'Agosto 2023'
      }
    },
    {
      nombre: 'Maíz Amarillo',
      descripcion: 'Ideal para consumo y producción de harina. Cosecha reciente.',
      detalleCompleto: 'Maíz amarillo duro de variedad PM-212, cultivado en la costa norte peruana. Grano limpio, seco y sin hongos. Excelente para producción de balanceados, chicha y harina de maíz.',
      precio: 'S/. 18',
      categoria: 'Cereales',
      imagen: 'assets/img/p3.jpg',
      disponible: true,
      unidad: 'por kg',
      stock: '5,000 kg disponibles',
      fechaCosecha: 'Abril 2026',
      condicionesEntrega: 'Despacho en sacos de 50 kg. Disponible para recojo en Lambayeque o envío a Lima.',
      vendedor: {
        nombre: 'Carlos Huanca Pérez',
        tipo: 'Persona Natural',
        dni: '17654321',
        region: 'Lambayeque – Ferreñafe',
        telefono: '+51 974 321 654',
        descripcion: 'Agricultor familiar con 8 hectáreas dedicadas al maíz amarillo. Trabaja con técnicas de riego tecnificado desde 2019.',
        miembroDesde: 'Marzo 2024'
      }
    },
    {
      nombre: 'Frutas Tropicales',
      descripcion: 'Mango, papaya y piña frescas directas del productor.',
      detalleCompleto: 'Selección de frutas tropicales frescas: mango Kent, papaya hawaiana y piña Golden. Cosechadas en su punto óptimo de madurez. Embaladas en cajas de cartón ventiladas para preservar frescura.',
      precio: 'S/. 30',
      categoria: 'Frutas',
      imagen: 'assets/img/p4.jpg',
      disponible: true,
      unidad: 'por caja',
      stock: '320 cajas disponibles',
      fechaCosecha: 'Abril 2026',
      condicionesEntrega: 'Cajas de 10 kg. Entrega en 24-48h en Lima. Requiere cadena de frío para envíos largos.',
      vendedor: {
        nombre: 'Frutas del Norte E.I.R.L.',
        tipo: 'Empresa',
        ruc: '20512987654',
        region: 'Piura – Sullana',
        telefono: '+51 073 987 123',
        descripcion: 'Empresa familiar dedicada a la producción y comercialización de frutas tropicales en la región Piura desde hace 12 años.',
        miembroDesde: 'Junio 2023'
      }
    },
    {
      nombre: 'Papa Andina',
      descripcion: 'Variedades nativas con alto valor nutritivo. Cosecha de temporada.',
      detalleCompleto: 'Colección de papas nativas: Huayro, Amarilla, Canchan y Peruanita. Cultivadas a 3,800 msnm en Puno sin uso de agroquímicos. Alta demanda en restaurantes gourmet y ferias gastronómicas.',
      precio: 'S/. 20',
      categoria: 'Tubérculos',
      imagen: 'assets/img/p5.jpg',
      disponible: false,
      unidad: 'por kg',
      stock: 'Sin stock — próxima cosecha en Junio 2026',
      fechaCosecha: 'Junio 2026 (estimado)',
      condicionesEntrega: 'Sacos de 25 kg. Solo recojo en finca por el momento.',
      vendedor: {
        nombre: 'Pedro Quispe Mamani',
        tipo: 'Persona Natural',
        dni: '04512378',
        region: 'Puno – Azángaro',
        telefono: '+51 951 258 369',
        descripcion: 'Guardián de papas nativas con más de 40 variedades registradas. Proveedor de restaurantes top de Lima y Cusco.',
        miembroDesde: 'Octubre 2023'
      }
    },
    {
      nombre: 'Quinua Real',
      descripcion: 'Superalimento con proteínas y minerales esenciales.',
      detalleCompleto: 'Quinua blanca perlada de grano grande, lavada y lista para cocinar. Origen Puno, certificado orgánico. Rica en proteínas completas (16g/100g), hierro y zinc. Apta para dietas veganas, celíacas y deportistas.',
      precio: 'S/. 35',
      categoria: 'Cereales',
      imagen: 'assets/img/p6.jpg',
      disponible: true,
      unidad: 'por kg',
      stock: '1,200 kg disponibles',
      fechaCosecha: 'Enero 2026',
      condicionesEntrega: 'Bolsas de 1 kg o 5 kg. Envío nacional. Pedido mínimo 3 kg.',
      vendedor: {
        nombre: 'Cooperativa Agraria Altiplano',
        tipo: 'Empresa',
        ruc: '20400876543',
        region: 'Puno – Juliaca',
        telefono: '+51 051 321 987',
        descripcion: 'Cooperativa con 380 agricultores asociados dedicada a la producción y exportación de quinua orgánica certificada. Presente en mercados de EE.UU y Europa.',
        miembroDesde: 'Mayo 2023'
      }
    },
    {
      nombre: 'Cebolla Roja',
      descripcion: 'Cebolla fresca de primer uso, ideal para exportación y mercados locales.',
      detalleCompleto: 'Cebolla roja de variedad Americana, cosechada en Arequipa. Bulbos grandes, firmes y con buen color. Seleccionada y clasificada por calibre. Ideal para exportación, supermercados y mercados mayoristas.',
      precio: 'S/. 12',
      categoria: 'Verduras',
      imagen: 'assets/img/p1.jpg',
      disponible: true,
      unidad: 'por kg',
      stock: '8,000 kg disponibles',
      fechaCosecha: 'Marzo 2026',
      condicionesEntrega: 'Mallas de 25 kg. Despacho desde Arequipa. Precio de flete según volumen.',
      vendedor: {
        nombre: 'Luis Mamani Ccallo',
        tipo: 'Persona Natural',
        dni: '29876543',
        region: 'Arequipa – Camaná',
        telefono: '+51 054 765 432',
        descripcion: 'Productor de cebolla con 20 ha cultivadas en el valle de Camaná. Exporta a Chile y Bolivia de forma directa.',
        miembroDesde: 'Febrero 2024'
      }
    },
    {
      nombre: 'Camote Morado',
      descripcion: 'Rico en antioxidantes. Cosechado en Cañete, Lima.',
      detalleCompleto: 'Camote morado variedad Jonathan cultivado en los valles de Cañete. Alto contenido de antocianinas y fibra. Muy demandado por la industria de jugos naturales, helados artesanales y pastelería saludable.',
      precio: 'S/. 15',
      categoria: 'Tubérculos',
      imagen: 'assets/img/p2.jpg',
      disponible: true,
      unidad: 'por kg',
      stock: '3,500 kg disponibles',
      fechaCosecha: 'Febrero 2026',
      condicionesEntrega: 'Cajas de 20 kg o sacos de 50 kg. Despacho a Lima en 12h.',
      vendedor: {
        nombre: 'Rosa Flores Vásquez',
        tipo: 'Persona Natural',
        dni: '21345678',
        region: 'Lima – Cañete',
        telefono: '+51 956 741 852',
        descripcion: 'Agricultora con especialización en tubérculos andinos. Proveedora de cadenas de restaurantes saludables y tiendas orgánicas en Lima.',
        miembroDesde: 'Abril 2024'
      }
    },
    {
      nombre: 'Chirimoya',
      descripcion: 'Fruta de temporada, dulce y cremosa. Producción limitada.',
      detalleCompleto: 'Chirimoya variedad Cumbe, reconocida mundialmente por su sabor excepcional. Cultivada en Calango, Lima a 1,200 msnm. La producción es estacional y limitada. Muy demandada por hoteles 5 estrellas y mercados gourmet.',
      precio: 'S/. 22',
      categoria: 'Frutas',
      imagen: 'assets/img/p3.jpg',
      disponible: false,
      unidad: 'por unidad',
      stock: 'Sin stock — temporada Julio–Septiembre',
      fechaCosecha: 'Julio 2026 (estimado)',
      condicionesEntrega: 'Cajas individuales con protección. Solo pedidos anticipados con reserva.',
      vendedor: {
        nombre: 'Miguel Altamirano Paz',
        tipo: 'Persona Natural',
        dni: '09123456',
        region: 'Lima – Cañete (Calango)',
        telefono: '+51 945 123 789',
        descripcion: 'Productor especializado en chirimoya de exportación. Ganador del Premio Nacional al Productor Agrícola 2023.',
        miembroDesde: 'Noviembre 2023'
      }
    }
  ];

  get productosFiltrados(): Producto[] {
    if (this.categoriaActiva === 'Todos') return this.productos;
    return this.productos.filter(p => p.categoria === this.categoriaActiva);
  }

  setCategoria(cat: string): void {
    this.categoriaActiva = cat;
  }

  abrirModal(producto: Producto): void {
    this.productoSeleccionado = producto;
    document.body.style.overflow = 'hidden';
  }

  cerrarModal(): void {
    this.productoSeleccionado = null;
    document.body.style.overflow = '';
  }
}
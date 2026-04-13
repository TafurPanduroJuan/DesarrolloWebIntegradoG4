import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';


@Component({
  selector: 'app-home',
  standalone:true,
  imports: [CommonModule, RouterLink],
  templateUrl:'./home.html',
  styleUrls:['./home.css']
})
export class Home {
 datos = [
  {
    frase: "¡Emprende con Nosotros!",
    descripcion: "Impulsamos la comunidad agrícola con tecnología que conecta productores y compradores en un solo lugar.",
    imagen: "assets/img/imagen1.jpg",
    boton: { texto: "Se Parte de AgroLink", ruta: "/form", estilo: "btn-success" }
  },
  {
    frase: "¡Productos frescos directamente del campo!",
    descripcion: "AgroLink trabaja con agricultores peruanos de primera. En nuestro catálogo encontrarás cereales, tubérculos, frutas y bebidas cultivadas con prácticas responsables y sostenibles.",
    imagen: "assets/img/imagen2.jpg",
    boton: { texto: "Ver Catálogo", ruta: "/catalog", estilo: "btn-light text-success" }
  },
  {
    frase: "¡AgroLink es la transformación!",
    descripcion: "Empresa 100% peruana liderando el mercado productor. Nuestra plataforma digital conecta directamente a productores agrícolas con mayoristas, distribuidores y consumidores.",
    imagen: "assets/img/imagen3.jpg",
    boton: { texto: "Conoce más de AgroLink", ruta: "/about", estilo: "btn-primary" }
  }
];
  testimonios = [
  { estrellas: 5, texto: 'AgroLink cambio mi manera de pensar y entender que la tecnologia es muy valiosa si la sabes aprovechar.', autor: 'Samuel Orosco Rodriguez', rol: 'Productor de Trigo' },
  { estrellas: 4, texto: 'Gracias a AgroLink encontré la manera de vender mis productos facilmente.', autor: 'María López Huaman', rol: 'Productora de Frutas' },
  { estrellas: 5, texto: 'La plataforma me da la tranquilidad de que mis ventas sean buenas. 100% Recomendado', autor: 'José Ramírez Alvarado', rol: 'Agricultor de Café' },
  { estrellas: 5, texto: 'Como cliente de AgroLink me siento satisfecho. Gracias a los productos de primera calidad', autor: 'Miguel Altamirano Paz', rol: 'EL Buen Sabor' }
];

productos = [
  {
    nombre: 'Trigo Premium',
    descripcion: 'Granos seleccionados de alta calidad.',
    precio: 'S/. 25',
    categoria: 'Cereales',
    imagen: 'assets/img/p1.jpg'
  
  },
  {
    nombre: 'Café Orgánico',
    descripcion: 'Cultivado en la sierra con procesos sostenibles.',
    precio: 'S/. 40',
    categoria: 'Bebidas',
    imagen: 'assets/img/p2.jpg'
   
  },
  {
    nombre: 'Maíz Amarillo',
    descripcion: 'Ideal para consumo y producción de harina.',
    precio: 'S/. 18',
    categoria: 'Cereales',
    imagen: 'assets/img/p3.jpg'
    
  },
  {
    nombre: 'Frutas Tropicales',
    descripcion: 'Mango, papaya y piña frescas.',
    precio: 'S/. 30',
    categoria: 'Frutas',
    imagen: 'assets/img/p4.jpg'
   
  },
  {
    nombre: 'Papa Andina',
    descripcion: 'Variedades nativas con alto valor nutritivo.',
    precio: 'S/. 20',
    categoria: 'Tubérculos',
    imagen: 'assets/img/p5.jpg'
    
  },
  {
    nombre: 'Quinua Real',
    descripcion: 'Superalimento con proteínas y minerales.',
    precio: 'S/. 35',
    categoria: 'Cereales',
    imagen: 'assets/img/p6.jpg'
    
  }
];
}

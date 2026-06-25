import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './contact.html',
  styleUrls: ['./contact.css']
})
export class Contact {

  form = {
    nombre: '',
    correo: '',
    telefono: '',
    tipo: '',
    mensaje: ''
  };

  enviado = false;

  enviar() {
    // TODO: conectar al backend cuando esté disponible el endpoint de solicitudes
    this.enviado = true;

    this.form = {
      nombre: '',
      correo: '',
      telefono: '',
      tipo: '',
      mensaje: ''
    };

    setTimeout(() => this.enviado = false, 5000);
  }
}
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Auth } from '../auth/services/auth';

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

  constructor(private auth: Auth) {}

  enviar() {
    this.auth.enviarSolicitud({
      nombre: this.form.nombre,
      correo: this.form.correo,
      telefono: this.form.telefono,
      mensaje: `[${this.form.tipo}] ${this.form.mensaje}`
    });

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
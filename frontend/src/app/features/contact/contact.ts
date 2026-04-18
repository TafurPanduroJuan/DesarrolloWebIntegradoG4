import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common'; // 👈 FALTA ESTO

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [FormsModule, CommonModule], // 👈 AÑÁDELO AQUÍ
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
    console.log(this.form);

    this.enviado = true;

    this.form = {
      nombre: '',
      correo: '',
      telefono: '',
      tipo: '',
      mensaje: ''
    };
  }
}
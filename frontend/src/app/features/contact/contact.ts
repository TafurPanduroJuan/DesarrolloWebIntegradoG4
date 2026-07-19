import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ContactService } from './contact.service';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './contact.html',
  styleUrls: ['./contact.css']
})
export class Contact {

  constructor(private contactService: ContactService) {}

  form = {
    nombre: '',
    correo: '',
    telefono: '',
    tipo: '',
    mensaje: ''
  };

  enviado = false;
  enviando = false;
  error = '';

  enviar() {
    if (this.enviando) return;
    this.error = '';
    this.enviando = true;

    this.contactService.enviar(this.form).subscribe({
      next: () => {
        this.enviando = false;
        this.enviado = true;
        this.form = { nombre: '', correo: '', telefono: '', tipo: '', mensaje: '' };
        setTimeout(() => this.enviado = false, 5000);
      },
      error: (err) => {
        this.enviando = false;
        this.error = err.error?.error || 'No se pudo enviar tu mensaje. Intenta nuevamente en unos minutos.';
      }
    });
  }
}
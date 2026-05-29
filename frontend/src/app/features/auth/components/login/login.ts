import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  error = '';
  cargando = false;
  mostrarPassword = false;

  form!: any;

  constructor(
    private fb: FormBuilder,
    private auth: Auth,
    private router: Router,
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  get emailInvalido() {
    return this.form.get('email')?.invalid && this.form.get('email')?.touched;
  }

  get passwordInvalido() {
    return this.form.get('password')?.invalid && this.form.get('password')?.touched;
  }

  onSubmit() {
  if (this.form.invalid) {
    this.form.markAllAsTouched();
    return;
  }
  this.cargando = true;
  this.error = '';

  const { email, password } = this.form.value;
  const resultado = this.auth.login(email!, password!);

  if (resultado.ok) {
    // === OBTENER EL USUARIO LOGUEADO Y VERIFICAR SU ESTADO ===
    const usuario = this.auth.getUsuarioActual();
    
    if (usuario?.status === 'pending') {
      this.error = 'Tu cuenta está pendiente de aprobación. Porfavor, espere respuesta del administrador.';
      this.cargando = false;
      return;
    }

    if (usuario?.status === 'inactive') {
      this.error = 'Tu cuenta ha sido rechazada. Contacta al administrador.';
      this.cargando = false;
      return;
    }
    this.router.navigate(['/intranet']);
  } else {
    this.error = resultado.mensaje;
    this.cargando = false;
  }
}
}
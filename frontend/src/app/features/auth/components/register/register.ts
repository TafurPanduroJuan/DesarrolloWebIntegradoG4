import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../../services/auth';

function passwordsCoinciden(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmar = control.get('confirmar')?.value;
  return password === confirmar ? null : { noCoinciden: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  error = '';
  exito = '';
  cargando = false;
  mostrarPassword = false;

  form!: any;

  constructor(
    private fb: FormBuilder,
    private auth: Auth,
    private router: Router,
  ) {

    this.form = this.fb.group(
    {
      nombre: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmar: ['', Validators.required],
      terminos: [false, Validators.requiredTrue],
    },
    { validators: passwordsCoinciden },
  );

  }

  get nombreInvalido() {
    return this.form.get('nombre')?.invalid && this.form.get('nombre')?.touched;
  }
  get emailInvalido() {
    return this.form.get('email')?.invalid && this.form.get('email')?.touched;
  }
  get passwordInvalido() {
    return this.form.get('password')?.invalid && this.form.get('password')?.touched;
  }
  get confirmarInvalido() {
    return (
      (this.form.get('confirmar')?.touched && this.form.errors?.['noCoinciden']) ||
      (this.form.get('confirmar')?.invalid && this.form.get('confirmar')?.touched)
    );
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.cargando = true;
    this.error = '';

    const { nombre, email, password } = this.form.value;
    const resultado = this.auth.register(nombre!, email!, password!);

    if (resultado.ok) {
      this.exito = resultado.mensaje;
      setTimeout(() => this.router.navigate(['/login']), 1800);
    } else {
      this.error = resultado.mensaje;
      this.cargando = false;
    }
  }
}
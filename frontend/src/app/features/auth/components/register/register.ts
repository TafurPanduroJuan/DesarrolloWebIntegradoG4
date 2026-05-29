import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Auth } from '../../services/auth';

function passwordsCoinciden(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmar = control.get('confirmar')?.value;
  return password === confirmar ? null : { noCoinciden: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  error = '';
  exito = '';
  cargando = false;
  mostrarPassword = false;
  tipoCuenta: 'comprador' | 'agricultor' = 'comprador';

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
        // Campos agrícolas
        location: [''],
        mainProducts: [''],
        // Campos compradores 
        buyerType: [''],
        ruc: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
        contactAddress: ['', [Validators.required]],
      },
      { validators: passwordsCoinciden },
    );
  }

  onTipoCuentaChange(tipo: 'comprador' | 'agricultor'): void {
    this.tipoCuenta = tipo;
    
    if (tipo === 'agricultor') {
      this.form.patchValue({ buyerType: '', contactAddress: '' });
      this.form.get('location')?.setValidators([Validators.required]);
      this.form.get('mainProducts')?.setValidators([Validators.required]);
      this.form.get('buyerType')?.clearValidators();
      this.form.get('ruc')?.clearValidators();
      this.form.get('contactAddress')?.clearValidators();
    } else {
      this.form.patchValue({ location: '', mainProducts: '' });
      this.form.get('location')?.clearValidators();
      this.form.get('mainProducts')?.clearValidators();
      this.form.get('buyerType')?.setValidators([Validators.required]);
      this.form.get('ruc')?.setValidators([Validators.required]);
      this.form.get('contactAddress')?.setValidators([Validators.required]);
    }
    
    ['location', 'mainProducts', 'buyerType', 'ruc', 'contactAddress'].forEach(field => {
      this.form.get(field)?.updateValueAndValidity();
    });
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
  get locationInvalido() {
    return this.tipoCuenta === 'agricultor' &&
           this.form.get('location')?.invalid &&
           this.form.get('location')?.touched;
  }
  get mainProductsInvalido() {
    return this.tipoCuenta === 'agricultor' &&
           this.form.get('mainProducts')?.invalid &&
           this.form.get('mainProducts')?.touched;
  }
  get buyerTypeInvalido() {
    return this.tipoCuenta === 'comprador' &&
           this.form.get('buyerType')?.invalid &&
           this.form.get('buyerType')?.touched;
  }
  get contactAddressInvalido() {
    return this.tipoCuenta === 'comprador' &&
           this.form.get('contactAddress')?.invalid &&
           this.form.get('contactAddress')?.touched;
  }
  get rucInvalido() {
    return this.tipoCuenta === 'comprador' &&
          this.form.get('ruc')?.invalid &&
          this.form.get('ruc')?.touched;
  }

  onSubmit() {
    if (this.tipoCuenta === 'agricultor') {
      this.form.get('location')?.markAsTouched();
      this.form.get('mainProducts')?.markAsTouched();
    } else {
      this.form.get('buyerType')?.markAsTouched();
      this.form.get('ruc')?.markAsTouched();
      this.form.get('contactAddress')?.markAsTouched();
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.cargando = true;
    this.error = '';

    const { nombre, email, password, location, mainProducts, buyerType, ruc, contactAddress } = this.form.value;

    const extras = this.tipoCuenta === 'agricultor'
      ? {
          status: 'pending' as const,
          location: location?.trim(),
          mainProducts: mainProducts?.split(',').map((p: string) => p.trim()).filter(Boolean),
        }
      : {
          status: 'active' as const,
          buyerType: buyerType,
          ruc: ruc?.trim(),
          contactAddress: contactAddress?.trim(),
        };

    // Registrar usuario
    const resultado = this.auth.registerWithRole(nombre!, email!, password!, this.tipoCuenta, extras);

    if (resultado.ok) {
      // === SI ES AGRICULTOR, ENVIAR SOLICITUD COMO EN CONTACT.TS ===
      if (this.tipoCuenta === 'agricultor') {
        const telefono = 'No registrado';
        const ubicacion = location || 'No registrada';
        const productos = Array.isArray(mainProducts) ? mainProducts.join(', ') : 'No registrados';

        this.auth.enviarSolicitud({
          nombre: nombre!,
          correo: email!,
          telefono: telefono,
          mensaje: `[Agricultor] Ubicación: ${ubicacion}. Productos: ${productos}.`
        });
      }

      this.exito = resultado.mensaje;
      setTimeout(() => this.router.navigate(['/login']), 1800);
    } else {
      this.error = resultado.mensaje;
      this.cargando = false;
    }
  }
}
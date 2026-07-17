import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { Auth, UsuarioSesion } from '../../../features/auth/services/auth';
import { RouterLink, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { NotificacionService, Notificacion } from '../../../features/intranet/notificacion.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
})
export class Navbar implements OnInit, OnDestroy {
  notificaciones: Notificacion[] = [];
  unreadCount = 0;
  showDropdown = false;
  private intervalId: any;
  private navSub?: Subscription;

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event) {
    this.showDropdown = false;
  }

  get usuarioActual(): UsuarioSesion | null {
    return this.auth.getUsuarioActual();
  }

  constructor(
    private auth: Auth,
    private notifService: NotificacionService,
    private router: Router
  ) {}

  ngOnInit() {
    if (this.usuarioActual) {
      this.cargarNotificaciones();
      
      // Carga inmediata al cambiar de ruta
      this.navSub = this.router.events.pipe(
        filter(event => event instanceof NavigationEnd)
      ).subscribe(() => {
        if (this.usuarioActual) {
          this.cargarNotificaciones();
        }
      });

      // Polling cada 15 segundos
      this.intervalId = setInterval(() => {
        if (this.usuarioActual) {
          this.cargarNotificaciones();
        }
      }, 15000);
    }
  }

  ngOnDestroy() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
    if (this.navSub) {
      this.navSub.unsubscribe();
    }
  }

  cargarNotificaciones() {
    this.notifService.getNotificaciones().subscribe({
      next: (data) => {
        this.notificaciones = data.slice(0, 5); // Mostrar últimas 5 en el navbar
      },
      error: () => {}
    });

    this.notifService.getNoLeidasCount().subscribe({
      next: (data) => {
        this.unreadCount = data.count;
      },
      error: () => {}
    });
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.showDropdown = !this.showDropdown;
  }

  cerrarDropdown() {
    this.showDropdown = false;
  }

  marcarLeida(n: Notificacion) {
    if (!n.leido) {
      this.notifService.marcarLeida(n.id).subscribe({
        next: () => {
          this.cargarNotificaciones();
        }
      });
    }
    this.showDropdown = false;
    if (n.idReferencia) {
      if (this.usuarioActual?.rol === 'AGRICULTOR') {
        this.router.navigate(['/intranet/agricultor']);
      } else if (this.usuarioActual?.rol === 'COMPRADOR') {
        this.router.navigate(['/intranet/comprador']);
      } else if (this.usuarioActual?.rol === 'ADMINISTRADOR') {
        this.router.navigate(['/intranet/admin']);
      }
    }
  }

  marcarTodasLeidas() {
    this.notifService.marcarTodasLeidas().subscribe({
      next: () => {
        this.cargarNotificaciones();
      }
    });
  }

  cerrarSesion() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
    this.auth.logout();
  }
}
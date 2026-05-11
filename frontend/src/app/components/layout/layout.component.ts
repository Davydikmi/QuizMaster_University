import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService, CurrentUser } from '../../services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatButtonModule,
    MatDividerModule,
    MatCardModule
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit {
  user: CurrentUser | null = null;

  constructor(private router: Router, private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.user$.subscribe((user) => {
      this.user = user;
    });

    if (this.authService.hasToken()) {
      this.authService.loadCurrentUser().subscribe({
        error: () => this.logout()
      });
    }
  }

  get isTeacherAreaVisible(): boolean {
    return this.user?.role === 'ADMIN' || this.user?.role === 'TEACHER';
  }

  get isAdmin(): boolean {
    return this.user?.role === 'ADMIN';
  }

  get isTeacher(): boolean {
    return this.user?.role === 'TEACHER';
  }

  logout(): void {
    this.authService.logout();
    this.user = null;
    this.router.navigate(['/auth/login']);
  }
}

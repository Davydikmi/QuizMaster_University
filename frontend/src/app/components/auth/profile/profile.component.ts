import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  loading = false;
  roleLabel = '';
  groupLabel = '—';
  form;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['']
    });
  }

  ngOnInit(): void {
    this.authService.loadCurrentUser().subscribe({
      next: (user) => {
        this.roleLabel = user.role;
        this.groupLabel = user.groupName ?? '—';
        this.form.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email
        });
        this.cdr.detectChanges();
      }
    });
  }

  save(): void {
    if (this.form.invalid || this.loading) {
      return;
    }

    this.loading = true;
    this.authService
      .updateProfile({
        firstName: String(this.form.value.firstName),
        lastName: String(this.form.value.lastName),
        email: String(this.form.value.email),
        password: this.form.value.password ? String(this.form.value.password) : undefined
      })
      .subscribe({
        next: () => {
          this.loading = false;
          this.snackBar.open('Профиль обновлен.', 'ОК', { duration: 2500 });
          this.form.patchValue({ password: '' });
          this.cdr.detectChanges();
        },
        error: () => {
          this.loading = false;
          this.snackBar.open('Не удалось обновить профиль.', 'ОК', { duration: 3000 });
          this.cdr.detectChanges();
        }
      });
  }
}

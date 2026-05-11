import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  loading = false;
  form;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  submit(): void {
    if (this.form.invalid || this.loading) {
      return;
    }

    this.loading = true;
    this.authService.login(this.form.getRawValue() as { email: string; password: string }).subscribe({
      next: () => {
        this.authService.loadCurrentUser().subscribe({
          next: (user) => {
            this.loading = false;
            this.cdr.detectChanges();
            const target = user.role === 'STUDENT' ? '/quizzes' : '/teacher/courses';
            this.router.navigate([target]);
          },
          error: () => {
            this.loading = false;
            this.cdr.detectChanges();
            this.router.navigate(['/quizzes']);
          }
        });
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
        this.snackBar.open('Не удалось войти. Проверьте email и пароль.', 'ОК', { duration: 3000 });
      }
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService, RegisterRequest, StudyGroup, UserRole } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit {
  loading = false;
  readonly roles: UserRole[] = ['STUDENT', 'TEACHER', 'ADMIN'];
  groups: StudyGroup[] = [];
  form;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
    this.form = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['STUDENT', [Validators.required]],
      groupId: [null as number | null]
    });

    this.form.get('role')?.valueChanges.subscribe((role) => {
      const groupControl = this.form.get('groupId');
      if ((role as UserRole | null) === 'STUDENT') {
        groupControl?.setValidators([Validators.required]);
      } else {
        groupControl?.clearValidators();
        groupControl?.setValue(null);
      }
      groupControl?.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    this.authService.getStudyGroups().subscribe({
      next: (groups) => (this.groups = groups)
    });
  }

  submit(): void {
    if (this.form.invalid || this.loading) {
      return;
    }

    this.loading = true;
    this.authService.register(this.form.getRawValue() as RegisterRequest).subscribe({
      next: () => {
        this.loading = false;
        this.snackBar.open('Регистрация прошла успешно.', 'ОК', { duration: 2500 });
        this.router.navigate(['/quizzes']);
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Ошибка регистрации. Проверьте данные.', 'ОК', { duration: 3000 });
      }
    });
  }
}

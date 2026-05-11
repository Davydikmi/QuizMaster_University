import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AdminStudyGroup, AdminUser } from '../../../services/admin.service';

@Component({
  selector: 'app-student-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  template: `
    <h2 mat-dialog-title>Студент</h2>
    <mat-dialog-content [formGroup]="form">
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Имя</mat-label>
        <input matInput formControlName="firstName" />
      </mat-form-field>
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Фамилия</mat-label>
        <input matInput formControlName="lastName" />
      </mat-form-field>
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Email</mat-label>
        <input matInput formControlName="email" />
      </mat-form-field>
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Группа</mat-label>
        <mat-select formControlName="groupId">
          <mat-option *ngFor="let group of data.groups" [value]="group.id">
            {{ group.name }} ({{ group.courseNumber }} курс)
          </mat-option>
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Новый пароль (опционально)</mat-label>
        <input matInput type="password" formControlName="password" />
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button color="warn" (click)="remove()">Удалить</button>
      <button mat-button (click)="dialogRef.close()">Отмена</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid" (click)="save()">Сохранить</button>
    </mat-dialog-actions>
  `,
  styles: [`.dialog-field{width:100%;margin-bottom:.75rem;}`]
})
export class StudentDialogComponent {
  form;

  constructor(
    public dialogRef: MatDialogRef<StudentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { student: AdminUser; groups: AdminStudyGroup[] },
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      firstName: [this.data.student.firstName, [Validators.required]],
      lastName: [this.data.student.lastName, [Validators.required]],
      email: [this.data.student.email, [Validators.required, Validators.email]],
      groupId: [this.data.student.groupId ?? null, [Validators.required]],
      password: ['']
    });
  }

  save(): void {
    const payload: AdminUser & { password?: string } = {
      ...this.data.student,
      firstName: String(this.form.value.firstName),
      lastName: String(this.form.value.lastName),
      email: String(this.form.value.email),
      groupId: Number(this.form.value.groupId),
      groupName: this.data.groups.find((g) => g.id === Number(this.form.value.groupId))?.name ?? null,
      password: this.form.value.password ? String(this.form.value.password) : ''
    };
    this.dialogRef.close({ student: payload, delete: false });
  }

  remove(): void {
    this.dialogRef.close({ student: this.data.student, delete: true });
  }
}

import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Course } from '../../../services/teacher.service';

@Component({
  selector: 'app-course-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>{{ data.course ? 'Редактировать курс' : 'Новый курс' }}</h2>
    <mat-dialog-content [formGroup]="form">
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Название курса</mat-label>
        <input matInput formControlName="name" />
      </mat-form-field>

      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Описание</mat-label>
        <textarea matInput formControlName="description" rows="3"></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Отмена</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid" (click)="save()">
        {{ data.course ? 'Сохранить' : 'Создать' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `.dialog-field { width: 100%; margin-bottom: 1rem; }`
  ]
})
export class CourseDialogComponent implements OnInit {
  form!: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<CourseDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { course?: Course },
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: [this.data?.course?.name || '', Validators.required],
      description: [this.data?.course?.description || '']
    });
  }

  save(): void {
    if (this.form.invalid) {
      return;
    }

    const course: Course = {
      id: this.data?.course?.id ?? 0,
      ...this.form.value
    };
    this.dialogRef.close(course);
  }
}

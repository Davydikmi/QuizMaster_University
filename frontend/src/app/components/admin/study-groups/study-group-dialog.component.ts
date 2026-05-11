import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AdminStudyGroup } from '../../../services/admin.service';

@Component({
  selector: 'app-study-group-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>{{ data.group ? 'Редактировать группу' : 'Новая группа' }}</h2>
    <mat-dialog-content [formGroup]="form">
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Название</mat-label>
        <input matInput formControlName="name" />
      </mat-form-field>
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Курс</mat-label>
        <input matInput type="number" min="1" max="6" formControlName="courseNumber" />
      </mat-form-field>
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Специальность</mat-label>
        <input matInput formControlName="speciality" />
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Отмена</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid" (click)="dialogRef.close(form.value)">
        {{ data.group ? 'Сохранить' : 'Создать' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`.dialog-field{width:100%;margin-bottom:0.75rem;}`]
})
export class StudyGroupDialogComponent {
  form;

  constructor(
    public dialogRef: MatDialogRef<StudyGroupDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { group?: AdminStudyGroup },
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      name: [this.data.group?.name ?? '', [Validators.required]],
      courseNumber: [this.data.group?.courseNumber ?? 1, [Validators.required, Validators.min(1), Validators.max(6)]],
      speciality: [this.data.group?.speciality ?? '']
    });
  }
}

import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { TeacherQuestion } from '../../../services/teacher.service';

@Component({
  selector: 'app-question-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatRadioModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.question ? 'Редактировать вопрос' : 'Добавить вопрос' }}</h2>
    <mat-dialog-content [formGroup]="form">
      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Текст вопроса</mat-label>
        <textarea matInput formControlName="text" rows="3"></textarea>
      </mat-form-field>

      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Тип вопроса</mat-label>
        <mat-select formControlName="type" (selectionChange)="onTypeChange($event.value)">
          <mat-option value="SINGLE_CHOICE">Один правильный</mat-option>
          <mat-option value="MULTIPLE_CHOICE">Несколько правильных</mat-option>
          <mat-option value="TRUE_FALSE">Верно/Неверно</mat-option>
        </mat-select>
      </mat-form-field>

      <div formArrayName="answers" class="answers-list">
        <div class="answer-row" *ngFor="let control of answers.controls; let i = index" [formGroupName]="i">
          <mat-form-field appearance="fill" class="answer-field">
            <mat-label>Ответ {{ i + 1 }}</mat-label>
            <input matInput formControlName="text" />
          </mat-form-field>

          <ng-container [ngSwitch]="form.value.type">
            <mat-checkbox *ngSwitchCase="'MULTIPLE_CHOICE'" formControlName="isCorrect" [disableRipple]="true">Правильный</mat-checkbox>

            <mat-radio-group *ngSwitchDefault [value]="selectedCorrectIndex.value" (change)="markCorrect($event.value)">
              <mat-radio-button [value]="i" [disableRipple]="true">Правильный</mat-radio-button>
            </mat-radio-group>
          </ng-container>

          <button
            mat-button
            type="button"
            color="warn"
            (click)="removeOption(i)"
            [disabled]="form.value.type === 'TRUE_FALSE' || answers.length <= 2"
          >
            Удалить
          </button>
        </div>
      </div>

      <button mat-stroked-button type="button" color="primary" (click)="addOption()" [disabled]="form.value.type === 'TRUE_FALSE'">
        Добавить вариант ответа
      </button>

      <mat-form-field appearance="fill" class="dialog-field">
        <mat-label>Баллы</mat-label>
        <input matInput type="number" min="1" formControlName="points" />
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Отмена</button>
      <button mat-flat-button color="primary" [disabled]="form.invalid" (click)="save()">
        {{ data.question ? 'Сохранить' : 'Добавить' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `.dialog-field { width: 100%; margin-bottom: 1rem; }`,
    `.answers-list { display: grid; gap: 0.5rem; margin-bottom: 1rem; }`,
    `.answer-row { display: grid; grid-template-columns: 1fr auto auto; align-items: center; gap: 0.5rem; }`,
    `.answer-field { width: 100%; margin: 0; }`
  ]
})
export class QuestionDialogComponent implements OnInit {
  form!: FormGroup;
  selectedCorrectIndex = new FormControl<number>(0, { nonNullable: true });

  constructor(
    public dialogRef: MatDialogRef<QuestionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { quizId: number; nextOrderNum: number; question?: TeacherQuestion },
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    const current = this.data.question;
    const type = current?.type ?? 'SINGLE_CHOICE';

    this.form = this.fb.group({
      text: [current?.text ?? '', Validators.required],
      type: [type, Validators.required],
      answers: this.fb.array([]),
      points: [current?.points ?? 1, [Validators.required, Validators.min(1)]]
    });

    if (current?.options?.length) {
      current.options.forEach((option) => this.answers.push(this.createAnswerControl(option.text, option.isCorrect)));
    } else if (type === 'TRUE_FALSE') {
      this.answers.push(this.createAnswerControl('Верно'));
      this.answers.push(this.createAnswerControl('Неверно'));
    } else {
      this.answers.push(this.createAnswerControl());
      this.answers.push(this.createAnswerControl());
    }

    if (type === 'MULTIPLE_CHOICE') {
      const hasCorrect = this.answers.controls.some((control) => Boolean(control.value.isCorrect));
      if (!hasCorrect) {
        this.answers.at(0).get('isCorrect')?.setValue(true);
      }
    } else {
      const existingCorrect = this.answers.controls.findIndex((control) => Boolean(control.value.isCorrect));
      this.markCorrect(existingCorrect >= 0 ? existingCorrect : 0);
    }
  }

  get answers(): FormArray<FormGroup> {
    return this.form.get('answers') as FormArray<FormGroup>;
  }

  private createAnswerControl(text = '', isCorrect = false): FormGroup {
    return this.fb.group({
      text: [text, Validators.required],
      isCorrect: [isCorrect]
    });
  }

  addOption(): void {
    this.answers.push(this.createAnswerControl());
  }

  removeOption(index: number): void {
    if (this.answers.length <= 2) {
      return;
    }
    this.answers.removeAt(index);
    if (this.selectedCorrectIndex.value >= this.answers.length) {
      this.markCorrect(0);
    }
  }

  onTypeChange(type: string): void {
    if (type === 'TRUE_FALSE') {
      while (this.answers.length > 0) {
        this.answers.removeAt(0);
      }
      this.answers.push(this.createAnswerControl('Верно'));
      this.answers.push(this.createAnswerControl('Неверно'));
      this.markCorrect(0);
      return;
    }

    if (this.answers.length < 2) {
      this.answers.push(this.createAnswerControl());
      this.answers.push(this.createAnswerControl());
    }

    if (type === 'SINGLE_CHOICE') {
      this.markCorrect(this.selectedCorrectIndex.value ?? 0);
    } else {
      const hasCorrect = this.answers.controls.some((control) => Boolean(control.value.isCorrect));
      if (!hasCorrect) {
        this.answers.at(0).get('isCorrect')?.setValue(true);
      }
    }
  }

  markCorrect(index: number): void {
    this.selectedCorrectIndex.setValue(index);
    this.answers.controls.forEach((control, i) => {
      control.get('isCorrect')?.setValue(i === index);
    });
  }

  save(): void {
    if (this.form.invalid || this.answers.length < 2) {
      return;
    }

    const options = this.answers.controls.map((control) => ({
      text: String(control.value.text).trim(),
      isCorrect: Boolean(control.value.isCorrect)
    }));

    if (options.some((option) => !option.text)) {
      return;
    }

    if (this.form.value.type === 'SINGLE_CHOICE' || this.form.value.type === 'TRUE_FALSE') {
      const correctCount = options.filter((option) => option.isCorrect).length;
      if (correctCount !== 1) {
        return;
      }
    }

    if (this.form.value.type === 'MULTIPLE_CHOICE') {
      const hasCorrect = options.some((option) => option.isCorrect);
      if (!hasCorrect) {
        return;
      }
    }

    const question: TeacherQuestion = {
      id: this.data.question?.id ?? 0,
      quizId: this.data.quizId,
      text: this.form.value.text,
      type: this.form.value.type,
      points: Number(this.form.value.points),
      orderNum: this.data.question?.orderNum ?? this.data.nextOrderNum,
      options
    };

    this.dialogRef.close(question);
  }
}

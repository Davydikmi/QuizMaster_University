import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TeacherService, TeacherQuiz, TeacherQuestion } from '../../../services/teacher.service';
import { QuestionDialogComponent } from './question-dialog.component';

@Component({
  selector: 'app-add-questions',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule
  ],
  templateUrl: './add-questions.component.html',
  styleUrls: ['./add-questions.component.scss']
})
export class AddQuestionsComponent implements OnInit {
  quizForm!: FormGroup;
  quizzes: TeacherQuiz[] = [];
  questions: TeacherQuestion[] = [];
  selectedQuiz?: TeacherQuiz;

  constructor(
    private teacherService: TeacherService,
    private fb: FormBuilder,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.quizForm = this.fb.group({
      quizId: [null, Validators.required]
    });

    this.teacherService.getQuizzes().subscribe((quizzes) => (this.quizzes = quizzes));
    this.quizForm.get('quizId')?.valueChanges.subscribe((id) => this.loadQuestions(id));
  }

  loadQuestions(quizId: number): void {
    this.selectedQuiz = this.quizzes.find((quiz) => quiz.id === quizId);
    this.teacherService.getQuestionsForQuiz(quizId).subscribe((questions) => (this.questions = questions));
  }

  openQuestionDialog(): void {
    if (!this.selectedQuiz) {
      return;
    }
    const ref = this.dialog.open(QuestionDialogComponent, {
      width: '560px',
      data: { quizId: this.selectedQuiz.id, nextOrderNum: this.questions.length + 1 }
    });

    ref.afterClosed().subscribe((result: TeacherQuestion | undefined) => {
      if (!result) {
        return;
      }
      this.teacherService.addQuestion(result).subscribe(() => this.loadQuestions(this.selectedQuiz!.id));
    });
  }

  editQuestion(question: TeacherQuestion): void {
    if (!this.selectedQuiz) {
      return;
    }

    const ref = this.dialog.open(QuestionDialogComponent, {
      width: '640px',
      data: { quizId: this.selectedQuiz.id, nextOrderNum: question.orderNum, question }
    });

    ref.afterClosed().subscribe((result: TeacherQuestion | undefined) => {
      if (!result) {
        return;
      }
      this.teacherService.updateQuestion(result).subscribe(() => this.loadQuestions(this.selectedQuiz!.id));
    });
  }

  removeQuestion(question: TeacherQuestion): void {
    if (!this.selectedQuiz) {
      return;
    }
    this.teacherService.deleteQuestion(this.selectedQuiz.id, question.id).subscribe(() => this.loadQuestions(this.selectedQuiz!.id));
  }
}

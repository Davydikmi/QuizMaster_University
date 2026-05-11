import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { TimerComponent } from '../timer/timer.component';
import { Quiz, QuizService, Question } from '../../../services/quiz.service';

@Component({
  selector: 'app-quiz-take',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatRadioModule,
    MatProgressBarModule,
    MatDividerModule,
    TimerComponent
  ],
  templateUrl: './quiz-take.component.html',
  styleUrls: ['./quiz-take.component.scss']
})
export class QuizTakeComponent implements OnInit {
  quizId = 0;
  questions: Question[] = [];
  form!: FormGroup;
  submitted = false;
  score = 0;
  total = 0;
  quizTitle = '';
  courseName = '';
  isExpired = false;
  attemptId: number | null = null;
  saving = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.quizId = Number(this.route.snapshot.paramMap.get('id')) || 0;
    if (!this.quizId) {
      this.router.navigate(['/quizzes']);
      return;
    }

    this.quizService
      .startAttempt(this.quizId)
      .subscribe({
        next: (attempt) => {
          this.attemptId = attempt.attemptId;
          this.quizTitle = attempt.quizTitle;
          this.loadQuiz();
        },
        error: () => this.router.navigate(['/quizzes'])
      });
  }

  private loadQuiz(): void {
    this.quizService.getQuiz(this.quizId).subscribe({
      next: (quiz: Quiz) => {
        this.courseName = quiz.courseName;
      }
    });

    this.quizService.getQuestions(this.quizId).subscribe({
      next: (questions) => {
        this.questions = questions;
        this.buildForm(questions);
      },
      error: () => this.router.navigate(['/quizzes'])
    });
  }

  buildForm(questions: Question[]): void {
    const groups = questions.map((question) =>
      this.fb.group({
        questionId: [question.id],
        selectedAnswerId: [null]
      })
    );

    this.form = this.fb.group({
      answers: this.fb.array(groups)
    });
    this.total = questions.length || 0;
  }

  get answerControls(): FormArray {
    return this.form.get('answers') as FormArray;
  }

  get progress(): number {
    const answered = this.answerControls.controls.filter((control) => control.value.selectedAnswerId).length;
    return this.total ? Math.round((answered / this.total) * 100) : 0;
  }

  async submit(): Promise<void> {
    if (!this.questions.length || this.submitted || !this.attemptId || this.saving) {
      return;
    }
    this.saving = true;

    const answerRequests = this.answerControls.controls
      .map((control) => ({
        questionId: Number(control.value.questionId),
        selectedOptionId: Number(control.value.selectedAnswerId)
      }))
      .filter((item) => !!item.selectedOptionId);

    try {
      for (const answer of answerRequests) {
        await firstValueFrom(this.quizService.saveAnswer(this.attemptId, answer.questionId, [answer.selectedOptionId]));
      }
      const result = await firstValueFrom(this.quizService.finishAttempt(this.attemptId));
      this.submitted = true;
      this.saving = false;
      this.router.navigate(['/results', this.quizId], { state: { result } });
    } catch {
      this.saving = false;
      this.router.navigate(['/quizzes']);
    }
  }

  onTimerExpired(): void {
    this.isExpired = true;
    this.submit();
  }
}

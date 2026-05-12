import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
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
    MatCheckboxModule,
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
  quizDescription = '';
  courseName = '';
  teacherName = '';
  durationSeconds = 0;
  availableFrom: string | null = null;
  dueDate: string | null = null;
  questionCount = 0;
  isExpired = false;
  attemptId: number | null = null;
  saving = false;
  isContinuing = false;
  loadError = '';
  questionsLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.quizId = Number(this.route.snapshot.paramMap.get('id')) || 0;
    if (!this.quizId) {
      this.router.navigate(['/quizzes']);
      return;
    }

    const retake = history.state?.retake === true;

    this.quizService
      .startAttempt(this.quizId, retake)
      .subscribe({
        next: (attempt) => {
          if (attempt.status === 'COMPLETED' || attempt.status === 'TIMEOUT') {
            this.router.navigate(['/results', this.quizId]);
            return;
          }
          this.attemptId = attempt.attemptId;
          this.quizTitle = attempt.quizTitle;
          this.quizDescription = attempt.description;
          this.teacherName = attempt.teacherName;
          this.courseName = attempt.courseName;
          this.durationSeconds = attempt.remainingSeconds ?? (((attempt.timeLimitMinutes ?? 15) || 15) * 60);
          this.questionCount = attempt.questionCount ?? 0;
          this.availableFrom = attempt.availableFrom ? new Date(attempt.availableFrom).toLocaleString() : null;
          this.dueDate = attempt.dueDate ? new Date(attempt.dueDate).toLocaleString() : null;
          this.isContinuing = attempt.status === 'IN_PROGRESS';
          if (attempt.questions?.length) {
            this.questions = attempt.questions;
            this.buildForm(attempt.questions);
            this.questionsLoading = false;
            this.cdr.detectChanges();
          } else {
            this.loadQuiz();
          }
        },
        error: (error) => {
          const message = error.error?.message || '';
          if (message.includes('Maximum number of attempts reached')) {
            this.questionsLoading = false;
            this.loadError = 'Количество попыток для этого теста исчерпано.';
            return;
          }
          this.questionsLoading = false;
          this.loadError = message || 'Не удалось начать тест. Попробуйте позже.';
          this.cdr.detectChanges();
          console.error('Error starting attempt:', error);
        }
      });
  }

  private loadQuiz(): void {
    this.questionsLoading = true;

    this.quizService.getQuiz(this.quizId).subscribe({
      next: (quiz: Quiz) => {
        this.courseName = quiz.courseName;
        this.cdr.detectChanges();
      }
    });

    this.quizService.getQuestions(this.quizId).subscribe({
      next: (questions) => {
        this.questions = questions;
        this.buildForm(questions);
        this.questionsLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.questionsLoading = false;
        this.loadError = 'Не удалось загрузить вопросы теста.';
        this.cdr.detectChanges();
        console.error('Error loading questions:', error);
      }
    });
  }

  buildForm(questions: Question[]): void {
    const groups = questions.map((question) =>
      this.fb.group({
        questionId: [question.id],
        selectedAnswerId: [null],
        selectedOptionIds: [[] as number[]]
      })
    );

    this.form = this.fb.group({
      answers: this.fb.array(groups)
    });
    this.total = questions.length || 0;
  }

  get answerControls(): FormArray {
    return (this.form?.get('answers') as FormArray) ?? this.fb.array([]);
  }

  get progress(): number {
    const answered = this.answerControls.controls.filter((control) => {
      const selectedOptionIds = (control.value.selectedOptionIds as number[]) ?? [];
      return Boolean(control.value.selectedAnswerId) || selectedOptionIds.length > 0;
    }).length;
    return this.total ? Math.round((answered / this.total) * 100) : 0;
  }

  toggleOption(questionIndex: number, optionId: number, checked: boolean): void {
    const control = this.answerControls.at(questionIndex);
    const current = ((control.value.selectedOptionIds as number[]) ?? []).filter((id) => id !== optionId);
    control.patchValue({
      selectedOptionIds: checked ? [...current, optionId] : current
    });
    this.cdr.detectChanges();
  }

  async submit(): Promise<void> {
    if (!this.questions.length || this.submitted || !this.attemptId || this.saving) {
      return;
    }
    this.saving = true;

    const answerRequests = this.answerControls.controls
      .map((control) => {
        const selectedOptionIds = (control.value.selectedOptionIds as number[]) ?? [];
        return {
          questionId: Number(control.value.questionId),
          selectedOptionIds: selectedOptionIds.length ? selectedOptionIds : [Number(control.value.selectedAnswerId)].filter(Boolean)
        };
      })
      .filter((item) => item.selectedOptionIds.length > 0);

    try {
      for (const answer of answerRequests) {
        await firstValueFrom(this.quizService.saveAnswer(this.attemptId, answer.questionId, answer.selectedOptionIds));
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

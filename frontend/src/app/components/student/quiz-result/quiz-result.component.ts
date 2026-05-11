import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { QuizService } from '../../../services/quiz.service';

interface QuizResult {
  attemptId: number;
  quizId: number;
  quizTitle: string;
  score: number;
  maxScore: number;
  finishedAt: string;
  startedAt: string;
  status: string;
}

@Component({
  selector: 'app-quiz-result',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatDividerModule],
  templateUrl: './quiz-result.component.html',
  styleUrls: ['./quiz-result.component.scss']
})
export class QuizResultComponent implements OnInit {
  result: QuizResult | null = history.state.result ?? null;
  loading = false;
  error = '';

  private fallbackResult: QuizResult = {
    attemptId: 0,
    quizId: 0,
    quizTitle: 'Тестирование завершено',
    score: 0,
    maxScore: 0,
    finishedAt: new Date().toISOString(),
    startedAt: new Date().toISOString(),
    status: 'COMPLETED'
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private cdr: ChangeDetectorRef
  ) {}

  get passed(): boolean {
    return !!this.result?.maxScore && (this.result.score / this.result.maxScore) >= 0.6;
  }

  ngOnInit(): void {
    if (this.result) {
      return;
    }

    const quizId = Number(this.route.snapshot.paramMap.get('id')) || 0;
    if (!quizId) {
      this.result = this.fallbackResult;
      return;
    }

    this.loading = true;
    this.quizService.getMyResults().subscribe({
      next: (results) => {
        const completed = results
          .filter((result) => result.quizId === quizId && result.finishedAt)
          .sort((a, b) => new Date(b.finishedAt).getTime() - new Date(a.finishedAt).getTime())[0];
        this.result = completed ?? { ...this.fallbackResult, quizId };
        this.error = completed ? '' : 'Завершенный результат для этого теста не найден.';
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.result = { ...this.fallbackResult, quizId };
        this.error = 'Не удалось загрузить результат теста.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  goToQuizzes(): void {
    this.router.navigate(['/quizzes']);
  }

  retakeQuiz(): void {
    if (!this.result?.quizId) {
      return;
    }
    this.quizService.getQuiz(this.result.quizId).subscribe({
      next: (quiz) => {
        if ((quiz.attemptsRemaining ?? 1) <= 0) {
          window.alert('Количество попыток для этого теста исчерпано.');
          return;
        }
        this.router.navigate(['/quizzes', this.result!.quizId], { state: { retake: true } });
      },
      error: () => {
        window.alert('Не удалось проверить количество доступных попыток.');
      }
    });
  }
}

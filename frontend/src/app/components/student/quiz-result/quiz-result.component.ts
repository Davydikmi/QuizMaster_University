import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

interface QuizResult {
  attemptId: number;
  quizId: number;
  quizTitle: string;
  score: number;
  maxScore: number;
  finishedAt: string;
  status: string;
}

@Component({
  selector: 'app-quiz-result',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatDividerModule],
  templateUrl: './quiz-result.component.html',
  styleUrls: ['./quiz-result.component.scss']
})
export class QuizResultComponent {
  result: QuizResult = history.state.result ?? {
    attemptId: 0,
    quizId: 0,
    quizTitle: 'Тестирование завершено',
    score: 0,
    maxScore: 0,
    finishedAt: new Date().toISOString(),
    status: 'COMPLETED'
  };

  constructor(private router: Router) {}

  goToQuizzes(): void {
    this.router.navigate(['/quizzes']);
  }
}

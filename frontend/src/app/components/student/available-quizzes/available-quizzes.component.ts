import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService, UserRole } from '../../../services/auth.service';
import { QuizService, Quiz } from '../../../services/quiz.service';

@Component({
  selector: 'app-available-quizzes',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressBarModule],
  templateUrl: './available-quizzes.component.html',
  styleUrls: ['./available-quizzes.component.scss']
})
export class AvailableQuizzesComponent implements OnInit {
  quizzes: Quiz[] = [];
  loading = true;
  error = '';
  role: UserRole | null = null;

  constructor(private quizService: QuizService, private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.role = this.authService.getRole();
    this.loadQuizzes();
  }

  loadQuizzes(): void {
    this.loading = true;
    this.error = '';
    this.quizService.getQuizzes().subscribe({
      next: (items) => {
        this.quizzes = items;
        this.loading = false;
      },
      error: () => {
        this.error = 'Не удалось загрузить тесты. Попробуйте обновить страницу.';
        this.loading = false;
      }
    });
  }

  openQuiz(id: number): void {
    if (this.role !== 'STUDENT') {
      return;
    }
    this.router.navigate(['/quizzes', id]);
  }
}

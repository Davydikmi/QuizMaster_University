import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService, UserRole } from '../../../services/auth.service';
import { QuizService, Quiz } from '../../../services/quiz.service';
import { ConfirmDialogComponent } from '../../teacher/dialogs/confirm-dialog.component';

@Component({
  selector: 'app-available-quizzes',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatProgressBarModule, MatDialogModule],
  templateUrl: './available-quizzes.component.html',
  styleUrls: ['./available-quizzes.component.scss']
})
export class AvailableQuizzesComponent implements OnInit {
  quizzes: Quiz[] = [];
  loading = true;
  error = '';
  role: UserRole | null = null;

  constructor(
    private quizService: QuizService,
    private authService: AuthService,
    private router: Router,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}

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
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Не удалось загрузить тесты. Попробуйте обновить страницу.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  openQuiz(id: number): void {
    if (this.role !== 'STUDENT') {
      return;
    }
    const quiz = this.quizzes.find((item) => item.id === id);
    this.router.navigate([quiz?.hasCompletedAttempt ? '/results' : '/quizzes', id]);
  }

  deleteQuiz(quiz: Quiz): void {
    if (this.role !== 'ADMIN') {
      return;
    }

    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Удалить тест?',
        message: `Тест "${quiz.title}" будет удален вместе с вопросами и попытками студентов.`
      }
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }
      this.quizService.deleteQuiz(quiz.id).subscribe(() => {
        this.loadQuizzes();
        this.cdr.detectChanges();
      });
    });
  }
}

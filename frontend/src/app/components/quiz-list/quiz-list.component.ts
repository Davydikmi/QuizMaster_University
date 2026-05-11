import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { QuizService, Quiz } from '../../services/quiz.service';

@Component({
  selector: 'app-quiz-list',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatListModule],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>Список тестов</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <mat-list>
          <mat-list-item *ngFor="let quiz of quizzes">
            <h3 matListItemTitle>{{ quiz.title }}</h3>
            <p matListItemLine>{{ quiz.description }}</p>
            <mat-action-list>
              <button mat-button (click)="viewQuiz(quiz.id)">Открыть</button>
            </mat-action-list>
          </mat-list-item>
        </mat-list>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    mat-card {
      margin: 20px;
    }
  `]
})
export class QuizListComponent implements OnInit {

  quizzes: Quiz[] = [];

  constructor(private quizService: QuizService) { }

  ngOnInit(): void {
    this.loadQuizzes();
  }

  loadQuizzes(): void {
    this.quizService.getQuizzes().subscribe({
      next: (data) => this.quizzes = data,
      error: (error) => console.error('Ошибка загрузки тестов', error)
    });
  }

  viewQuiz(id: number): void {
    // Placeholder for navigation to the test.
    console.log('Открыть тест', id);
  }
}
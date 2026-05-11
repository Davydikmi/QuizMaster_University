import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TeacherService, QuizResultRow } from '../../../services/teacher.service';
import { ConfirmDialogComponent } from '../dialogs/confirm-dialog.component';

@Component({
  selector: 'app-results-table',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatTableModule, MatButtonModule, MatChipsModule, MatDialogModule],
  templateUrl: './results-table.component.html',
  styleUrls: ['./results-table.component.scss']
})
export class ResultsTableComponent implements OnInit {
  displayedColumns = ['student', 'quizTitle', 'course', 'score', 'status', 'submittedAt', 'actions'];
  results: QuizResultRow[] = [];
  private sortState: {
    field: 'student' | 'quizTitle' | 'score' | 'status' | 'course' | 'submittedAt';
    direction: 'asc' | 'desc';
  } = { field: 'student', direction: 'asc' };

  constructor(private teacherService: TeacherService, private dialog: MatDialog, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadResults();
  }

  loadResults(): void {
    this.teacherService.getResults().subscribe((results) => {
      this.results = results;
      this.applySort();
      this.cdr.detectChanges();
    });
  }

  sortBy(field: 'student' | 'quizTitle' | 'score' | 'status' | 'course' | 'submittedAt'): void {
    if (this.sortState.field === field) {
      this.sortState.direction = this.sortState.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortState = { field, direction: 'asc' };
    }
    this.applySort();
    this.cdr.detectChanges();
  }

  sortLabel(field: 'student' | 'quizTitle' | 'score' | 'status' | 'course' | 'submittedAt'): string {
    if (this.sortState.field !== field) {
      return '↕';
    }
    if (field === 'score') {
      return this.sortState.direction === 'asc' ? 'меньше' : 'больше';
    }
    if (field === 'submittedAt') {
      return this.sortState.direction === 'asc' ? 'старые' : 'новые';
    }
    return this.sortState.direction === 'asc' ? 'А-Я' : 'Я-А';
  }

  private applySort(): void {
    const direction = this.sortState.direction === 'asc' ? 1 : -1;
    this.results = [...this.results].sort((a, b) => {
      if (this.sortState.field === 'score') {
        return (a.score - b.score) * direction;
      }
      if (this.sortState.field === 'status') {
        return (a.passed === b.passed ? 0 : a.passed ? 1 : -1) * direction;
      }
      if (this.sortState.field === 'submittedAt') {
        return (new Date(a.submittedAt).getTime() - new Date(b.submittedAt).getTime()) * direction;
      }
      return String(a[this.sortState.field]).localeCompare(String(b[this.sortState.field]), 'ru') * direction;
    });
  }

  deleteAttempt(row: QuizResultRow): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: {
        title: 'Удалить попытку?',
        message: `Попытка студента "${row.student}" по тесту "${row.quizTitle}" будет удалена.`
      }
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }
      this.teacherService.deleteAttempt(row.id).subscribe(() => this.loadResults());
    });
  }
}

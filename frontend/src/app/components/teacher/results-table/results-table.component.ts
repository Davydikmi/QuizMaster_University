import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { TeacherService, QuizResultRow } from '../../../services/teacher.service';

@Component({
  selector: 'app-results-table',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatTableModule, MatButtonModule, MatChipsModule],
  templateUrl: './results-table.component.html',
  styleUrls: ['./results-table.component.scss']
})
export class ResultsTableComponent implements OnInit {
  displayedColumns = ['student', 'quizTitle', 'course', 'score', 'status', 'submittedAt'];
  results: QuizResultRow[] = [];

  constructor(private teacherService: TeacherService) {}

  ngOnInit(): void {
    this.teacherService.getResults().subscribe((results) => (this.results = results));
  }
}

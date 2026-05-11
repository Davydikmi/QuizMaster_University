import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TeacherService, TeacherQuiz, Course, StudyGroup } from '../../../services/teacher.service';

@Component({
  selector: 'app-create-quiz',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './create-quiz.component.html',
  styleUrls: ['./create-quiz.component.scss']
})
export class CreateQuizComponent implements OnInit {
  quizForm!: FormGroup;
  courses: Course[] = [];
  groups: StudyGroup[] = [];

  constructor(
    private fb: FormBuilder,
    private teacherService: TeacherService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quizForm = this.fb.group({
      title: ['', [Validators.required]],
      courseId: [null, [Validators.required]],
      description: ['', [Validators.required]],
      timeLimitMinutes: [20, [Validators.required, Validators.min(5)]],
      maxAttempts: [1, [Validators.required, Validators.min(1)]],
      groupIds: [[], [Validators.required]],
      availableFrom: [new Date().toISOString().slice(0, 16), [Validators.required]],
      dueDate: [new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16), [Validators.required]]
    });

    this.teacherService.getCourses().subscribe((courses) => (this.courses = courses));
    this.teacherService.getStudyGroups().subscribe((groups) => (this.groups = groups));
  }

  submit(): void {
    if (this.quizForm.invalid) {
      return;
    }

    const quiz: TeacherQuiz = {
      id: 0,
      ...this.quizForm.value,
      createdAt: new Date().toISOString()
    };

    this.teacherService.createQuiz(quiz).subscribe((createdQuiz) => {
      const groupIds = this.quizForm.value.groupIds as number[];
      const availableFrom = new Date(this.quizForm.value.availableFrom).toISOString();
      const dueDate = new Date(this.quizForm.value.dueDate).toISOString();

      const assignmentRequests = groupIds.map((groupId) =>
        this.teacherService.assignQuizToGroup(createdQuiz.id, groupId, {
          availableFrom,
          dueDate
        })
      );

      forkJoin(assignmentRequests).subscribe({
        next: () => {
          this.snackBar.open('Тест создан и назначен выбранным группам', 'ОК', { duration: 2500 });
          this.router.navigate(['/teacher/add-questions']);
        },
        error: () => {
          this.snackBar.open('Тест создан, но назначения группам не выполнены', 'ОК', { duration: 3500 });
          this.router.navigate(['/teacher/add-questions']);
        }
      });
    });
  }
}

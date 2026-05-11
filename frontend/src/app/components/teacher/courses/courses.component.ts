import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TeacherService, Course } from '../../../services/teacher.service';
import { CourseDialogComponent } from '../dialogs/course-dialog.component';
import { ConfirmDialogComponent } from '../dialogs/confirm-dialog.component';

@Component({
  selector: 'app-teacher-courses',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatTableModule,
    MatDialogModule,
    MatTooltipModule,
    ReactiveFormsModule
  ],
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.scss']
})
export class CoursesComponent implements OnInit {
  displayedColumns = ['name', 'description', 'teacherEmail', 'actions'];
  courses: Course[] = [];

  constructor(private teacherService: TeacherService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.teacherService.getCourses().subscribe((courses) => (this.courses = courses));
  }

  openCourseDialog(course?: Course): void {
    const ref = this.dialog.open(CourseDialogComponent, {
      width: '520px',
      data: course ? { course } : {}
    });

    ref.afterClosed().subscribe((result: Course | undefined) => {
      if (!result) {
        return;
      }
      if (course) {
        this.teacherService.updateCourse(result).subscribe(() => this.loadCourses());
      } else {
        this.teacherService.addCourse(result).subscribe(() => this.loadCourses());
      }
    });
  }

  removeCourse(course: Course): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: { title: 'Удалить курс', message: `Вы уверены, что хотите удалить курс «${course.name}»?` }
    });

    ref.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.teacherService.deleteCourse(course.id).subscribe(() => this.loadCourses());
      }
    });
  }
}

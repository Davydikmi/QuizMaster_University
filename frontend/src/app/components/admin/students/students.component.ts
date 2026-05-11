import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { AdminService, AdminStudyGroup, AdminUser } from '../../../services/admin.service';
import { StudentDialogComponent } from './student-dialog.component';

@Component({
  selector: 'app-admin-students',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatDialogModule, MatTableModule],
  templateUrl: './students.component.html',
  styleUrl: './students.component.scss'
})
export class StudentsComponent implements OnInit {
  displayedColumns = ['name', 'email', 'group', 'actions'];
  students: AdminUser[] = [];
  groups: AdminStudyGroup[] = [];

  constructor(private adminService: AdminService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.adminService.getGroups().subscribe((groups) => (this.groups = groups));
    this.adminService.getStudents().subscribe((students) => (this.students = students));
  }

  openStudentDialog(student: AdminUser): void {
    const ref = this.dialog.open(StudentDialogComponent, {
      width: '560px',
      data: { student, groups: this.groups }
    });

    ref.afterClosed().subscribe((result?: { student: AdminUser; delete: boolean }) => {
      if (!result) {
        return;
      }
      if (result.delete) {
        this.adminService.deleteStudent(result.student.id).subscribe(() => this.loadAll());
        return;
      }
      this.adminService.updateStudent(result.student).subscribe(() => this.loadAll());
    });
  }
}

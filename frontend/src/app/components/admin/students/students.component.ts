import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
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
  private sortState: { field: 'name' | 'group'; direction: 'asc' | 'desc' } = { field: 'name', direction: 'asc' };

  constructor(private adminService: AdminService, private dialog: MatDialog, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.adminService.getGroups().subscribe((groups) => {
      this.groups = groups;
      this.cdr.detectChanges();
    });
    this.adminService.getStudents().subscribe((students) => {
      this.students = students;
      this.applySort();
      this.cdr.detectChanges();
    });
  }

  sortBy(field: 'name' | 'group'): void {
    if (this.sortState.field === field) {
      this.sortState.direction = this.sortState.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortState = { field, direction: 'asc' };
    }
    this.applySort();
    this.cdr.detectChanges();
  }

  sortLabel(field: 'name' | 'group'): string {
    if (this.sortState.field !== field) {
      return '↕';
    }
    return this.sortState.direction === 'asc' ? 'А-Я' : 'Я-А';
  }

  private applySort(): void {
    const direction = this.sortState.direction === 'asc' ? 1 : -1;
    this.students = [...this.students].sort((a, b) => {
      const left = this.sortState.field === 'name' ? `${a.lastName} ${a.firstName}` : a.groupName || '';
      const right = this.sortState.field === 'name' ? `${b.lastName} ${b.firstName}` : b.groupName || '';
      return left.localeCompare(right, 'ru') * direction;
    });
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

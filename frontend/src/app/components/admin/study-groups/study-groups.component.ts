import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { AdminService, AdminStudyGroup } from '../../../services/admin.service';
import { StudyGroupDialogComponent } from './study-group-dialog.component';
import { ConfirmDialogComponent } from '../../teacher/dialogs/confirm-dialog.component';

@Component({
  selector: 'app-study-groups',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatCardModule, MatDialogModule, MatIconModule, MatTableModule],
  templateUrl: './study-groups.component.html',
  styleUrl: './study-groups.component.scss'
})
export class StudyGroupsComponent implements OnInit {
  displayedColumns = ['name', 'courseNumber', 'speciality', 'actions'];
  groups: AdminStudyGroup[] = [];

  constructor(private adminService: AdminService, private dialog: MatDialog, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadGroups();
  }

  loadGroups(): void {
    this.adminService.getGroups().subscribe((groups) => {
      this.groups = groups;
      this.cdr.detectChanges();
    });
  }

  openDialog(group?: AdminStudyGroup): void {
    const ref = this.dialog.open(StudyGroupDialogComponent, {
      width: '520px',
      data: group ? { group } : {}
    });

    ref.afterClosed().subscribe((result?: Omit<AdminStudyGroup, 'id'>) => {
      if (!result) {
        return;
      }
      if (group) {
        this.adminService.updateGroup(group.id, result).subscribe(() => this.loadGroups());
      } else {
        this.adminService.createGroup(result).subscribe(() => this.loadGroups());
      }
    });
  }

  remove(group: AdminStudyGroup): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '440px',
      data: {
        title: 'Удалить учебную группу?',
        message: `Группа "${group.name}" будет удалена вместе со студентами этой группы, их попытками и назначениями тестов.`
      }
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }
      this.adminService.deleteGroup(group.id).subscribe(() => this.loadGroups());
    });
  }
}

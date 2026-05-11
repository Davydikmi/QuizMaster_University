import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface AdminStudyGroup {
  id: number;
  name: string;
  courseNumber: number;
  speciality?: string;
}

export interface AdminUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'ADMIN' | 'TEACHER' | 'STUDENT';
  enabled: boolean;
  groupId?: number | null;
  groupName?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getGroups(): Observable<AdminStudyGroup[]> {
    return this.http.get<AdminStudyGroup[]>(`${this.apiUrl}/study-groups`);
  }

  createGroup(payload: Omit<AdminStudyGroup, 'id'>): Observable<AdminStudyGroup> {
    return this.http.post<AdminStudyGroup>(`${this.apiUrl}/study-groups`, payload);
  }

  updateGroup(id: number, payload: Omit<AdminStudyGroup, 'id'>): Observable<AdminStudyGroup> {
    return this.http.put<AdminStudyGroup>(`${this.apiUrl}/study-groups/${id}`, payload);
  }

  deleteGroup(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/study-groups/${id}`);
  }

  getStudents(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(`${this.apiUrl}/users`).pipe(
      map((users) => users.filter((user) => user.role === 'STUDENT'))
    );
  }

  updateStudent(student: AdminUser & { password?: string }): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.apiUrl}/users/${student.id}`, {
      ...student,
      password: student.password ?? ''
    });
  }

  deleteStudent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`);
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map, of, switchMap } from 'rxjs';

export interface Course {
  id: number;
  name: string;
  description: string;
  teacherId: number;
  teacherEmail: string;
  createdAt: string;
}

export interface TeacherQuiz {
  id: number;
  title: string;
  courseId: number;
  courseName?: string;
  description: string;
  timeLimitMinutes: number;
  maxAttempts: number;
  createdAt: string;
}

export interface TeacherQuestion {
  id: number;
  quizId: number;
  text: string;
  type: string;
  points: number;
  orderNum: number;
  options: QuestionOption[];
}

export interface QuestionOption {
  id?: number;
  text: string;
  isCorrect: boolean;
}

export interface StudyGroup {
  id: number;
  name: string;
  courseNumber: number;
  speciality?: string;
}

export interface QuizResultRow {
  id: number;
  student: string;
  quizTitle: string;
  course: string;
  score: number;
  maxScore: number;
  passed: boolean;
  submittedAt: string;
}

interface PagedResponse<T> {
  content: T[];
}

interface CourseRequest {
  name: string;
  description: string;
}

interface QuizRequest {
  title: string;
  description: string;
  courseId: number;
  timeLimitMinutes: number;
  maxAttempts: number;
}

interface QuestionRequest {
  text: string;
  type: string;
  points: number;
  orderNum: number;
  options: QuestionOption[];
}

interface QuizResultsResponse {
  results: {
    attemptId: number;
    studentName: string;
    score: number;
    maxScore: number;
    finishedAt: string;
  }[];
}

interface QuizAssignmentRequest {
  availableFrom: string;
  dueDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class TeacherService {
  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getCourses(): Observable<Course[]> {
    return this.http.get<PagedResponse<Course>>(`${this.apiUrl}/courses`).pipe(map((response) => response.content ?? []));
  }

  addCourse(course: Course): Observable<Course> {
    const payload: CourseRequest = {
      name: course.name,
      description: course.description
    };
    return this.http.post<Course>(`${this.apiUrl}/courses`, payload);
  }

  updateCourse(updated: Course): Observable<Course> {
    const payload: CourseRequest = {
      name: updated.name,
      description: updated.description
    };
    return this.http.put<Course>(`${this.apiUrl}/courses/${updated.id}`, payload);
  }

  deleteCourse(courseId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/courses/${courseId}`);
  }

  getQuizzes(): Observable<TeacherQuiz[]> {
    return this.http.get<PagedResponse<TeacherQuiz>>(`${this.apiUrl}/quizzes`).pipe(map((response) => response.content ?? []));
  }

  createQuiz(quiz: TeacherQuiz): Observable<TeacherQuiz> {
    const payload: QuizRequest = {
      title: quiz.title,
      description: quiz.description,
      courseId: quiz.courseId,
      timeLimitMinutes: quiz.timeLimitMinutes,
      maxAttempts: quiz.maxAttempts
    };
    return this.http.post<TeacherQuiz>(`${this.apiUrl}/quizzes`, payload);
  }

  getQuizById(id: number): Observable<TeacherQuiz | undefined> {
    return this.http.get<TeacherQuiz>(`${this.apiUrl}/quizzes/${id}`);
  }

  getQuestionsForQuiz(quizId: number): Observable<TeacherQuestion[]> {
    return this.http.get<TeacherQuestion[]>(`${this.apiUrl}/quizzes/${quizId}/questions`);
  }

  addQuestion(question: TeacherQuestion): Observable<TeacherQuestion> {
    const payload: QuestionRequest = {
      text: question.text,
      type: question.type,
      points: question.points,
      orderNum: question.orderNum,
      options: question.options
    };
    return this.http.post<TeacherQuestion>(`${this.apiUrl}/quizzes/${question.quizId}/questions`, payload);
  }

  updateQuestion(question: TeacherQuestion): Observable<TeacherQuestion> {
    const payload: QuestionRequest = {
      text: question.text,
      type: question.type,
      points: question.points,
      orderNum: question.orderNum,
      options: question.options
    };
    return this.http.put<TeacherQuestion>(`${this.apiUrl}/quizzes/${question.quizId}/questions/${question.id}`, payload);
  }

  deleteQuestion(quizId: number, questionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/quizzes/${quizId}/questions/${questionId}`);
  }

  getResults(): Observable<QuizResultRow[]> {
    return this.getQuizzes().pipe(
      switchMap((quizzes) => {
        if (quizzes.length === 0) {
          return of([]);
        }

        return forkJoin(
          quizzes.map((quiz) =>
            this.http.get<QuizResultsResponse>(`${this.apiUrl}/results/quiz/${quiz.id}`).pipe(
              map((response) =>
                (response.results ?? []).map((row) => ({
                  id: row.attemptId,
                  student: row.studentName,
                  quizTitle: quiz.title,
                  course: quiz.courseName ?? `Курс #${quiz.courseId}`,
                  score: Number(row.score),
                  maxScore: Number(row.maxScore),
                  passed: Number(row.score) >= Number(row.maxScore) * 0.6,
                  submittedAt: row.finishedAt
                }))
              )
            )
          )
        ).pipe(map((groups) => groups.flat()));
      })
    );
  }

  getStudyGroups(): Observable<StudyGroup[]> {
    return this.http.get<StudyGroup[]>(`${this.apiUrl}/study-groups`);
  }

  assignQuizToGroup(quizId: number, groupId: number, payload: QuizAssignmentRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/quizzes/${quizId}/assign/${groupId}`, payload);
  }

  deleteAttempt(attemptId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/attempts/${attemptId}`);
  }
}

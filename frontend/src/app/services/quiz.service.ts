import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface Quiz {
  id: number;
  title: string;
  description: string;
  creatorId: number;
  creatorEmail: string;
  creatorName?: string;
  courseId: number;
  courseName: string;
  hasInProgressAttempt?: boolean;
  hasCompletedAttempt?: boolean;
  questionCount?: number;
  availableFrom?: string;
  dueDate?: string;
  attemptsRemaining?: number;
  timeLimitMinutes: number;
  maxAttempts: number;
  createdAt: string;
  updatedAt: string;
  questions?: Question[];
}

export interface AvailableAttempt {
  quizId: number;
  quizTitle: string;
  availableFrom: string;
  dueDate: string;
  maxAttempts: number;
  attemptsRemaining: number;
}

export interface AttemptStartResponse {
  attemptId: number;
  quizId: number;
  quizTitle: string;
  description: string;
  teacherName: string;
  courseName: string;
  timeLimitMinutes: number;
  questionCount: number;
  availableFrom?: string;
  dueDate?: string;
  maxScore: number;
  startedAt: string;
  finishedAt?: string;
  remainingSeconds?: number;
  status: string;
  questions?: Question[];
}

export interface AttemptResponse {
  attemptId: number;
  quizId: number;
  studentId: number;
  quizTitle: string;
  score: number;
  maxScore: number;
  status: string;
  startedAt: string;
  finishedAt: string;
  attemptNumber: number;
}

export interface QuizUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  active: boolean;
}

export interface Question {
  id: number;
  text: string;
  type: string;
  points: number;
  options: Answer[];
  orderNum: number;
}

export interface Answer {
  id: number;
  text: string;
  isCorrect?: boolean;
}

interface PagedResponse<T> {
  content: T[];
}

@Injectable({
  providedIn: 'root'
})
export class QuizService {

  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  private normalizeQuiz(quiz: Quiz): Quiz {
    return {
      ...quiz,
      questions: quiz.questions ?? []
    };
  }

  getQuizzes(): Observable<Quiz[]> {
    return this.http.get<PagedResponse<Quiz> | Quiz[]>(`${this.apiUrl}/quizzes`).pipe(
      map((response) => (Array.isArray(response) ? response : response.content ?? []).map((quiz) => this.normalizeQuiz(quiz)))
    );
  }

  getQuiz(id: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/quizzes/${id}`).pipe(map((quiz) => this.normalizeQuiz(quiz)));
  }

  getQuestions(quizId: number): Observable<Question[]> {
    return this.http.get<Question[]>(`${this.apiUrl}/quizzes/${quizId}/questions`);
  }

  createQuiz(quiz: Quiz): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.apiUrl}/quizzes`, quiz);
  }

  updateQuiz(id: number, quiz: Quiz): Observable<Quiz> {
    return this.http.put<Quiz>(`${this.apiUrl}/quizzes/${id}`, quiz);
  }

  deleteQuiz(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/quizzes/${id}`);
  }

  getAvailableAttempts(): Observable<AvailableAttempt[]> {
    return this.http.get<AvailableAttempt[]>(`${this.apiUrl}/attempts/available`);
  }

  startAttempt(quizId: number, retake = false): Observable<AttemptStartResponse> {
    return this.http.post<AttemptStartResponse>(`${this.apiUrl}/attempts/start/${quizId}?retake=${retake}`, {});
  }

  saveAnswer(attemptId: number, questionId: number, selectedOptionIds: number[]): Observable<AttemptResponse> {
    return this.http.post<AttemptResponse>(`${this.apiUrl}/attempts/${attemptId}/answer`, {
      questionId,
      selectedOptionIds
    });
  }

  finishAttempt(attemptId: number): Observable<AttemptResponse> {
    return this.http.post<AttemptResponse>(`${this.apiUrl}/attempts/${attemptId}/finish`, {});
  }

  getMyResults(): Observable<AttemptResponse[]> {
    return this.http.get<AttemptResponse[]>(`${this.apiUrl}/attempts/my-results`);
  }
}

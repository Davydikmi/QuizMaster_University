import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './auth.guards';
import { AvailableQuizzesComponent } from './components/student/available-quizzes/available-quizzes.component';
import { QuizTakeComponent } from './components/student/quiz-take/quiz-take.component';
import { QuizResultComponent } from './components/student/quiz-result/quiz-result.component';
import { CoursesComponent } from './components/teacher/courses/courses.component';
import { CreateQuizComponent } from './components/teacher/create-quiz/create-quiz.component';
import { AddQuestionsComponent } from './components/teacher/add-questions/add-questions.component';
import { ResultsTableComponent } from './components/teacher/results-table/results-table.component';
import { LoginComponent } from './components/auth/login/login.component';
import { RegisterComponent } from './components/auth/register/register.component';
import { ProfileComponent } from './components/auth/profile/profile.component';
import { StudyGroupsComponent } from './components/admin/study-groups/study-groups.component';
import { StudentsComponent } from './components/admin/students/students.component';

export const routes: Routes = [
  { path: '', redirectTo: 'quizzes', pathMatch: 'full' },
  { path: 'auth/login', component: LoginComponent },
  { path: 'auth/register', component: RegisterComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'quizzes', component: AvailableQuizzesComponent, canActivate: [authGuard] },
  { path: 'quizzes/:id', component: QuizTakeComponent, canActivate: [authGuard, roleGuard(['STUDENT'])] },
  { path: 'results/:id', component: QuizResultComponent, canActivate: [authGuard, roleGuard(['STUDENT'])] },
  { path: 'teacher/courses', component: CoursesComponent, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'teacher/create-quiz', component: CreateQuizComponent, canActivate: [authGuard, roleGuard(['ADMIN', 'TEACHER'])] },
  { path: 'teacher/add-questions', component: AddQuestionsComponent, canActivate: [authGuard, roleGuard(['ADMIN', 'TEACHER'])] },
  { path: 'teacher/results', component: ResultsTableComponent, canActivate: [authGuard, roleGuard(['ADMIN', 'TEACHER'])] },
  { path: 'admin/study-groups', component: StudyGroupsComponent, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'admin/students', component: StudentsComponent, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: '**', redirectTo: 'quizzes' }
];

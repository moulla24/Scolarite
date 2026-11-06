import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/auth/auth.interceptor';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import {
  CLASSROOMS_DATA_PROVIDER,
  DASHBOARD_DATA_PROVIDER,
  GRADES_DATA_PROVIDER,
  MAQUETTES_DATA_PROVIDER,
  NAVIGATION_PROVIDER,
  PARENTS_DATA_PROVIDER,
  SCHEDULE_DATA_PROVIDER,
  STUDENTS_DATA_PROVIDER,
  TEACHERS_DATA_PROVIDER,
} from './core/constants/app.constants';
import { ApiClassroomsService } from './core/services/api-classrooms.service';
import { ApiDashboardService } from './core/services/api-dashboard.service';
import { ApiGradesService } from './core/services/api-grades.service';
import { MaquettesApiService } from './features/maquettes/services/maquettes-api.service';
import { ApiParentsService } from './core/services/api-parents.service';
import { ApiScheduleService } from './core/services/api-schedule.service';
import { ApiStudentsService } from './core/services/api-students.service';
import { ApiTeachersService } from './core/services/api-teachers.service';
import { NavigationApiService } from './core/services/navigation-api.service';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    provideRouter(routes, withComponentInputBinding()),
    { provide: DASHBOARD_DATA_PROVIDER, useExisting: ApiDashboardService },
    { provide: NAVIGATION_PROVIDER, useExisting: NavigationApiService },
    { provide: PARENTS_DATA_PROVIDER, useExisting: ApiParentsService },
    { provide: STUDENTS_DATA_PROVIDER, useExisting: ApiStudentsService },
    { provide: GRADES_DATA_PROVIDER, useExisting: ApiGradesService },
    { provide: MAQUETTES_DATA_PROVIDER, useExisting: MaquettesApiService },
    { provide: TEACHERS_DATA_PROVIDER, useExisting: ApiTeachersService },
    { provide: CLASSROOMS_DATA_PROVIDER, useExisting: ApiClassroomsService },
    { provide: SCHEDULE_DATA_PROVIDER, useExisting: ApiScheduleService },
  ],
};

import { InjectionToken } from '@angular/core';
import { ClassroomsDataProvider } from '../services/interfaces/classrooms-data-provider.interface';
import { DashboardDataProvider } from '../services/interfaces/dashboard-data-provider.interface';
import { GradesDataProvider } from '../services/interfaces/grades-data-provider.interface';
import { MaquettesDataProvider } from '../services/interfaces/maquettes-data-provider.interface';
import { NavigationProvider } from '../services/interfaces/navigation-provider.interface';
import { ParentsDataProvider } from '../services/interfaces/parents-data-provider.interface';
import { ScheduleDataProvider } from '../services/interfaces/schedule-data-provider.interface';
import { StudentsDataProvider } from '../services/interfaces/students-data-provider.interface';
import { TeachersDataProvider } from '../services/interfaces/teachers-data-provider.interface';

export const DASHBOARD_DATA_PROVIDER = new InjectionToken<DashboardDataProvider>(
  'DASHBOARD_DATA_PROVIDER'
);

export const NAVIGATION_PROVIDER = new InjectionToken<NavigationProvider>(
  'NAVIGATION_PROVIDER'
);

export const PARENTS_DATA_PROVIDER = new InjectionToken<ParentsDataProvider>(
  'PARENTS_DATA_PROVIDER'
);

export const STUDENTS_DATA_PROVIDER = new InjectionToken<StudentsDataProvider>(
  'STUDENTS_DATA_PROVIDER'
);

export const GRADES_DATA_PROVIDER = new InjectionToken<GradesDataProvider>(
  'GRADES_DATA_PROVIDER'
);

export const MAQUETTES_DATA_PROVIDER = new InjectionToken<MaquettesDataProvider>(
  'MAQUETTES_DATA_PROVIDER'
);

export const TEACHERS_DATA_PROVIDER = new InjectionToken<TeachersDataProvider>(
  'TEACHERS_DATA_PROVIDER'
);

export const CLASSROOMS_DATA_PROVIDER = new InjectionToken<ClassroomsDataProvider>(
  'CLASSROOMS_DATA_PROVIDER'
);

export const SCHEDULE_DATA_PROVIDER = new InjectionToken<ScheduleDataProvider>(
  'SCHEDULE_DATA_PROVIDER'
);

export const APP_NAME = 'Ecoli';

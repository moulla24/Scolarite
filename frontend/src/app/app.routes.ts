import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { guestGuard } from './core/auth/guest.guard';
import { roleGuard } from './core/auth/role.guard';
import { AdminLayoutComponent } from './core/layout/admin-layout/admin-layout.component';
import { ClassroomsPageComponent } from './features/classrooms/pages/classrooms-page/classrooms-page.component';
import { MaquettesPageComponent } from './features/maquettes/pages/maquettes-page/maquettes-page.component';
import { SchedulePageComponent } from './features/schedule/pages/schedule-page/schedule-page.component';
import { TeachersPageComponent } from './features/teachers/pages/teachers-page/teachers-page.component';
import { ComptablesPageComponent } from './features/comptables/pages/comptables-page/comptables-page.component';
import { ComptabiliteLayoutComponent } from './features/comptabilite/layout/comptabilite-layout.component';
import { ComptabiliteDashboardPageComponent } from './features/comptabilite/pages/comptabilite-dashboard-page/comptabilite-dashboard-page.component';
import { FacturesPageComponent } from './features/comptabilite/pages/factures-page/factures-page.component';
import { PaiementsPageComponent } from './features/comptabilite/pages/paiements-page/paiements-page.component';
import { JournalPageComponent } from './features/comptabilite/pages/journal-page/journal-page.component';
import { BilanPageComponent } from './features/comptabilite/pages/bilan-page/bilan-page.component';
import { PersonnelPageComponent } from './features/comptabilite/pages/personnel-page/personnel-page.component';
import { PaiePageComponent } from './features/comptabilite/pages/paie-page/paie-page.component';
import { FraisPageComponent } from './features/comptabilite/pages/frais-page/frais-page.component';
import { DepensesPageComponent } from './features/comptabilite/pages/depenses-page/depenses-page.component';
import { SoldesPageComponent } from './features/comptabilite/pages/soldes-page/soldes-page.component';
import { RapportsPageComponent } from './features/comptabilite/pages/rapports-page/rapports-page.component';
import { ValidationsPageComponent } from './features/comptabilite/pages/validations-page/validations-page.component';

const adminRoles = roleGuard('ADMIN');
const comptabiliteRoles = roleGuard('ADMIN', 'COMPTABLE');

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/pages/login-page/login-page.component').then(
        (m) => m.LoginPageComponent
      ),
  },
  {
    path: '',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        canActivate: [roleGuard('ADMIN', 'ENSEIGNANT')],
        loadComponent: () =>
          import('./features/dashboard/pages/dashboard-page/dashboard-page.component').then(
            (m) => m.DashboardPageComponent
          ),
      },
      {
        path: 'parents',
        canActivate: [adminRoles],
        loadComponent: () =>
          import('./features/parents/pages/parents-page/parents-page.component').then(
            (m) => m.ParentsPageComponent
          ),
      },
      {
        path: 'students',
        canActivate: [adminRoles],
        loadComponent: () =>
          import('./features/students/pages/students-page/students-page.component').then(
            (m) => m.StudentsPageComponent
          ),
      },
      {
        path: 'notes',
        canActivate: [adminRoles],
        loadComponent: () =>
          import('./features/grades/pages/grades-page/grades-page.component').then(
            (m) => m.GradesPageComponent
          ),
      },
      {
        path: 'affectations',
        canActivate: [adminRoles],
        component: TeachersPageComponent,
        data: { teachersTab: 'affectations' },
      },
      { path: 'grades', redirectTo: 'notes', pathMatch: 'full' },
      { path: 'maquettes', canActivate: [adminRoles], component: MaquettesPageComponent },
      { path: 'enseignants', canActivate: [adminRoles], component: TeachersPageComponent },
      { path: 'classes', canActivate: [adminRoles], component: ClassroomsPageComponent },
      { path: 'emploi-du-temps', canActivate: [adminRoles], component: SchedulePageComponent },
      {
        path: 'absences',
        canActivate: [adminRoles],
        loadComponent: () =>
          import('./features/absences/pages/absences-page/absences-page.component').then(
            (m) => m.AbsencesPageComponent
          ),
      },
      {
        path: 'config-plan/parametres',
        canActivate: [adminRoles],
        loadComponent: () =>
          import('./features/settings/pages/settings-page/settings-page.component').then(
            (m) => m.SettingsPageComponent
          ),
      },
      {
        path: 'config-plan/planification',
        canActivate: [adminRoles],
        loadComponent: () =>
          import('./features/config-plan/pages/planification-page/planification-page.component').then(
            (m) => m.PlanificationPageComponent
          ),
      },
      { path: 'config-plan', redirectTo: 'config-plan/parametres', pathMatch: 'full' },
      { path: 'parametres', redirectTo: 'config-plan/parametres', pathMatch: 'full' },
      { path: 'comptables', canActivate: [adminRoles], component: ComptablesPageComponent },
      {
        path: 'comptabilite',
        canActivate: [comptabiliteRoles],
        component: ComptabiliteLayoutComponent,
        children: [
          { path: '', component: ComptabiliteDashboardPageComponent },
          { path: 'frais', component: FraisPageComponent },
          { path: 'factures', component: FacturesPageComponent },
          { path: 'paiements', component: PaiementsPageComponent },
          { path: 'validations', component: ValidationsPageComponent },
          { path: 'soldes', component: SoldesPageComponent },
          { path: 'depenses', component: DepensesPageComponent },
          { path: 'rapports', component: RapportsPageComponent },
          { path: 'personnel', component: PersonnelPageComponent },
          { path: 'paie', component: PaiePageComponent },
          { path: 'journal', component: JournalPageComponent },
          { path: 'bilan', component: BilanPageComponent },
        ],
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];

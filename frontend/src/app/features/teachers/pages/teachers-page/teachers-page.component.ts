import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { combineLatest, map, take } from 'rxjs';
import { EnseignementDto } from '../../../../core/api/api-dto';
import { ExcelPageActionsComponent } from '../../../../shared/components/excel-page-actions/excel-page-actions.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { SearchFieldComponent } from '../../../../shared/components/search-field/search-field.component';
import {
  downloadAssignmentsTemplate,
  downloadTeachersTemplate,
  exportAssignmentsToExcel,
  exportTeachersToExcel,
  formatImportResult,
  parseAssignmentsImportExcel,
  parseTeachersImportExcel,
  runImportBatch,
} from '../../../../shared/utils/excel.util';
import { AssignmentFormPanelComponent } from '../../../assignments/components/assignment-form-panel/assignment-form-panel.component';
import { AssignmentsTableComponent } from '../../../assignments/components/assignments-table/assignments-table.component';
import { AssignmentsApiService } from '../../../assignments/services/assignments-api.service';
import { AssignmentsFacadeService } from '../../../assignments/services/assignments-facade.service';
import { TeacherCreatePanelComponent } from '../../components/teacher-create-panel/teacher-create-panel.component';
import { TeachersTableComponent } from '../../components/teachers-table/teachers-table.component';
import { TeachersApiService } from '../../services/teachers-api.service';
import { TeachersFacadeService } from '../../services/teachers-facade.service';

export type TeachersTab = 'enseignants' | 'affectations';

@Component({
  selector: 'app-teachers-page',
  standalone: true,
  imports: [
    AsyncPipe,
    FormsModule,
    PageHeaderComponent,
    SearchFieldComponent,
    FilterCardComponent,
    ExcelPageActionsComponent,
    TeacherCreatePanelComponent,
    TeachersTableComponent,
    AssignmentFormPanelComponent,
    AssignmentsTableComponent,
  ],
  providers: [TeachersFacadeService, AssignmentsFacadeService],
  templateUrl: './teachers-page.component.html',
  styleUrl: './teachers-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TeachersPageComponent implements OnInit {
  private readonly facade = inject(TeachersFacadeService);
  private readonly api = inject(TeachersApiService);
  private readonly assignmentsFacade = inject(AssignmentsFacadeService);
  private readonly assignmentsApi = inject(AssignmentsApiService);
  private readonly route = inject(ActivatedRoute);

  readonly activeTab = signal<TeachersTab>('enseignants');

  readonly viewState$ = this.facade.viewState$;
  readonly showCreateForm$ = this.facade.createFormOpen$;
  readonly editId$ = this.facade.editId$;

  readonly assignmentsViewState$ = this.assignmentsFacade.viewState$;
  readonly assignmentsShowCreateForm$ = this.assignmentsFacade.createFormOpen$;
  readonly assignmentsEditId$ = this.assignmentsFacade.editId$;

  readonly pageState$ = combineLatest([this.viewState$, this.showCreateForm$, this.editId$]).pipe(
    map(([view, showCreateForm, editId]) => ({ view, showCreateForm, editId }))
  );

  readonly assignmentsPageState$ = combineLatest([
    this.assignmentsViewState$,
    this.assignmentsShowCreateForm$,
    this.assignmentsEditId$,
  ]).pipe(map(([view, showCreateForm, editId]) => ({ view, showCreateForm, editId })));

  ngOnInit(): void {
    const routeTab = this.route.snapshot.data['teachersTab'] as TeachersTab | undefined;
    const queryTab = this.route.snapshot.queryParamMap.get('tab');
    if (routeTab === 'affectations' || queryTab === 'affectations') {
      this.activeTab.set('affectations');
    }
  }

  setTab(tab: TeachersTab): void {
    this.facade.closeCreateForm();
    this.assignmentsFacade.closeCreateForm();
    this.activeTab.set(tab);
  }

  onSearch(q: string): void {
    this.facade.setSearch(q);
  }

  onSubject(id: string): void {
    this.facade.setSubject(id);
  }

  onAssignmentSearch(q: string): void {
    this.assignmentsFacade.setSearch(q);
  }

  onAssignmentClass(id: string): void {
    this.assignmentsFacade.setClass(id);
  }

  onAssignmentSubject(id: string): void {
    this.assignmentsFacade.setSubject(id);
  }

  onAssignmentLevel(id: string): void {
    this.assignmentsFacade.setLevel(id);
  }

  downloadTeachersExcelTemplate(): void {
    downloadTeachersTemplate();
  }

  downloadAssignmentsExcelTemplate(): void {
    downloadAssignmentsTemplate();
  }

  onExportTeachers(): void {
    this.viewState$.pipe(take(1)).subscribe((state) => exportTeachersToExcel(state.teachers));
  }

  onExportAssignments(): void {
    this.assignmentsViewState$
      .pipe(take(1))
      .subscribe((state) => exportAssignmentsToExcel(state.assignments));
  }

  async onImportTeachersFile(file: File): Promise<void> {
    try {
      const rows = await parseTeachersImportExcel(file);
      if (rows.length === 0) {
        alert('Fichier vide ou colonnes incorrectes. Téléchargez le modèle Excel.');
        return;
      }
      const result = await runImportBatch(rows, (row) =>
        this.api.create({
          nom: row.nom,
          prenom: row.prenom,
          email: row.email,
          telephone: row.telephone,
          specialite: row.specialite,
          dateEmbauche: null,
          actif: row.actif,
        })
      );
      alert(formatImportResult(result));
      this.facade.refreshList();
    } catch {
      alert('Import impossible. Vérifiez le fichier Excel.');
    }
  }

  async onImportAssignmentsFile(file: File): Promise<void> {
    try {
      const rows = await parseAssignmentsImportExcel(file);
      if (rows.length === 0) {
        alert('Fichier vide ou colonnes incorrectes. Téléchargez le modèle Excel.');
        return;
      }
      const result = await runImportBatch(rows, (row) => this.assignmentsApi.create(row));
      alert(formatImportResult(result));
      this.assignmentsFacade.refreshList();
    } catch {
      alert('Import impossible. Vérifiez le fichier Excel.');
    }
  }

  onNew(): void {
    if (this.activeTab() === 'affectations') {
      this.assignmentsFacade.toggleCreateForm();
      return;
    }
    this.facade.toggleCreateForm();
  }

  onTeacherSaved(): void {
    this.facade.refreshList();
    this.facade.closeCreateForm();
  }

  onCreateCancelled(): void {
    this.facade.closeCreateForm();
  }

  onAssignmentSaved(): void {
    this.assignmentsFacade.refreshList();
    this.assignmentsFacade.closeCreateForm();
  }

  onAssignmentCancelled(): void {
    this.assignmentsFacade.closeCreateForm();
  }

  onEditTeacher(id: string): void {
    this.facade.openEditForm(Number(id));
  }

  onDeleteTeacher(id: string, teachers: readonly { id: string; fullName: string }[]): void {
    const teacher = teachers.find((t) => t.id === id);
    const label = teacher?.fullName ?? 'cet enseignant';
    if (!confirm(`Supprimer « ${label} » ? Cette action est irréversible.`)) {
      return;
    }
    this.api.delete(Number(id)).subscribe({
      next: () => this.facade.refreshList(),
      error: (err: { error?: { message?: string } }) => {
        alert(err?.error?.message ?? 'Impossible de supprimer cet enseignant.');
      },
    });
  }

  onEditAssignment(id: number): void {
    this.assignmentsFacade.openEditForm(id);
  }

  onDeleteAssignment(id: number, assignments: readonly EnseignementDto[]): void {
    const item = assignments.find((a) => a.id === id);
    const label = item?.libelle ?? 'cette affectation';
    if (!confirm(`Supprimer l’affectation « ${label} » ?`)) return;
    this.assignmentsApi.delete(id).subscribe({
      next: () => this.assignmentsFacade.refreshList(),
      error: (err: { error?: { message?: string } }) =>
        alert(err?.error?.message ?? 'Suppression impossible.'),
    });
  }
}

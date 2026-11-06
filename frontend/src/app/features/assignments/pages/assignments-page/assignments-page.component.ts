import { AsyncPipe } from '@angular/common';

import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { combineLatest, map, take } from 'rxjs';

import { EnseignementDto } from '../../../../core/api/api-dto';

import { ExcelPageActionsComponent } from '../../../../shared/components/excel-page-actions/excel-page-actions.component';

import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';

import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

import { SearchFieldComponent } from '../../../../shared/components/search-field/search-field.component';

import {

  downloadAssignmentsTemplate,

  exportAssignmentsToExcel,

  formatImportResult,

  parseAssignmentsImportExcel,

  runImportBatch,

} from '../../../../shared/utils/excel.util';

import { AssignmentFormPanelComponent } from '../../components/assignment-form-panel/assignment-form-panel.component';

import { AssignmentsTableComponent } from '../../components/assignments-table/assignments-table.component';

import { AssignmentsApiService } from '../../services/assignments-api.service';

import { AssignmentsFacadeService } from '../../services/assignments-facade.service';



@Component({

  selector: 'app-assignments-page',

  standalone: true,

  imports: [

    AsyncPipe,

    FormsModule,

    PageHeaderComponent,

    SearchFieldComponent,

    FilterCardComponent,

    ExcelPageActionsComponent,

    AssignmentFormPanelComponent,

    AssignmentsTableComponent,

  ],

  providers: [AssignmentsFacadeService],

  templateUrl: './assignments-page.component.html',

  styleUrl: './assignments-page.component.scss',

  changeDetection: ChangeDetectionStrategy.OnPush,

})

export class AssignmentsPageComponent {

  private readonly facade = inject(AssignmentsFacadeService);

  private readonly api = inject(AssignmentsApiService);



  readonly viewState$ = this.facade.viewState$;

  readonly showCreateForm$ = this.facade.createFormOpen$;

  readonly editId$ = this.facade.editId$;



  readonly pageState$ = combineLatest([this.viewState$, this.showCreateForm$, this.editId$]).pipe(

    map(([view, showCreateForm, editId]) => ({ view, showCreateForm, editId }))

  );



  onSearch(q: string): void {

    this.facade.setSearch(q);

  }



  onClass(id: string): void {

    this.facade.setClass(id);

  }



  onSubject(id: string): void {

    this.facade.setSubject(id);

  }



  onLevel(id: string): void {

    this.facade.setLevel(id);

  }



  downloadExcelTemplate(): void {

    downloadAssignmentsTemplate();

  }



  onExport(): void {

    this.viewState$.pipe(take(1)).subscribe((state) => exportAssignmentsToExcel(state.assignments));

  }



  async onImportFile(file: File): Promise<void> {

    try {

      const rows = await parseAssignmentsImportExcel(file);

      if (rows.length === 0) {

        alert('Fichier vide ou colonnes incorrectes. Téléchargez le modèle Excel.');

        return;

      }

      const result = await runImportBatch(rows, (row) => this.api.create(row));

      alert(formatImportResult(result));

      this.facade.refreshList();

    } catch {

      alert('Import impossible. Vérifiez le fichier Excel.');

    }

  }



  onNew(): void {

    this.facade.toggleCreateForm();

  }



  onSaved(): void {

    this.facade.refreshList();

    this.facade.closeCreateForm();

  }



  onCancelled(): void {

    this.facade.closeCreateForm();

  }



  onEdit(id: number): void {

    this.facade.openEditForm(id);

  }



  onDelete(id: number, assignments: readonly EnseignementDto[]): void {

    const item = assignments.find((a) => a.id === id);

    const label = item?.libelle ?? 'cette affectation';

    if (!confirm(`Supprimer l’affectation « ${label} » ?`)) return;

    this.api.delete(id).subscribe({

      next: () => this.facade.refreshList(),

      error: (err: { error?: { message?: string } }) =>

        alert(err?.error?.message ?? 'Suppression impossible.'),

    });

  }

}



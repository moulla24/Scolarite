import { AsyncPipe } from '@angular/common';

import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { combineLatest, map, take } from 'rxjs';

import { ExcelPageActionsComponent } from '../../../../shared/components/excel-page-actions/excel-page-actions.component';

import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

import { SearchFieldComponent } from '../../../../shared/components/search-field/search-field.component';

import {

  downloadStudentsTemplate,

  exportStudentsToExcel,

  formatImportResult,

  parseStudentsImportExcel,

  runImportBatch,

} from '../../../../shared/utils/excel.util';

import { StudentCreatePanelComponent } from '../../components/student-create-panel/student-create-panel.component';

import { StudentsTableComponent } from '../../components/students-table/students-table.component';

import { StudentsApiService } from '../../services/students-api.service';

import { StudentsFacadeService } from '../../services/students-facade.service';



@Component({

  selector: 'app-students-page',

  standalone: true,

  imports: [

    AsyncPipe,

    FormsModule,

    PageHeaderComponent,

    SearchFieldComponent,

    ExcelPageActionsComponent,

    StudentCreatePanelComponent,

    StudentsTableComponent,

  ],

  providers: [StudentsFacadeService],

  templateUrl: './students-page.component.html',

  styleUrl: './students-page.component.scss',

  changeDetection: ChangeDetectionStrategy.OnPush,

})

export class StudentsPageComponent {

  private readonly facade = inject(StudentsFacadeService);

  private readonly api = inject(StudentsApiService);



  readonly viewState$ = this.facade.viewState$;

  readonly showCreateForm$ = this.facade.createFormOpen$;

  readonly editId$ = this.facade.editId$;



  readonly pageState$ = combineLatest([this.viewState$, this.showCreateForm$, this.editId$]).pipe(

    map(([view, showCreateForm, editId]) => ({ view, showCreateForm, editId }))

  );



  onSearch(query: string): void {

    this.facade.setSearch(query);

  }



  onLevelChange(levelId: string): void {

    this.facade.setLevel(levelId);

  }



  onClassChange(classId: string): void {

    this.facade.setClass(classId);

  }



  downloadExcelTemplate(): void {

    downloadStudentsTemplate();

  }



  onExport(): void {

    this.viewState$.pipe(take(1)).subscribe((state) => exportStudentsToExcel(state.students));

  }



  async onImportFile(file: File): Promise<void> {

    try {

      const rows = await parseStudentsImportExcel(file);

      if (rows.length === 0) {

        alert('Fichier vide ou colonnes incorrectes. Téléchargez le modèle Excel.');

        return;

      }

      const result = await runImportBatch(rows, (row) =>

        this.api.create({

          matricule: row.matricule,

          nom: row.nom,

          prenom: row.prenom,

          tuteurId: row.tuteurId,

          classeId: row.classeId,

          ecoleId: this.api.defaultEcoleId,

          dateNaissance: row.dateNaissance,

          sexe: row.sexe,

        })

      );

      alert(formatImportResult(result));

      this.facade.refreshList();

    } catch {

      alert('Import impossible. Vérifiez le fichier Excel.');

    }

  }



  onNewStudent(): void {

    this.facade.toggleCreateForm();

  }



  onStudentSaved(): void {

    this.facade.refreshList();

    this.facade.closeCreateForm();

  }



  onCreateCancelled(): void {

    this.facade.closeCreateForm();

  }



  onEditStudent(id: string): void {

    this.facade.openEditForm(Number(id));

  }



  onDeleteStudent(id: string, students: readonly { id: string; fullName: string }[]): void {

    const student = students.find((s) => s.id === id);

    const label = student?.fullName ?? 'cet élève';

    if (!confirm(`Supprimer « ${label} » ? Cette action est irréversible.`)) {

      return;

    }

    this.api.delete(Number(id)).subscribe({

      next: () => this.facade.refreshList(),

      error: (err: { error?: { message?: string } }) => {

        alert(err?.error?.message ?? 'Impossible de supprimer cet élève.');

      },

    });

  }

}



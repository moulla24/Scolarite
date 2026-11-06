import { AsyncPipe } from '@angular/common';

import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { combineLatest, map, take } from 'rxjs';

import { ExcelPageActionsComponent } from '../../../../shared/components/excel-page-actions/excel-page-actions.component';

import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';

import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

import { SearchFieldComponent } from '../../../../shared/components/search-field/search-field.component';

import {

  downloadClassroomsTemplate,

  exportClassroomsToExcel,

  formatImportResult,

  parseClassroomsImportExcel,

  runImportBatch,

} from '../../../../shared/utils/excel.util';

import { ClassroomCreatePanelComponent } from '../../components/classroom-create-panel/classroom-create-panel.component';

import { ClassroomsGridComponent } from '../../components/classrooms-grid/classrooms-grid.component';

import { ClassroomsApiService } from '../../services/classrooms-api.service';

import { ClassroomsFacadeService } from '../../services/classrooms-facade.service';



@Component({

  selector: 'app-classrooms-page',

  standalone: true,

  imports: [

    AsyncPipe,

    FormsModule,

    PageHeaderComponent,

    SearchFieldComponent,

    FilterCardComponent,

    ExcelPageActionsComponent,

    ClassroomCreatePanelComponent,

    ClassroomsGridComponent,

  ],

  providers: [ClassroomsFacadeService],

  templateUrl: './classrooms-page.component.html',

  styleUrl: './classrooms-page.component.scss',

  changeDetection: ChangeDetectionStrategy.OnPush,

})

export class ClassroomsPageComponent {

  private readonly facade = inject(ClassroomsFacadeService);

  private readonly api = inject(ClassroomsApiService);



  readonly viewState$ = this.facade.viewState$;

  readonly showCreateForm$ = this.facade.createFormOpen$;

  readonly editId$ = this.facade.editId$;



  readonly pageState$ = combineLatest([this.viewState$, this.showCreateForm$, this.editId$]).pipe(

    map(([view, showCreateForm, editId]) => ({ view, showCreateForm, editId }))

  );



  onSearch(q: string): void {

    this.facade.setSearch(q);

  }



  onLevel(id: string): void {

    this.facade.setLevel(id);

  }



  downloadExcelTemplate(): void {

    downloadClassroomsTemplate();

  }



  onExport(): void {

    this.viewState$.pipe(take(1)).subscribe((state) => exportClassroomsToExcel(state.classrooms));

  }



  async onImportFile(file: File): Promise<void> {

    try {

      const rows = await parseClassroomsImportExcel(file);

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



  onClassroomSaved(): void {

    this.facade.refreshList();

    this.facade.closeCreateForm();

  }



  onCreateCancelled(): void {

    this.facade.closeCreateForm();

  }



  onViewDetail(id: string): void {

    this.api.getById(Number(id)).subscribe({

      next: (dto) => {

        alert(

          `Classe : ${dto.nom}\n` +

            `Niveau : ${dto.niveau}\n` +

            `Salle : ${dto.salle ?? '—'}\n` +

            `Effectif : ${dto.effectif} / ${dto.capacite}\n` +

            `Enseignant référent : ${dto.enseignantReferent}\n` +

            `Taux d’occupation : ${dto.tauxOccupation} %`

        );

      },

      error: () => alert('Impossible de charger le détail de la classe.'),

    });

  }

}



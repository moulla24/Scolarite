import { AsyncPipe } from '@angular/common';

import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { combineLatest, firstValueFrom, map, take, throwError } from 'rxjs';

import { ExcelPageActionsComponent } from '../../../../shared/components/excel-page-actions/excel-page-actions.component';

import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';

import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';

import { SearchFieldComponent } from '../../../../shared/components/search-field/search-field.component';

import {
  buildMatriculeToEleveIdMap,
  downloadParentsTemplate,
  exportParentsToExcel,
  formatImportResult,
  parseParentsImportExcel,
  resolveEleveIdsFromMatricules,
  runImportBatch,
} from '../../../../shared/utils/excel.util';

import { ParentCreatePanelComponent } from '../../components/parent-create-panel/parent-create-panel.component';

import { ParentsTableComponent } from '../../components/parents-table/parents-table.component';

import { ParentsApiService } from '../../services/parents-api.service';

import { ParentsFacadeService } from '../../services/parents-facade.service';



@Component({

  selector: 'app-parents-page',

  standalone: true,

  imports: [

    AsyncPipe,

    FormsModule,

    PageHeaderComponent,

    SearchFieldComponent,

    FilterCardComponent,

    ExcelPageActionsComponent,

    ParentCreatePanelComponent,

    ParentsTableComponent,

  ],

  providers: [ParentsFacadeService],

  templateUrl: './parents-page.component.html',

  styleUrl: './parents-page.component.scss',

  changeDetection: ChangeDetectionStrategy.OnPush,

})

export class ParentsPageComponent {

  private readonly facade = inject(ParentsFacadeService);

  private readonly api = inject(ParentsApiService);



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



  onStatutChange(statutId: string): void {

    this.facade.setStatut(statutId);

  }



  downloadExcelTemplate(): void {
    this.api.listAllEleves().subscribe({
      next: (eleves) => downloadParentsTemplate(eleves),
      error: () => downloadParentsTemplate(),
    });
  }



  onExport(): void {

    this.viewState$.pipe(take(1)).subscribe((state) => exportParentsToExcel(state.parents));

  }



  async onImportFile(file: File): Promise<void> {

    try {

      const rows = await parseParentsImportExcel(file);

      if (rows.length === 0) {

        alert('Fichier vide ou colonnes incorrectes. Téléchargez le modèle Excel.');

        return;

      }

      const eleves = await firstValueFrom(this.api.listAllEleves());
      const matriculeToId = buildMatriculeToEleveIdMap(eleves);

      const result = await runImportBatch(rows, (row) => {
        const { eleveIds, unknownMatricules } = resolveEleveIdsFromMatricules(
          row.matricules,
          matriculeToId
        );
        if (unknownMatricules.length > 0) {
          return throwError(
            () =>
              new Error(
                `Matricule(s) inconnu(s) pour ${row.nom} : ${unknownMatricules.join(', ')}`
              )
          );
        }
        return this.api.create({
          nom: row.nom,
          prenom: row.prenom,
          telephone: row.telephone,
          email: row.email,
          adresse: row.adresse,
          eleveIds,
        });
      });

      alert(formatImportResult(result));

      this.facade.refreshList();

    } catch {

      alert('Import impossible. Vérifiez le fichier Excel.');

    }

  }



  onNewParent(): void {

    this.facade.toggleCreateForm();

  }



  onParentSaved(): void {

    this.facade.refreshList();

    this.facade.closeCreateForm();

  }



  onCreateCancelled(): void {

    this.facade.closeCreateForm();

  }



  onEditParent(id: string): void {

    this.facade.openEditForm(Number(id));

  }



  onDeleteParent(id: string, parents: readonly { id: string; fullName: string }[]): void {

    const parent = parents.find((p) => p.id === id);

    const label = parent?.fullName ?? 'ce parent';

    if (!confirm(`Supprimer « ${label} » ? Cette action est irréversible.`)) {

      return;

    }

    this.api.delete(Number(id)).subscribe({

      next: () => this.facade.refreshList(),

      error: (err: { error?: { message?: string } }) => {

        alert(err?.error?.message ?? 'Impossible de supprimer ce parent.');

      },

    });

  }

}



import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { combineLatest, map } from 'rxjs';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { SearchFieldComponent } from '../../../../shared/components/search-field/search-field.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { ComptableCreatePanelComponent } from '../../components/comptable-create-panel/comptable-create-panel.component';
import { ComptablesTableComponent } from '../../components/comptables-table/comptables-table.component';
import { ComptablesApiService } from '../../services/comptables-api.service';
import { ComptablesFacadeService } from '../../services/comptables-facade.service';

@Component({
  selector: 'app-comptables-page',
  standalone: true,
  imports: [
    AsyncPipe,
    PageHeaderComponent,
    SearchFieldComponent,
    FilterCardComponent,
    ComptableCreatePanelComponent,
    ComptablesTableComponent,
  ],
  providers: [ComptablesFacadeService],
  templateUrl: './comptables-page.component.html',
  styleUrl: './comptables-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComptablesPageComponent {
  private readonly facade = inject(ComptablesFacadeService);
  private readonly api = inject(ComptablesApiService);

  readonly viewState$ = this.facade.viewState$;
  readonly showCreateForm$ = this.facade.createFormOpen$;
  readonly editId$ = this.facade.editId$;

  readonly pageState$ = combineLatest([this.viewState$, this.showCreateForm$, this.editId$]).pipe(
    map(([view, showCreateForm, editId]) => ({ view, showCreateForm, editId }))
  );

  onSearch(q: string): void {
    this.facade.setSearch(q);
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

  onEdit(id: string): void {
    this.facade.openEditForm(Number(id));
  }

  onDelete(id: string, comptables: readonly { id: string; fullName: string }[]): void {
    const item = comptables.find((c) => c.id === id);
    const label = item?.fullName ?? 'ce comptable';
    if (!confirm(`Supprimer « ${label} » ? Cette action est irréversible.`)) return;
    this.api.delete(Number(id)).subscribe({
      next: () => this.facade.refreshList(),
      error: (err: { error?: { message?: string } }) => {
        alert(err?.error?.message ?? 'Impossible de supprimer ce comptable.');
      },
    });
  }
}

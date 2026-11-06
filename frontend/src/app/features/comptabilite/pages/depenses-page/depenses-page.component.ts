import { DatePipe, KeyValuePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DepenseScolaireDto, DepenseScolaireRequest } from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';

@Component({
  selector: 'app-depenses-page',
  standalone: true,
  imports: [DatePipe, KeyValuePipe, FormsModule, PageHeaderComponent, FilterCardComponent, CurrencyMruPipe],
  templateUrl: './depenses-page.component.html',
  styleUrl: './depenses-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DepensesPageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);

  readonly depenses = signal<readonly DepenseScolaireDto[]>([]);
  readonly categories = signal<Record<string, string>>({});
  readonly categorie = signal('');
  readonly showForm = signal(false);
  readonly saving = signal(false);

  form: DepenseScolaireRequest = {
    categorie: 'FOURNITURES',
    dateDepense: new Date().toISOString().slice(0, 10),
    montant: 0,
    description: '',
  };

  ngOnInit(): void {
    this.api.getDepenseCategories().subscribe((c) => this.categories.set(c));
    this.load();
  }

  load(): void {
    this.api.listDepenses(this.categorie() || undefined).subscribe((list) => this.depenses.set(list));
  }

  onFilterChange(): void {
    this.load();
  }

  openForm(): void {
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
  }

  submit(): void {
    if (!this.form.description.trim() || this.form.montant <= 0) return;
    this.saving.set(true);
    this.api.createDepense(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.form = {
          categorie: 'FOURNITURES',
          dateDepense: new Date().toISOString().slice(0, 10),
          montant: 0,
          description: '',
        };
        this.load();
      },
      error: () => this.saving.set(false),
    });
  }
}

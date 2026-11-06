import { DatePipe, KeyValuePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PersonnelDto, PersonnelRequest } from '../../../../core/api/api-dto';
import { PageHeaderComponent } from '../../../../shared/components/page-header/page-header.component';
import { FilterCardComponent } from '../../../../shared/components/filter-card/filter-card.component';
import { SearchFieldComponent } from '../../../../shared/components/search-field/search-field.component';
import { CurrencyMruPipe } from '../../../../shared/pipes/currency-mru.pipe';
import { ComptabiliteApiService } from '../../services/comptabilite-api.service';

@Component({
  selector: 'app-personnel-page',
  standalone: true,
  imports: [
    DatePipe,
    KeyValuePipe,
    FormsModule,
    PageHeaderComponent,
    FilterCardComponent,
    SearchFieldComponent,
    CurrencyMruPipe,
  ],
  templateUrl: './personnel-page.component.html',
  styleUrl: './personnel-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonnelPageComponent implements OnInit {
  private readonly api = inject(ComptabiliteApiService);

  readonly personnel = signal<readonly PersonnelDto[]>([]);
  readonly categories = signal<Record<string, string>>({});
  readonly search = signal('');
  readonly categorie = signal('');
  readonly statut = signal('ACTIF');
  readonly showForm = signal(false);
  readonly editId = signal<number | null>(null);
  readonly saving = signal(false);

  form: PersonnelRequest = this.emptyForm();

  ngOnInit(): void {
    this.api.getPersonnelCategories().subscribe((c) => this.categories.set(c));
    this.load();
  }

  load(): void {
    this.api
      .listPersonnel(this.categorie() || undefined, this.statut() || undefined, this.search() || undefined)
      .subscribe((list) => this.personnel.set(list));
  }

  onSearch(value: string): void {
    this.search.set(value);
    this.load();
  }

  onFilterChange(): void {
    this.load();
  }

  openCreate(): void {
    this.editId.set(null);
    this.form = this.emptyForm();
    this.showForm.set(true);
  }

  openEdit(p: PersonnelDto): void {
    this.editId.set(p.id);
    this.form = {
      nom: p.nom,
      prenom: p.prenom,
      email: p.email,
      telephone: p.telephone,
      matricule: p.matricule,
      categorie: p.categorie,
      enseignantId: p.enseignantId,
      salaireMensuel: p.salaireMensuel,
      dateEmbauche: p.dateEmbauche,
      statut: p.statut,
    };
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editId.set(null);
  }

  save(): void {
    this.saving.set(true);
    const id = this.editId();
    const req = id ? this.api.updatePersonnel(id, this.form) : this.api.createPersonnel(this.form);
    req.subscribe({
      next: () => {
        this.saving.set(false);
        this.showForm.set(false);
        this.load();
      },
      error: () => this.saving.set(false),
    });
  }

  delete(p: PersonnelDto): void {
    if (!confirm(`Supprimer ${p.nomComplet} ?`)) return;
    this.api.deletePersonnel(p.id).subscribe(() => this.load());
  }

  private emptyForm(): PersonnelRequest {
    return {
      nom: '',
      prenom: '',
      email: '',
      telephone: '',
      matricule: '',
      categorie: 'AGENT_ENTRETIEN',
      enseignantId: null,
      salaireMensuel: 25000,
      dateEmbauche: null,
      statut: 'ACTIF',
    };
  }
}

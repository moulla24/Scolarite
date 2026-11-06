import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ClasseDto, EleveDto, NiveauDto } from '../../../../core/api/api-dto';
import { ReferentielApiService } from '../../../../core/api/referentiel-api.service';
import { environment } from '../../../../../environments/environment';
import {
  ComptabiliteEleveFilterState,
  emptyEleveFilters,
} from './comptabilite-eleve-filters.model';

@Component({
  selector: 'app-comptabilite-eleve-filters',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './comptabilite-eleve-filters.component.html',
  styleUrl: './comptabilite-eleve-filters.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComptabiliteEleveFiltersComponent implements OnInit {
  private readonly referentiel = inject(ReferentielApiService);
  private readonly http = inject(HttpClient);

  readonly allowAllEleves = input(true);
  readonly elevePlaceholder = input('Tous les élèves');
  readonly value = input<ComptabiliteEleveFilterState>(emptyEleveFilters());

  readonly valueChange = output<ComptabiliteEleveFilterState>();

  readonly niveaux = signal<readonly NiveauDto[]>([]);
  readonly classes = signal<readonly ClasseDto[]>([]);
  readonly eleves = signal<readonly EleveDto[]>([]);

  readonly filteredEleves = computed(() => {
    const q = this.value().eleveSearch.trim().toLowerCase();
    const list = this.eleves();
    if (!q) return list;
    return list.filter(
      (e) =>
        e.nomComplet.toLowerCase().includes(q) ||
        e.matricule.toLowerCase().includes(q) ||
        e.classe.toLowerCase().includes(q)
    );
  });

  ngOnInit(): void {
    this.referentiel.getNiveaux().subscribe((n) => this.niveaux.set(n));
    const v = this.value();
    if (v.niveauId) {
      this.loadClasses(v.niveauId);
    }
    this.loadEleves(v.niveauId, v.classeId, v.eleveSearch);
  }

  onNiveauChange(niveauId: string): void {
    this.classes.set([]);
    this.eleves.set([]);
    if (niveauId) {
      this.loadClasses(niveauId);
    }
    this.loadEleves(niveauId);
    this.valueChange.emit({
      niveauId,
      classeId: '',
      eleveId: '',
      eleveSearch: this.value().eleveSearch,
    });
  }

  onClasseChange(classeId: string): void {
    this.loadEleves(this.value().niveauId, classeId, this.value().eleveSearch);
    this.valueChange.emit({
      ...this.value(),
      classeId,
      eleveId: '',
    });
  }

  onEleveSearch(eleveSearch: string): void {
    this.loadEleves(this.value().niveauId, this.value().classeId, eleveSearch);
    this.valueChange.emit({
      ...this.value(),
      eleveSearch,
      eleveId: '',
    });
  }

  onEleveChange(eleveId: string): void {
    this.valueChange.emit({ ...this.value(), eleveId });
  }

  private loadClasses(niveauId: string): void {
    this.referentiel.getClasses(niveauId).subscribe((list) => this.classes.set(list));
  }

  private loadEleves(niveauId?: string, classeId?: string, search?: string): void {
    const params: Record<string, string> = {};
    if (niveauId) params['niveauId'] = niveauId;
    if (classeId) params['classeId'] = classeId;
    if (search?.trim()) params['search'] = search.trim();
    this.http.get<EleveDto[]>(`${environment.apiUrl}/eleves`, { params }).subscribe({
      next: (list) => this.eleves.set(list),
    });
  }
}

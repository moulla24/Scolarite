import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FilterOption } from '../../../../core/models/filter-option.model';
import { ReferentielApiService } from '../../../../core/api/referentiel-api.service';
import { toClassFilters } from '../../../../core/api/api-mappers';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';
import { EnseignantDto, SeanceEmploiDto } from '../../../../core/api/api-dto';
import { ScheduleApiService } from '../../services/schedule-api.service';
import { ScheduleSlot } from '../../../../core/models/schedule.model';

export interface ScheduleFormModel {
  classeId: string;
  matiereId: string;
  enseignantId: string;
  jourSemaine: string;
  heureDebut: string;
  heureFin: string;
  salle: string;
  type: string;
}

const emptyForm = (classeId = ''): ScheduleFormModel => ({
  classeId,
  matiereId: '',
  enseignantId: '',
  jourSemaine: '1',
  heureDebut: '08:00',
  heureFin: '10:00',
  salle: '',
  type: 'Cours',
});

@Component({
  selector: 'app-schedule-form-panel',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './schedule-form-panel.component.html',
  styleUrl: '../../../students/components/student-create-panel/student-create-panel.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScheduleFormPanelComponent implements OnInit {
  private readonly api = inject(ScheduleApiService);
  private readonly referentiel = inject(ReferentielApiService);
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  readonly defaultClasseId = input<string>('all');
  readonly semaineId = input<number | null>(null);
  readonly editSlot = input<ScheduleSlot | null>(null);

  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly form = signal<ScheduleFormModel>(emptyForm());
  readonly classOptions = signal<readonly FilterOption[]>([]);
  readonly subjectOptions = signal<readonly FilterOption[]>([]);
  readonly teacherOptions = signal<readonly FilterOption[]>([]);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  private editingId: number | null = null;

  ngOnInit(): void {
    this.referentiel.getClasses().subscribe({
      next: (c) => this.classOptions.set(this.withoutAll(toClassFilters(c))),
    });
    this.referentiel.getMatieres().subscribe({
      next: (m) =>
        this.subjectOptions.set(
          m.map((x) => ({ id: String(x.id), label: x.nom }))
        ),
    });
    this.http.get<EnseignantDto[]>(`${this.base}/enseignants`).subscribe({
      next: (list) =>
        this.teacherOptions.set(
          list.map((t) => ({ id: String(t.id), label: t.nomComplet }))
        ),
    });

    const slot = this.editSlot();
    const classeId = this.defaultClasseId();
    if (slot) {
      this.editingId = Number(slot.id);
      const [debut, fin] = slot.startTime && slot.endTime
        ? [slot.startTime, slot.endTime]
        : ['08:00', '09:00'];
      this.form.set({
        classeId: slot.classId ?? classeId !== 'all' ? classeId : '',
        matiereId: '',
        enseignantId: '',
        jourSemaine: String(slot.dayIndex),
        heureDebut: debut,
        heureFin: fin,
        salle: slot.room,
        type: 'Cours',
      });
      this.loadSeanceForEdit(Number(slot.id));
    } else {
      this.form.set(emptyForm(classeId !== 'all' ? classeId : ''));
    }
  }

  patchField(field: keyof ScheduleFormModel, value: string): void {
    this.form.update((f) => ({ ...f, [field]: value }));
  }

  onSubmit(): void {
    const f = this.form();
    const err = this.validate(f);
    if (err) {
      this.errorMessage.set(err);
      return;
    }

    const semaineId = this.semaineId() ?? Number(this.editSlot()?.semaineId);
    if (!semaineId) {
      this.errorMessage.set('Semaine non définie.');
      return;
    }

    const payload = {
      semaineId,
      classeId: Number(f.classeId),
      matiereId: Number(f.matiereId),
      enseignantId: Number(f.enseignantId),
      jourSemaine: Number(f.jourSemaine),
      heureDebut: f.heureDebut,
      heureFin: f.heureFin,
      salle: f.salle.trim(),
      type: f.type.trim() || 'Cours',
    };

    this.saving.set(true);
    this.errorMessage.set(null);

    const req$ = this.editingId
      ? this.api.update(this.editingId, payload)
      : this.api.create(payload);

    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.saved.emit();
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('Impossible d’enregistrer le créneau.');
      },
    });
  }

  onDelete(): void {
    if (!this.editingId) return;
    if (!confirm('Supprimer ce créneau ?')) return;
    this.api.delete(this.editingId).subscribe({
      next: () => this.saved.emit(),
      error: () => this.errorMessage.set('Suppression impossible.'),
    });
  }

  onCancel(): void {
    this.cancelled.emit();
  }

  private loadSeanceForEdit(id: number): void {
    this.api.getById(id).subscribe({
      next: (s: SeanceEmploiDto) => {
        const horaire = s.horaire?.split(' - ') ?? ['08:00', '09:00'];
        this.form.update((f) => ({
          ...f,
          jourSemaine: String(s.jourIndex),
          heureDebut: horaire[0]?.trim() ?? f.heureDebut,
          heureFin: horaire[1]?.trim() ?? f.heureFin,
          salle: s.salle ?? '',
        }));
      },
    });
  }

  private validate(f: ScheduleFormModel): string | null {
    if (!f.classeId) return 'La classe est obligatoire.';
    if (!f.matiereId) return 'La matière est obligatoire.';
    if (!f.enseignantId) return 'L’enseignant est obligatoire.';
    if (!f.heureDebut || !f.heureFin) return 'Les horaires sont obligatoires.';
    return null;
  }

  private withoutAll(options: readonly FilterOption[]): readonly FilterOption[] {
    return options.filter((o) => o.id !== 'all');
  }
}

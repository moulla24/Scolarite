import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MaquetteMatiereCardDto } from '../../../../core/api/api-dto';

@Component({
  selector: 'app-maquette-matieres-grid',
  standalone: true,
  templateUrl: './maquette-matieres-grid.component.html',
  styleUrl: './maquette-matieres-grid.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MaquetteMatieresGridComponent {
  readonly matieres = input.required<readonly MaquetteMatiereCardDto[]>();
  readonly selectedId = input<number | null>(null);

  readonly selectMatiere = output<number>();
  readonly removeMatiere = output<number>();
}

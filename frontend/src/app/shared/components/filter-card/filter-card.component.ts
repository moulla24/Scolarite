import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-filter-card',
  standalone: true,
  templateUrl: './filter-card.component.html',
  styleUrl: './filter-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FilterCardComponent {
  readonly resultCount = input<number | null>(null);
  readonly resultLabel = input('résultats affichés');
}

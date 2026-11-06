import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-comptabilite-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './comptabilite-layout.component.html',
  styleUrl: './comptabilite-layout.component.scss',
})
export class ComptabiliteLayoutComponent {
  readonly navItems = [
    { route: '/comptabilite', label: 'Tableau de bord', exact: true },
    { route: '/comptabilite/frais', label: 'Frais', exact: false },
    { route: '/comptabilite/factures', label: 'Factures', exact: false },
    { route: '/comptabilite/paiements', label: 'Paiements', exact: false },
    { route: '/comptabilite/validations', label: 'Validations', exact: false },
    { route: '/comptabilite/soldes', label: 'Soldes', exact: false },
    { route: '/comptabilite/depenses', label: 'Dépenses', exact: false },
    { route: '/comptabilite/rapports', label: 'Rapports', exact: false },
    { route: '/comptabilite/personnel', label: 'Personnel', exact: false },
    { route: '/comptabilite/paie', label: 'Paie', exact: false },
    { route: '/comptabilite/journal', label: 'Journal', exact: false },
    { route: '/comptabilite/bilan', label: 'Bilan', exact: false },
  ] as const;
}

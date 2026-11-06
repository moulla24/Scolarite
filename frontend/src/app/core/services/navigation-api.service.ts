import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { NavigationItem } from '../models/navigation-item.model';
import { UserProfile } from '../models/user.model';
import { NavigationProvider } from './interfaces/navigation-provider.interface';

const ADMIN_MENU: readonly NavigationItem[] = [
  { label: 'Tableau de bord', icon: 'dashboard', route: '/dashboard', exact: true },
  {
    label: 'Élèves & familles',
    icon: 'family_restroom',
    children: [
      { label: 'Parents', icon: 'supervisor_account', route: '/parents' },
      { label: 'Élèves', icon: 'school', route: '/students' },
    ],
  },
  {
    label: 'Suivi scolaire',
    icon: 'fact_check',
    children: [
      { label: 'Notes', icon: 'grade', route: '/notes' },
      { label: 'Absences', icon: 'event_busy', route: '/absences' },
    ],
  },
  {
    label: 'Organisation',
    icon: 'account_tree',
    children: [
      { label: 'Enseignants', icon: 'person', route: '/enseignants' },
      { label: 'Classes', icon: 'groups', route: '/classes' },
      { label: 'Emploi du temps', icon: 'calendar_month', route: '/emploi-du-temps' },
      { label: 'Maquettes pédagogiques', icon: 'menu_book', route: '/maquettes' },
    ],
  },
  { label: 'Comptabilité', icon: 'account_balance', route: '/comptabilite', exact: true },
  {
    label: 'Config & Plan',
    icon: 'folder',
    children: [
      { label: 'Paramètres', icon: 'settings', route: '/config-plan/parametres', exact: true },
      {
        label: 'Planification',
        icon: 'event_note',
        route: '/config-plan/planification',
        exact: true,
      },
    ],
  },
];

const COMPTABLE_MENU: readonly NavigationItem[] = [
  { label: 'Comptabilité', icon: 'account_balance', route: '/comptabilite', exact: true },
  { label: 'Factures', icon: 'receipt_long', route: '/comptabilite/factures' },
  { label: 'Paiements scolaires', icon: 'payments', route: '/comptabilite/paiements' },
  { label: 'Personnel de service', icon: 'groups', route: '/comptabilite/personnel' },
  { label: 'Paie mensuelle', icon: 'account_balance_wallet', route: '/comptabilite/paie' },
  { label: 'Journal', icon: 'menu_book', route: '/comptabilite/journal' },
  { label: 'Bilan', icon: 'assessment', route: '/comptabilite/bilan' },
];

function filterMenuByRoutes(
  items: readonly NavigationItem[],
  excludedRoutes: readonly string[]
): readonly NavigationItem[] {
  return items
    .map((item) => {
      if (item.children?.length) {
        const children = item.children.filter(
          (child) => !child.route || !excludedRoutes.includes(child.route)
        );
        if (!children.length) {
          return null;
        }
        return { ...item, children };
      }
      if (item.route && excludedRoutes.includes(item.route)) {
        return null;
      }
      return item;
    })
    .filter((item): item is NavigationItem => item !== null);
}

@Injectable({ providedIn: 'root' })
export class NavigationApiService implements NavigationProvider {
  private readonly auth = inject(AuthService);

  getMenuItems(): Observable<readonly NavigationItem[]> {
    return this.auth.currentUser$.pipe(
      map((user) => {
        const role = user?.role?.toUpperCase() ?? '';
        if (role === 'COMPTABLE') {
          return COMPTABLE_MENU;
        }
        if (role === 'ADMIN') {
          return ADMIN_MENU;
        }
        return filterMenuByRoutes(ADMIN_MENU, ['/comptabilite']);
      })
    );
  }

  getCurrentUser(): Observable<UserProfile> {
    return this.auth.currentUser$.pipe(
      map((user) => {
        if (!user) {
          return { name: 'Utilisateur', role: '', avatarInitials: '?' };
        }
        const name = [user.prenom, user.nom].filter(Boolean).join(' ').trim() || user.email;
        return {
          name,
          role: this.formatRole(user.role),
          avatarInitials: user.avatarInitials || '?',
        };
      })
    );
  }

  private formatRole(role: string): string {
    switch (role?.toUpperCase()) {
      case 'ADMIN':
        return 'Administrateur';
      case 'ENSEIGNANT':
        return 'Enseignant';
      case 'COMPTABLE':
        return 'Comptable';
      default:
        return role || '';
    }
  }
}

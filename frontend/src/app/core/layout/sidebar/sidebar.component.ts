import { AsyncPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  output,
  signal,
} from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';
import { APP_NAME, NAVIGATION_PROVIDER } from '../../constants/app.constants';
import { NavigationItem } from '../../models/navigation-item.model';
import { NavigationProvider } from '../../services/interfaces/navigation-provider.interface';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [AsyncPipe, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidebarComponent {
  private readonly navigationProvider = inject<NavigationProvider>(NAVIGATION_PROVIDER);
  private readonly router = inject(Router);

  readonly logout = output<void>();

  readonly appName = APP_NAME;
  readonly menuItems$ = this.navigationProvider.getMenuItems();
  readonly user$ = this.navigationProvider.getCurrentUser();

  /** État manuel ouvert/fermé par dossier (label → true/false) */
  private readonly folderExpanded = signal<Readonly<Record<string, boolean>>>({});

  constructor() {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.autoExpandActiveFolders();
    });
    this.autoExpandActiveFolders();
  }

  onLogout(): void {
    this.logout.emit();
  }

  trackItem(item: NavigationItem): string {
    return item.route ?? item.label;
  }

  isFolderOpen(item: NavigationItem): boolean {
    const manual = this.folderExpanded()[item.label];
    if (manual !== undefined) {
      return manual;
    }
    return this.isFolderActive(item);
  }

  toggleFolder(item: NavigationItem): void {
    const next = !this.isFolderOpen(item);
    this.folderExpanded.update((state) => ({ ...state, [item.label]: next }));
  }

  private isFolderActive(item: NavigationItem): boolean {
    if (!item.children?.length) return false;
    const url = this.router.url;
    return item.children.some((child) => child.route && url.startsWith(child.route));
  }

  private autoExpandActiveFolders(): void {
    this.navigationProvider.getMenuItems().subscribe((items) => {
      const updates: Record<string, boolean> = {};
      for (const item of items) {
        if (item.children?.length && this.isFolderActive(item)) {
          if (this.folderExpanded()[item.label] === undefined) {
            updates[item.label] = true;
          }
        }
      }
      if (Object.keys(updates).length > 0) {
        this.folderExpanded.update((state) => ({ ...state, ...updates }));
      }
    });
  }
}

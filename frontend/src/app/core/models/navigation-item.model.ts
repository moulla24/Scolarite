export interface NavigationItem {
  readonly label: string;
  readonly icon: string;
  readonly route?: string;
  readonly exact?: boolean;
  /** Sous-pages (dossier → fichiers dans la sidebar) */
  readonly children?: readonly NavigationItem[];
}

export interface MaquetteCreatePayload {
  titre: string;
  matiereId: number;
  niveauId: number;
  description: string | null;
  format: string;
  fichierUrl: string | null;
  nbPages: number;
  dateMaj: string | null;
}

export interface MaquetteCreateFormModel {
  titre: string;
  matiereId: string;
  niveauId: string;
  description: string;
  format: string;
  fichierUrl: string;
  nbPages: string;
  dateMaj: string;
  fichier?: File | null;
}

export const emptyMaquetteCreateForm = (): MaquetteCreateFormModel => ({
  titre: '',
  matiereId: '',
  niveauId: '',
  description: '',
  format: 'PDF',
  fichierUrl: '',
  nbPages: '0',
  dateMaj: '',
  fichier: null,
});

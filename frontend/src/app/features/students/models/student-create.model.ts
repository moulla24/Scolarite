export interface StudentCreatePayload {
  matricule: string;
  nom: string;
  prenom: string;
  dateNaissance: string | null;
  sexe: string | null;
  classeId: number | null;
  ecoleId: number;
}

export interface StudentCreateFormModel {
  matricule: string;
  nom: string;
  prenom: string;
  dateNaissance: string;
  sexe: string;
  niveauId: string;
  classeId: string;
}

export const emptyStudentCreateForm = (): StudentCreateFormModel => ({
  matricule: '',
  nom: '',
  prenom: '',
  dateNaissance: '',
  sexe: '',
  niveauId: 'all',
  classeId: '',
});

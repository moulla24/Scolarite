export interface ClassroomCreateFormModel {
  niveauId: string;
  nom: string;
  salle: string;
  capacite: string;
}

export const emptyClassroomCreateForm = (): ClassroomCreateFormModel => ({
  niveauId: '',
  nom: '',
  salle: '',
  capacite: '30',
});

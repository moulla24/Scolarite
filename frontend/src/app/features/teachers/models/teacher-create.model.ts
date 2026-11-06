export interface TeacherCreateFormModel {
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  specialite: string;
  dateEmbauche: string;
  actif: boolean;
}

export const emptyTeacherCreateForm = (): TeacherCreateFormModel => ({
  nom: '',
  prenom: '',
  email: '',
  telephone: '',
  specialite: '',
  dateEmbauche: '',
  actif: true,
});

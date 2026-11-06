export interface ComptableCreateFormModel {
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  matricule: string;
  dateEmbauche: string;
  actif: boolean;
}

export function emptyComptableCreateForm(): ComptableCreateFormModel {
  return {
    nom: '',
    prenom: '',
    email: '',
    telephone: '',
    matricule: '',
    dateEmbauche: '',
    actif: true,
  };
}

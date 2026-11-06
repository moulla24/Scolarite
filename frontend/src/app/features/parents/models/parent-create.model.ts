export interface ParentCreatePayload {
  nom: string;
  prenom: string | null;
  email: string | null;
  telephone: string;
  adresse: string | null;
}

export interface ParentCreateFormModel {
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  adresse: string;
}

export const emptyParentCreateForm = (): ParentCreateFormModel => ({
  nom: '',
  prenom: '',
  email: '',
  telephone: '',
  adresse: '',
});

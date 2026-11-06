/** Découpe « prénom nom » (format backend) en champs séparés. */
export function splitNomComplet(nomComplet: string): { prenom: string; nom: string } {
  const parts = nomComplet.trim().split(/\s+/).filter(Boolean);
  if (parts.length <= 1) {
    return { prenom: '', nom: parts[0] ?? '' };
  }
  return { prenom: parts[0], nom: parts.slice(1).join(' ') };
}

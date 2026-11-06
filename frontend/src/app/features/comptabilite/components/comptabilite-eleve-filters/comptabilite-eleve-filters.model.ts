export interface ComptabiliteEleveFilterState {
  niveauId: string;
  classeId: string;
  eleveId: string;
  eleveSearch: string;
}

export const emptyEleveFilters = (): ComptabiliteEleveFilterState => ({
  niveauId: '',
  classeId: '',
  eleveId: '',
  eleveSearch: '',
});

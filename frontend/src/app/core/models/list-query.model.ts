export interface ParentsListQuery {
  readonly search?: string;
  readonly niveauId?: string;
  readonly statutCompte?: string;
}

export interface StudentsListQuery {
  readonly search?: string;
  readonly niveauId?: string;
  readonly classeId?: string;
}

export interface ClassroomsListQuery {
  readonly search?: string;
  readonly niveauId?: string;
}

export interface MaquettesListQuery {
  readonly search?: string;
  readonly matiereId?: string;
  readonly niveauId?: string;
}

export interface TeachersListQuery {
  readonly search?: string;
}

export interface GradesListQuery {
  readonly classeId?: string;
  readonly niveauId?: string;
}

export interface ScheduleListQuery {
  readonly classeId?: string;
  readonly semaineId?: string;
}

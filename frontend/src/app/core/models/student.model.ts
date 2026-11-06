export type StudentAccountStatus = 'up_to_date' | 'debt';

export interface Student {
  readonly id: string;
  readonly fullName: string;
  readonly initials: string;
  readonly matricule: string;
  readonly className: string;
  readonly classId?: string;
  readonly level: string;
  readonly levelId?: string;
  readonly balance: number;
  readonly status: StudentAccountStatus;
}

export type AccountStatus = 'up_to_date' | 'debt';

export interface ParentStudentSummary {
  readonly name: string;
}

export interface Parent {
  readonly id: string;
  readonly fullName: string;
  readonly initials: string;
  readonly phone: string;
  readonly email: string;
  readonly students: readonly ParentStudentSummary[];
  readonly balance: number;
  readonly status: AccountStatus;
  readonly schoolLevel: string;
}

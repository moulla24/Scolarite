export interface GradeRecord {
  readonly id: string;
  readonly fullName: string;
  readonly className: string;
  readonly classId?: string;
  readonly level: string;
  readonly levelId?: string;
  readonly subjectCount: number;
}

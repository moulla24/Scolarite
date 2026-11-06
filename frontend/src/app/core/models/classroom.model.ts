export interface Classroom {
  readonly id: string;
  readonly name: string;
  readonly level: string;
  readonly levelId?: string;
  readonly teacherName: string;
  readonly studentCount: number;
  readonly capacity: number;
  readonly room: string;
}

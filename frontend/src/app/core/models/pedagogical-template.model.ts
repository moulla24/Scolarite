export type TemplateFormat = 'pdf' | 'docx' | 'pptx';

export interface PedagogicalTemplate {
  readonly id: string;
  readonly title: string;
  readonly subject: string;
  readonly subjectId?: string;
  readonly level: string;
  readonly levelId?: string;
  readonly description: string;
  readonly format: TemplateFormat;
  readonly updatedAt: string;
  readonly pageCount: number;
  readonly fichierUrl?: string | null;
}

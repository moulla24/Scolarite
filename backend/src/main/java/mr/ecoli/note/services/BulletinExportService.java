package mr.ecoli.note.services;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import mr.ecoli.note.dtos.BulletinMatiereDto;
import mr.ecoli.note.dtos.BulletinTrimestreDto;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class BulletinExportService {

    private static final Color HEADER_BG = new Color(41, 98, 255);
    private static final Color ACCENT_BG = new Color(232, 240, 254);
    private static final byte[] HEADER_RGB = new byte[] {(byte) 41, (byte) 98, (byte) 255};

    public byte[] toPdf(BulletinTrimestreDto bulletin) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 48, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.WHITE);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(HEADER_BG);
            headerCell.setBorderWidth(0);
            headerCell.setPadding(14);
            headerCell.addElement(new Paragraph(bulletin.ecole(), titleFont));
            headerCell.addElement(new Paragraph("BULLETIN SCOLAIRE — " + bulletin.trimestreLibelle(), subtitleFont));
            header.addCell(headerCell);
            document.add(header);
            document.add(new Paragraph(" "));

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.setSpacingBefore(4);
            addMetaRow(meta, "Élève", bulletin.nomComplet(), labelFont, valueFont);
            addMetaRow(meta, "Classe", bulletin.classe(), labelFont, valueFont);
            addMetaRow(meta, "Année scolaire", bulletin.anneeScolaire(), labelFont, valueFont);
            document.add(meta);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[] {3.2f, 1.2f, 1.4f, 1.6f, 1.4f});
            addHeaderCell(table, "Matière", headerFont);
            addHeaderCell(table, "Coef.", headerFont);
            addHeaderCell(table, "Devoir", headerFont);
            addHeaderCell(table, "Composition", headerFont);
            addHeaderCell(table, "Moyenne", headerFont);

            for (BulletinMatiereDto ligne : bulletin.matieres()) {
                addBodyCell(table, ligne.matiere(), cellFont, false);
                addBodyCell(table, formatDecimal(ligne.coefficient()), cellFont, false);
                addBodyCell(table, formatDecimal(ligne.noteDevoir()), cellFont, false);
                addBodyCell(table, formatDecimal(ligne.noteComposition()), cellFont, false);
                addBodyCell(table, formatDecimal(ligne.moyenne()), cellFont, true);
            }
            document.add(table);
            document.add(new Paragraph(" "));

            PdfPTable footer = new PdfPTable(1);
            footer.setWidthPercentage(100);
            PdfPCell moyenneCell = new PdfPCell(
                    new Phrase(
                            "Moyenne générale : " + formatDecimal(bulletin.moyenneGenerale()) + " / 20",
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, HEADER_BG)));
            moyenneCell.setBackgroundColor(ACCENT_BG);
            moyenneCell.setPadding(10);
            moyenneCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footer.addCell(moyenneCell);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Échec de génération du PDF", ex);
        }
    }

    public byte[] toExcel(BulletinTrimestreDto bulletin) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bulletin");
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(new XSSFColor(HEADER_RGB, null));
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = borderedStyle(workbook);
            headerStyle.setFillForegroundColor(new XSSFColor(HEADER_RGB, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);

            CellStyle bodyStyle = borderedStyle(workbook);
            CellStyle accentStyle = borderedStyle(workbook);
            accentStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            accentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle boldStyle = borderedStyle(workbook);
            boldStyle.setFont(boldFont);
            boldStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            boldStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(bulletin.ecole() + " — Bulletin " + bulletin.trimestreLibelle());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));

            rowIdx++;
            rowIdx = writeMetaRow(sheet, rowIdx, "Élève", bulletin.nomComplet(), boldStyle, bodyStyle);
            rowIdx = writeMetaRow(sheet, rowIdx, "Classe", bulletin.classe(), boldStyle, bodyStyle);
            rowIdx = writeMetaRow(sheet, rowIdx, "Année scolaire", bulletin.anneeScolaire(), boldStyle, bodyStyle);
            rowIdx++;

            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"Matière", "Coef.", "Devoir", "Composition", "Moyenne"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (BulletinMatiereDto ligne : bulletin.matieres()) {
                Row row = sheet.createRow(rowIdx++);
                writeDecimalCell(row, 0, ligne.matiere(), bodyStyle);
                writeDecimalCell(row, 1, formatDecimal(ligne.coefficient()), bodyStyle);
                writeDecimalCell(row, 2, formatDecimal(ligne.noteDevoir()), bodyStyle);
                writeDecimalCell(row, 3, formatDecimal(ligne.noteComposition()), bodyStyle);
                writeDecimalCell(row, 4, formatDecimal(ligne.moyenne()), accentStyle);
            }

            rowIdx++;
            Row moyenneRow = sheet.createRow(rowIdx);
            Cell moyenneLabel = moyenneRow.createCell(0);
            moyenneLabel.setCellValue("Moyenne générale");
            moyenneLabel.setCellStyle(boldStyle);
            Cell moyenneValue = moyenneRow.createCell(4);
            moyenneValue.setCellValue(formatDecimal(bulletin.moyenneGenerale()) + " / 20");
            moyenneValue.setCellStyle(boldStyle);

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Échec de génération Excel", ex);
        }
    }

    private static CellStyle borderedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static int writeMetaRow(
            Sheet sheet, int rowIdx, String label, String value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowIdx++);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
        return rowIdx;
    }

    private static void writeDecimalCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void addMetaRow(
            PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorderWidth(0.5f);
        labelCell.setBackgroundColor(ACCENT_BG);
        labelCell.setPadding(6);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorderWidth(0.5f);
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private static void addBodyCell(PdfPTable table, String text, Font font, boolean highlight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        if (highlight) {
            cell.setBackgroundColor(ACCENT_BG);
        }
        table.addCell(cell);
    }

    private static String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}

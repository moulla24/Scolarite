package mr.ecoli.maquette.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mr.ecoli.dao.entities.MaquetteClasse;
import mr.ecoli.dao.entities.MaquetteMatiere;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.maquette.dtos.MaquetteChapitreRequest;
import mr.ecoli.maquette.dtos.MaquetteGrilleDto;
import mr.ecoli.maquette.dtos.MaquetteMatiereDetailDto;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
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
public class MaquetteGrilleExportService {

    private static final byte[] HEADER_RGB = new byte[] {(byte) 41, (byte) 98, (byte) 255};
    private static final DataFormatter CELL_FORMATTER = new DataFormatter();

    public byte[] toExcel(MaquetteGrilleDto grille, List<MaquetteMatiereDetailDto> details) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Maquette");
            CellStyle titleStyle = headerStyle(workbook, (short) 14);
            CellStyle headerStyle = headerStyle(workbook, (short) 11);
            CellStyle bodyStyle = borderedStyle(workbook);
            CellStyle accentStyle = borderedStyle(workbook);
            accentStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            accentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Maquette pédagogique — " + grille.classe() + " (" + grille.niveau() + ")");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));
            rowIdx++;

            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"Matière", "Volume horaire (h/sem)", "N°", "Chapitre", "Description"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (MaquetteMatiereDetailDto detail : details) {
                if (detail.chapitres().isEmpty()) {
                    Row row = sheet.createRow(rowIdx++);
                    writeCell(row, 0, detail.matiere(), accentStyle);
                    writeCell(row, 1, formatDecimal(detail.volumeHoraire()), bodyStyle);
                    writeCell(row, 2, "", bodyStyle);
                    writeCell(row, 3, "", bodyStyle);
                    writeCell(row, 4, "", bodyStyle);
                } else {
                    for (var ch : detail.chapitres()) {
                        Row row = sheet.createRow(rowIdx++);
                        writeCell(row, 0, detail.matiere(), accentStyle);
                        writeCell(row, 1, formatDecimal(detail.volumeHoraire()), bodyStyle);
                        writeCell(row, 2, String.valueOf(ch.ordre()), bodyStyle);
                        writeCell(row, 3, ch.titre(), bodyStyle);
                        writeCell(row, 4, ch.description() != null ? ch.description() : "", bodyStyle);
                    }
                }
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Échec export maquette", ex);
        }
    }

    public byte[] templateExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Maquette");
            CellStyle titleStyle = headerStyle(workbook, (short) 14);
            CellStyle headerStyle = headerStyle(workbook, (short) 11);
            CellStyle bodyStyle = borderedStyle(workbook);
            CellStyle accentStyle = borderedStyle(workbook);
            accentStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            accentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Maquette pédagogique — Modèle à remplir");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));
            rowIdx++;

            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"Matière", "Volume horaire (h/sem)", "N°", "Chapitre", "Description"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            String[][] examples = {
                {"Français", "5", "1", "Chapitre 1 — Introduction", "Notions de base"},
                {"Français", "5", "2", "Chapitre 2 — Approfondissement", ""},
                {"Mathématiques", "5", "1", "Chapitre 1 — Nombres", ""},
                {"Mathématiques", "5", "2", "Chapitre 2 — Géométrie", ""},
            };
            for (String[] ex : examples) {
                Row row = sheet.createRow(rowIdx++);
                writeCell(row, 0, ex[0], accentStyle);
                writeCell(row, 1, ex[1], bodyStyle);
                writeCell(row, 2, ex[2], bodyStyle);
                writeCell(row, 3, ex[3], bodyStyle);
                writeCell(row, 4, ex[4], bodyStyle);
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Échec génération modèle maquette", ex);
        }
    }

    public void importExcel(MaquetteClasse maquette, byte[] content, MaquetteGrilleService grilleService) {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new BadRequestException("Le fichier Excel est vide.");
            }
            Sheet sheet = workbook.getSheetAt(0);
            int startRow = findDataStartRow(sheet);
            if (startRow < 0) {
                throw new BadRequestException(
                        "En-têtes introuvables. Utilisez le modèle : Matière, Volume horaire, N°, Chapitre, Description.");
            }

            Map<String, List<ImportRow>> byMatiere = new LinkedHashMap<>();
            BigDecimal lastVolume = BigDecimal.valueOf(3);

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String matiere = cellStr(row, 0);
                if (matiere.isBlank() || isSkippedMatiere(matiere)) {
                    continue;
                }
                String volStr = cellStr(row, 1);
                if (!volStr.isBlank()) {
                    try {
                        lastVolume = new BigDecimal(volStr.replace(',', '.'));
                    } catch (NumberFormatException ignored) {
                    }
                }
                String chapitre = cellStr(row, 3);
                if (chapitre.isBlank() || "Chapitre".equalsIgnoreCase(chapitre)) {
                    continue;
                }
                short ordre = parseOrdre(cellStr(row, 2));
                String desc = cellStr(row, 4);
                byMatiere.computeIfAbsent(matiere, k -> new ArrayList<>())
                        .add(new ImportRow(lastVolume, ordre, chapitre, desc));
            }

            if (byMatiere.isEmpty()) {
                throw new BadRequestException(
                        "Aucune ligne valide trouvée. Renseignez au moins une matière et un chapitre.");
            }

            for (Map.Entry<String, List<ImportRow>> entry : byMatiere.entrySet()) {
                BigDecimal volume = entry.getValue().get(0).volume();
                MaquetteMatiere mm = grilleService.findOrCreateMatiereLine(maquette, entry.getKey(), volume);
                List<MaquetteChapitreRequest> chapitres = entry.getValue().stream()
                        .map(r -> new MaquetteChapitreRequest(r.ordre(), r.titre(), r.description()))
                        .toList();
                grilleService.replaceChapitresForImport(mm, chapitres);
            }
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Import maquette invalide : vérifiez le format du fichier.");
        }
    }

    private static int findDataStartRow(Sheet sheet) {
        int last = Math.min(sheet.getLastRowNum(), 20);
        for (int i = 0; i <= last; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String first = cellStr(row, 0);
            if ("Matière".equalsIgnoreCase(first.trim())) {
                return i + 1;
            }
        }
        return -1;
    }

    private static boolean isSkippedMatiere(String matiere) {
        String m = matiere.trim();
        return m.startsWith("Maquette") || "Matière".equalsIgnoreCase(m);
    }

    private static short parseOrdre(String raw) {
        try {
            return (short) Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private static String cellStr(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            return "";
        }
        return CELL_FORMATTER.formatCellValue(cell).trim();
    }

    private static void writeCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static CellStyle headerStyle(Workbook workbook, short fontSize) {
        CellStyle style = borderedStyle(workbook);
        style.setFillForegroundColor(new XSSFColor(HEADER_RGB, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints(fontSize);
        style.setFont(font);
        return style;
    }

    private static CellStyle borderedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static String formatDecimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private record ImportRow(BigDecimal volume, short ordre, String titre, String description) {}
}

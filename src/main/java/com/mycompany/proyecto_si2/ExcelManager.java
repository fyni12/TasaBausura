package com.mycompany.proyecto_si2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public final class ExcelManager implements AutoCloseable {

    private final Workbook workbook;
    private final Sheet sheet;
    private final Map<String, Integer> headers;
    private final DataFormatter formatter = new DataFormatter(Locale.ROOT);

    public ExcelManager(Path excelPath, String sheetName) throws IOException {
        try (InputStream in = Files.newInputStream(excelPath)) {
            this.workbook = WorkbookFactory.create(in);
        } catch (Exception e) {
            throw new IOException("No se pudo abrir el libro Excel", e);
        }

        this.sheet = workbook.getSheet(sheetName);
        if (this.sheet == null) {
            throw new IllegalArgumentException("No existe la hoja: " + sheetName);
        }

        this.headers = readHeaders(sheet.getRow(0));
    }

    private Map<String, Integer> readHeaders(Row headerRow) {
        if (headerRow == null) {
            throw new IllegalStateException("La hoja no tiene fila de cabeceras");
        }

        Map<String, Integer> map = new LinkedHashMap<>();
        for (Cell cell : headerRow) {
            String value = formatter.formatCellValue(cell);
            if (value != null && !value.trim().isEmpty()) {
                map.put(normalizeHeader(value), cell.getColumnIndex());
            }
        }
        return map;
    }

    private String normalizeHeader(String text) {
        return text == null ? null : text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private int columnIndex(ExcelColumn column) {
        Integer idx = headers.get(normalizeHeader(column.getHeader()));
        if (idx == null) {
            throw new IllegalArgumentException("No existe la columna: " + column.getHeader());
        }
        return idx;
    }

    public List<Row> getDataRows() {
        List<Row> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                rows.add(row);
            }
        }
        return rows;
    }

    public boolean isEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            if (i < 0) {
                continue;
            }
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int getExcelRowId(Row row) {
        return row.getRowNum() + 1;
    }

    public String getString(Row row, ExcelColumn column) {
        Cell cell = row.getCell(columnIndex(column), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell);
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    public LocalDate getDate(Row row, ExcelColumn column) {
        Cell cell = row.getCell(columnIndex(column), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        String text = formatter.formatCellValue(cell);
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        return LocalDate.parse(text.trim());
    }

    public Integer getInt(Row row, ExcelColumn column) {
        Cell cell = row.getCell(columnIndex(column), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }

        String text = formatter.formatCellValue(cell);
        if (text == null) {
            return null;
        }

        text = text.trim();
        if (text.isEmpty()) {
            return null;
        }

        return Integer.parseInt(text);
    }
    public Double getDouble(Row row, ExcelColumn column) {
    Cell cell = row.getCell(columnIndex(column), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null) {
        return null;
    }

    if (cell.getCellType() == CellType.NUMERIC) {
        return cell.getNumericCellValue();
    }

    String text = formatter.formatCellValue(cell);
    if (text == null) {
        return null;
    }

    text = text.trim();
    if (text.isEmpty()) {
        return null;
    }

    return Double.parseDouble(text);
}

    public void setString(Row row, ExcelColumn column, String value) {
        Cell cell = row.getCell(columnIndex(column), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value == null ? "" : value);
    }

    public void save(Path outputPath) throws IOException {
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        try (OutputStream out = Files.newOutputStream(outputPath)) {
            workbook.write(out);
        }
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}

package com.mycompany.proyecto_si2.infra.excel;

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
/**
 * Clase de infraestructura encargada de gestionar la lectura, escritura y guardado de datos en un fichero Excel.
 * Su función principal es abrir una hoja concreta de un libro, localizar sus columnas mediante las cabeceras
 * y proporcionar métodos de utilidad para obtener y modificar valores de distintos tipos en las filas del Excel.
 *
 * Funciones de la clase:
 *
 * - ExcelManager(Path excelPath, String sheetName):
 *   Constructor que abre el fichero Excel indicado, carga el libro en memoria, selecciona la hoja
 *   especificada por nombre y construye el mapa interno de cabeceras para facilitar el acceso a las columnas.
 *   Si el fichero no puede abrirse o la hoja no existe, lanza la excepción correspondiente.
 *
 * - readHeaders(Row headerRow):
 *   Método auxiliar privado que recorre la fila de cabeceras de la hoja y genera un mapa que asocia
 *   cada nombre de columna normalizado con su índice dentro de la hoja.
 *
 * - normalizeHeader(String text):
 *   Método auxiliar privado que normaliza el texto de una cabecera eliminando espacios sobrantes,
 *   convirtiéndolo a minúsculas y suprimiendo separaciones internas para facilitar las búsquedas.
 *
 * - columnIndex(ExcelColumn column):
 *   Método auxiliar privado que obtiene el índice de la columna asociada a un valor del enumerado
 *   ExcelColumn. Si la cabecera no existe en la hoja, lanza una excepción.
 *
 * - getDataRows():
 *   Devuelve una lista con todas las filas de datos de la hoja, excluyendo la fila de cabeceras.
 *
 * - isEmpty(Row row):
 *   Comprueba si una fila está vacía. Para ello revisa todas sus celdas y determina si alguna contiene
 *   información no vacía.
 *
 * - getExcelRowId(Row row):
 *   Devuelve el número de fila tal y como se identifica en Excel, ajustando el índice interno para que
 *   coincida con la numeración habitual del usuario.
 *
 * - getString(Row row, ExcelColumn column):
 *   Obtiene el valor de una celda como texto a partir de una fila y una columna concreta. Si la celda
 *   no existe o está vacía, devuelve null.
 *
 * - getDate(Row row, ExcelColumn column):
 *   Obtiene el valor de una celda como LocalDate. Soporta tanto celdas de tipo fecha en Excel como
 *   fechas expresadas en texto, devolviendo null si no hay contenido.
 *
 * - getInt(Row row, ExcelColumn column):
 *   Obtiene el valor de una celda como entero. Puede leer tanto celdas numéricas como texto convertible
 *   a entero, devolviendo null si no hay dato.
 *
 * - getDouble(Row row, ExcelColumn column):
 *   Obtiene el valor de una celda como número decimal de tipo Double. Puede leer tanto celdas numéricas
 *   como texto convertible a decimal, devolviendo null si el contenido está vacío.
 *
 * - setString(Row row, ExcelColumn column, String value):
 *   Escribe un valor de texto en la celda correspondiente a la fila y columna indicadas. Si la celda
 *   no existe, la crea automáticamente.
 *
 * - save(Path outputPath):
 *   Guarda en disco el libro Excel con los cambios realizados. Si el directorio de destino no existe,
 *   lo crea antes de escribir el fichero.
 *
 * - close():
 *   Cierra el libro Excel liberando los recursos asociados. Este método permite usar la clase dentro
 *   de estructuras try-with-resources.
 */
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

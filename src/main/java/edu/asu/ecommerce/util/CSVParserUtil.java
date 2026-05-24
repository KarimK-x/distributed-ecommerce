package edu.asu.ecommerce.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for parsing CSV files containing product (item) data.
 *
 * Expected CSV header (order does not matter):
 *   itemName, description, price, quantity, categoryId, brandId, email
 *
 * Features:
 *  - Handles quoted fields (e.g., "Gaming Laptop, Pro Edition")
 *  - Skips blank lines
 *  - Validates that all required headers are present
 *  - Reports the row number on field-count mismatches
 */
public class CSVParserUtil {

    /** Required column names that must appear in the CSV header row. */
    public static final List<String> REQUIRED_HEADERS = Arrays.asList(
            "itemName", "description", "price", "quantity", "categoryId", "brandId", "email"
    );

    /**
     * Parses CSV text into a list of row maps keyed by header name.
     *
     * @param csvContent raw CSV string (may use \r\n or \n line endings)
     * @return list of maps, one per data row; each map keys header → value
     * @throws Exception if the content is blank, has no data rows,
     *                   is missing required headers, or has row-length mismatches
     */
    public static List<Map<String, String>> parse(String csvContent) throws Exception {
        if (csvContent == null || csvContent.isBlank()) {
            throw new Exception("CSV content is empty");
        }

        // Split on newlines (handles both \r\n and \n)
        String[] lines = csvContent.split("\\r?\\n", -1);

        // Find first non-blank line as header
        int headerLineIndex = -1;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].trim().isBlank()) {
                headerLineIndex = i;
                break;
            }
        }
        if (headerLineIndex < 0) {
            throw new Exception("CSV has no content");
        }

        String[] headers = parseLine(lines[headerLineIndex]);
        for (int i = 0; i < headers.length; i++) {
            headers[i] = headers[i].trim();
        }
        validateHeaders(headers);

        List<Map<String, String>> rows = new ArrayList<>();
        int dataRowNumber = 0;

        for (int i = headerLineIndex + 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().isBlank()) {
                continue; // skip empty lines
            }
            dataRowNumber++;
            String[] values = parseLine(line);

            if (values.length != headers.length) {
                throw new Exception(
                        "Row " + dataRowNumber + " has " + values.length +
                        " fields but header has " + headers.length + " columns"
                );
            }

            Map<String, String> row = new LinkedHashMap<>();
            for (int j = 0; j < headers.length; j++) {
                row.put(headers[j], values[j].trim());
            }
            rows.add(row);
        }

        if (rows.isEmpty()) {
            throw new Exception("CSV must contain at least one data row after the header");
        }

        return rows;
    }

    /**
     * Validates that all required column headers are present.
     * Comparison is case-sensitive to match field names used in services.
     */
    private static void validateHeaders(String[] headers) throws Exception {
        Set<String> headerSet = new HashSet<>(Arrays.asList(headers));
        List<String> missing = new ArrayList<>();
        for (String required : REQUIRED_HEADERS) {
            if (!headerSet.contains(required)) {
                missing.add(required);
            }
        }
        if (!missing.isEmpty()) {
            throw new Exception("CSV is missing required column(s): " + String.join(", ", missing));
        }
    }

    /**
     * Parses a single CSV line into tokens.
     * Handles fields enclosed in double-quotes (including commas inside quotes).
     * A literal double-quote inside a quoted field is represented as two double-quotes ("").
     */
    static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Check for escaped quote: ""
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // skip the second quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        fields.add(current.toString()); // last field
        return fields.toArray(new String[0]);
    }
}

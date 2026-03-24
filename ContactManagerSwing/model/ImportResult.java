package model;

/**
 * ImportResult Model Class
 * 
 * Holds the results of CSV import operation.
 * Tracks imported, skipped, and invalid rows.
 */
public class ImportResult {
    private int imported;
    private int duplicatesSkipped;
    private int invalidRows;

    public ImportResult() {
        this.imported = 0;
        this.duplicatesSkipped = 0;
        this.invalidRows = 0;
    }

    public void incrementImported() {
        this.imported++;
    }

    public void incrementDuplicates() {
        this.duplicatesSkipped++;
    }

    public void incrementInvalid() {
        this.invalidRows++;
    }

    public int getImported() {
        return imported;
    }

    public int getDuplicatesSkipped() {
        return duplicatesSkipped;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public int getTotal() {
        return imported + duplicatesSkipped + invalidRows;
    }

    @Override
    public String toString() {
        return String.format("Import Results:\nImported: %d\nDuplicates Skipped: %d\nInvalid Rows: %d\nTotal Processed: %d",
                             imported, duplicatesSkipped, invalidRows, getTotal());
    }
}

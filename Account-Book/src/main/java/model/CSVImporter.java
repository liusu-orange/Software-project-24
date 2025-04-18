package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {
    public List<Entry> importCSV(String filePath) {
        List<Entry> entries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header line
            br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                // Assumes CSV columns: Date, Amount, Category, Description
                String date = fields[0];
                double amount = Double.parseDouble(fields[1]);
                String category = fields[2];
                String description = fields[3];
                
                Entry entry = new Entry(date, amount, category, description);
                entries.add(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entries;
    }
}

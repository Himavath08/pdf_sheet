package com.metaextract.pdf;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
public class PdfTextExtractor {
    public String extract(File pdfFile) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            PDFTextStripper s = new PDFTextStripper();
            s.setSortByPosition(true);
            s.setAddMoreFormatting(false);
            return clean(s.getText(doc));
        }
    }
    private String clean(String raw) {
        String[] lines = raw.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        int blank = 0;
        for (String line : lines) {
            String t = line.stripTrailing().replace("\f","").stripTrailing();
            if (t.isBlank()) { if (++blank==1) sb.append('\n'); }
            else { blank=0; sb.append(t).append('\n'); }
        }
        return sb.toString().strip();
    }
}

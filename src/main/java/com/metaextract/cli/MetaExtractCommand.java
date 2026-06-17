package com.metaextract.cli;
import com.metaextract.excel.ExcelWriter;
import com.metaextract.extractor.RuleBasedExtractor;
import com.metaextract.mapper.MetadataResult;
import com.metaextract.pdf.PdfTextExtractor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.File;
import java.util.concurrent.Callable;
@Command(name="metaextract",mixinStandardHelpOptions=true,version="1.0.0",description="Extracts metadata from a PDF and exports to Excel. No API key required.")
public class MetaExtractCommand implements Callable<Integer> {
    @Option(names={"-p","--pdf"},required=true,description="Path to source PDF file") private File pdfFile;
    @Option(names={"-t","--template"},required=true,description="Path to metadata template xlsx") private File templateFile;
    @Option(names={"-o","--out"},required=true,description="Path for output Excel file") private File outputFile;
    @Override public Integer call() {
        try {
            if (!pdfFile.exists()) throw new IllegalArgumentException("PDF not found: "+pdfFile);
            if (!templateFile.exists()) throw new IllegalArgumentException("Template not found: "+templateFile);
            System.out.println("[1/3] Extracting text from PDF: "+pdfFile.getName());
            String txt = new PdfTextExtractor().extract(pdfFile);
            System.out.println("      Extracted "+txt.length()+" characters.");
            System.out.println("[2/3] Parsing metadata...");
            MetadataResult meta = new RuleBasedExtractor().extract(txt);
            System.out.printf("      %d entities, %d attributes.%n",meta.entities().size(),meta.attributes().size());
            System.out.println("[3/3] Writing Excel: "+outputFile.getName());
            new ExcelWriter().write(templateFile, outputFile, meta);
            System.out.println("\nDone! Output: "+outputFile.getAbsolutePath());
            return 0;
        } catch (Exception e) { System.err.println("Error: "+e.getMessage()); return 1; }
    }
}

package com.metaextract.mcp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaextract.excel.ExcelWriter;
import com.metaextract.extractor.RuleBasedExtractor;
import com.metaextract.mapper.MetadataResult;
import com.metaextract.pdf.PdfTextExtractor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
public class McpServerMain {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static void start() {
        PrintStream rawOut = System.out;
        System.setOut(System.err);
        PrintWriter   out = new PrintWriter(new OutputStreamWriter(rawOut, StandardCharsets.UTF_8), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.err.println("[MCP] Server started.");
        try {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                System.err.println("[MCP] IN: " + line);
                Map<?,?> req = mapper.readValue(line, Map.class);
                String method = req.get("method") != null ? (String)req.get("method") : "";
                Object id = req.get("id");
                String resp = null;
                switch (method) {
                    case "initialize"                -> resp = init(id);
                    case "tools/list"                -> resp = toolsList(id);
                    case "tools/call"                -> resp = toolCall(id, req);
                    case "ping"                      -> resp = ok(id, new LinkedHashMap<>());
                    case "notifications/initialized" -> resp = null;
                    default -> resp = err(id, -32601, "Unknown: " + method);
                }
                if (resp != null) { out.println(resp); System.err.println("[MCP] OUT: " + resp); }
            }
        } catch (Exception e) { System.err.println("[MCP] Fatal: " + e.getMessage()); }
    }
    private static String init(Object id) throws Exception {
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("protocolVersion","2024-11-05");
        r.put("capabilities", Map.of("tools", Map.of()));
        r.put("serverInfo", Map.of("name","metadata-extractor","version","1.0.0"));
        return ok(id, r);
    }
    private static String toolsList(Object id) throws Exception {
        Map<String,Object> props = new LinkedHashMap<>();
        props.put("pdf_path",      Map.of("type","string","description","Absolute path to PDF file"));
        props.put("template_path", Map.of("type","string","description","Absolute path to metadata_standard.xlsx"));
        props.put("output_path",   Map.of("type","string","description","Absolute path for output Excel file"));
        Map<String,Object> schema = new LinkedHashMap<>();
        schema.put("type","object"); schema.put("properties",props);
        schema.put("required", new String[]{"pdf_path","template_path","output_path"});
        Map<String,Object> tool = new LinkedHashMap<>();
        tool.put("name","extract_metadata");
        tool.put("description","Extracts metadata from a PDF Control Sheet and exports to Excel.");
        tool.put("inputSchema",schema);
        return ok(id, Map.of("tools", new Object[]{tool}));
    }
    private static String toolCall(Object id, Map<?,?> req) throws Exception {
        try {
            Map<?,?> p = (Map<?,?>)((Map<?,?>)req.get("params")).get("arguments");
            File pdf = new File((String)p.get("pdf_path"));
            File tpl = new File((String)p.get("template_path"));
            File out = new File((String)p.get("output_path"));
            if (!pdf.exists()) return toolErr(id, "PDF not found: " + pdf);
            if (!tpl.exists()) return toolErr(id, "Template not found: " + tpl);
            String pdfText = new PdfTextExtractor().extract(pdf);
            MetadataResult meta = new RuleBasedExtractor().extract(pdfText);
            new ExcelWriter().write(tpl, out, meta);
            long ints = meta.attributes().stream().filter(a->"INTEGER".equals(a.getDatatype())).count();
            long decs = meta.attributes().stream().filter(a->"DECIMAL".equals(a.getDatatype())).count();
            long oth  = meta.attributes().size()-ints-decs;
            String txt = "Extraction complete!\n\nPDF: "+pdf.getName()+"\nOutput: "+out.getAbsolutePath()
                +"\nEntities: "+meta.entities().size()+"\nAttributes: "+meta.attributes().size()
                +"\n\nVARCHAR/DATE/CHAR: "+oth+"\nINTEGER: "+ints+"\nDECIMAL: "+decs;
            return ok(id, Map.of("content", new Object[]{Map.of("type","text","text",txt)}));
        } catch (Exception e) { return toolErr(id, e.getMessage()); }
    }
    private static String ok(Object id, Object result) throws Exception {
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("jsonrpc","2.0"); r.put("id",id); r.put("result",result);
        return mapper.writeValueAsString(r);
    }
    private static String err(Object id, int code, String msg) throws Exception {
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("jsonrpc","2.0"); r.put("id",id); r.put("error",Map.of("code",code,"message",msg));
        return mapper.writeValueAsString(r);
    }
    private static String toolErr(Object id, String msg) throws Exception {
        Map<String,Object> r = new LinkedHashMap<>();
        r.put("content", new Object[]{Map.of("type","text","text","Error: "+msg)});
        r.put("isError",true);
        return ok(id, r);
    }
}

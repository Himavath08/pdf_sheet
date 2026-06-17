package com.metaextract.excel;
import com.metaextract.mapper.AttributeRow;
import com.metaextract.mapper.EntityRow;
import com.metaextract.mapper.MetadataResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
public class ExcelWriter {
    private static final int[] EC = {40,40,80,14};
    private static final int[] AC = {40,50,50,70,14,20,14,10,10,24,20,24,12,28,28};
    public void write(File tmpl, File out, MetadataResult r) throws IOException {
        XSSFWorkbook wb;
        try (FileInputStream f = new FileInputStream(tmpl)) { wb = new XSSFWorkbook(f); }
        XSSFSheet es = find(wb,"Entity Sheet"), as = find(wb,"Attribute Sheet");
        CellStyle hs = hdr(wb), ds = dat(wb);
        styleRow(es,hs); styleRow(as,hs);
        es.createFreezePane(0,1); as.createFreezePane(0,1);
        for (EntityRow e : r.entities()) writeEnt(es,e,ds);
        for (AttributeRow a : r.attributes()) writeAttr(as,a,ds);
        setWidths(es,EC); setWidths(as,AC);
        if (out.getParentFile()!=null) out.getParentFile().mkdirs();
        try (FileOutputStream fo = new FileOutputStream(out)) { wb.write(fo); }
        wb.close();
    }
    private void writeEnt(XSSFSheet s, EntityRow e, CellStyle cs) {
        Row r=s.createRow(s.getLastRowNum()+1);
        c(r,0,e.getEntityPhysicalName(),cs); c(r,1,e.getEntityLogicalName(),cs);
        c(r,2,e.getEntityDescription(),cs); c(r,3,e.getType(),cs);
    }
    private void writeAttr(XSSFSheet s, AttributeRow a, CellStyle cs) {
        Row r=s.createRow(s.getLastRowNum()+1);
        c(r,0,a.getEntityPhysicalName(),cs); c(r,1,a.getAttributePhysicalName(),cs);
        c(r,2,a.getAttributeLogicalName(),cs); c(r,3,a.getAttributeDescription(),cs);
        c(r,4,a.getDatatype(),cs); c(r,5,a.getLengthPrecision(),cs);
        cn(r,6,a.getColumnOrder(),cs); c(r,7,a.getScale(),cs);
        c(r,8,a.getNullable(),cs); c(r,9,a.getDataFormat(),cs);
        c(r,10,a.getDefaultValue(),cs); c(r,11,a.getCheckConstraint(),cs);
        c(r,12,a.getKeyType(),cs); c(r,13,a.getPrimaryTableName(),cs);
        c(r,14,a.getPrimaryColumnName(),cs);
    }
    private void c(Row r,int i,String v,CellStyle s){Cell c=r.createCell(i,CellType.STRING);c.setCellValue(v!=null?v:"");c.setCellStyle(s);}
    private void cn(Row r,int i,int v,CellStyle s){Cell c=r.createCell(i,CellType.NUMERIC);c.setCellValue(v);c.setCellStyle(s);}
    private CellStyle hdr(XSSFWorkbook wb){
        XSSFCellStyle s=wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0x44,(byte)0x72,(byte)0xC4},null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f=wb.createFont(); f.setFontName("Arial"); f.setFontHeightInPoints((short)10); f.setBold(true);
        f.setColor(new XSSFColor(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF},null)); s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER); s.setVerticalAlignment(VerticalAlignment.CENTER); s.setWrapText(true);
        border(s); return s;
    }
    private CellStyle dat(XSSFWorkbook wb){
        XSSFCellStyle s=wb.createCellStyle();
        XSSFFont f=wb.createFont(); f.setFontName("Arial"); f.setFontHeightInPoints((short)10); s.setFont(f);
        s.setAlignment(HorizontalAlignment.LEFT); s.setVerticalAlignment(VerticalAlignment.CENTER); s.setWrapText(true);
        border(s); return s;
    }
    private void border(CellStyle s){s.setBorderTop(BorderStyle.THIN);s.setBorderBottom(BorderStyle.THIN);s.setBorderLeft(BorderStyle.THIN);s.setBorderRight(BorderStyle.THIN);}
    private void styleRow(XSSFSheet s,CellStyle cs){Row r=s.getRow(0);if(r==null)return;for(Cell c:r)c.setCellStyle(cs);r.setHeight((short)450);}
    private void setWidths(XSSFSheet s,int[]w){for(int i=0;i<w.length;i++)s.setColumnWidth(i,w[i]*256);}
    private XSSFSheet find(XSSFWorkbook wb,String name){
        for(int i=0;i<wb.getNumberOfSheets();i++)if(wb.getSheetName(i).equalsIgnoreCase(name))return wb.getSheetAt(i);
        List<String> n=new ArrayList<>(); for(int j=0;j<wb.getNumberOfSheets();j++)n.add(wb.getSheetName(j));
        throw new IllegalArgumentException("Sheet '"+name+"' not found. Available: "+n);
    }
}

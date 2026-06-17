package com.metaextract.extractor;
import com.metaextract.mapper.AttributeRow;
import com.metaextract.mapper.EntityRow;
import com.metaextract.mapper.MetadataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
public class RuleBasedExtractor {
    private static final String EP = "CTL_CONTROL_TOTALS_REPORT";
    private static final Pattern HP = Pattern.compile("^([A-Z][A-Z0-9 /]+?)\\s*:\\s*(.*)$");
    private static final Pattern RP = Pattern.compile("^(NUMBER OF .+?RECORDS?)\\s+([\\d,]+)\\s*([YN])?\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FP = Pattern.compile("^((?:PAID|INCURRED|RECOVERED|REIMBURSED|PAYMENT|RESERVE|RECOVERY SUMMARY|RECEIVABLES|REIMBURSEMENT|ISSUED).+?)\\s*([\\d,]+\\.\\d{2})?\\s*([\\d,]+\\.\\d{2})?\\s*$", Pattern.CASE_INSENSITIVE);

    public MetadataResult extract(String txt) {
        EntityRow entity = new EntityRow(EP, "Control Totals Report",
            "Interface summary report generated from the CTL record type.", "Table");
        List<AttributeRow> attrs = new ArrayList<>();
        int order = 1;
        for (String raw : txt.split("\\r?\\n")) {
            String line = raw.strip();
            if (line.isEmpty()) continue;
            AttributeRow a = null;
            if (isH(line)) a = parseH(line, order);
            else if (isR(line)) a = parseR(line, order);
            else if (isF(line)) a = parseF(line, order);
            if (a != null) { a.setEntityPhysicalName(EP); attrs.add(a); order++; }
        }
        return new MetadataResult(List.of(entity), attrs);
    }

    private boolean isH(String l) {
        Matcher m = HP.matcher(l);
        if (!m.matches()) return false;
        String f = m.group(1).trim();
        return f.matches("CLIENT NUMBER|ACCOUNT NUMBER|RESPONSIBLE OFFICE|COMBINED PROCESSING CODE|DATA SETS|CARRIER CODE|DATE PROCESSED|TIME PROCESSED|ACTIVITY PERIOD|CLAIM PERIOD|CURRENT ACTIVITY ONLY");
    }

    private AttributeRow parseH(String l, int o) {
        Matcher m = HP.matcher(l);
        if (!m.matches()) return null;
        String f = m.group(1).trim();
        String v = m.group(2).trim();
        AttributeRow a = new AttributeRow();
        a.setAttributePhysicalName(pn(f));
        a.setAttributeLogicalName(tc(f));
        a.setAttributeDescription(hdesc(f));
        a.setDatatype(hdt(f));
        a.setLengthPrecision(hlen(f));
        a.setColumnOrder(o);
        a.setScale("");
        a.setNullable(hreq(f) ? "N" : "Y");
        a.setDataFormat(hfmt(f));
        a.setDefaultValue(v);
        a.setCheckConstraint(f.equals("CURRENT ACTIVITY ONLY") ? "Y, N" : "");
        a.setKeyType("");
        a.setPrimaryTableName("");
        a.setPrimaryColumnName("");
        return a;
    }

    private boolean isR(String l) { return RP.matcher(l).matches(); }

    private AttributeRow parseR(String l, int o) {
        Matcher m = RP.matcher(l);
        if (!m.matches()) return null;
        String f = m.group(1).trim();
        String v = m.group(2) != null ? m.group(2).trim() : "0";
        String n = m.group(3) != null ? m.group(3).trim() : "Y";
        AttributeRow a = new AttributeRow();
        a.setAttributePhysicalName(pn(f));
        a.setAttributeLogicalName(tc(f));
        a.setAttributeDescription("Count of " + f.replace("NUMBER OF","").replace("RECORDS","records").toLowerCase().trim() + " in the file.");
        a.setDatatype("INTEGER");
        a.setLengthPrecision("10");
        a.setColumnOrder(o);
        a.setScale("");
        a.setNullable(n.equals("N") ? "N" : "Y");
        a.setDataFormat("");
        a.setDefaultValue(v.equals("0") ? "0" : "");
        a.setCheckConstraint("");
        a.setKeyType("");
        a.setPrimaryTableName("");
        a.setPrimaryColumnName("");
        return a;
    }

    private boolean isF(String l) {
        return FP.matcher(l).matches() && l.toUpperCase().matches(".*(PAID|INCURRED|RECOVERED|REIMBURSED|CONTROL TOTAL|ISSUED).*");
    }

    private AttributeRow parseF(String l, int o) {
        Matcher m = FP.matcher(l);
        if (!m.matches()) return null;
        String f = m.group(1).trim().replaceAll("\\s+[\\d,]+\\.\\d{2}.*$", "").trim();
        if (f.isEmpty()) return null;
        AttributeRow a = new AttributeRow();
        a.setAttributePhysicalName(pn(f));
        a.setAttributeLogicalName(tc(f));
        a.setAttributeDescription(fdesc(f));
        a.setDatatype("DECIMAL");
        a.setLengthPrecision("15");
        a.setColumnOrder(o);
        a.setScale("2");
        a.setNullable("Y");
        a.setDataFormat("9,999,999.99");
        a.setDefaultValue("");
        a.setCheckConstraint("");
        a.setKeyType("");
        a.setPrimaryTableName("");
        a.setPrimaryColumnName("");
        return a;
    }

    private String pn(String l) {
        return l.trim().toUpperCase().replaceAll("[^A-Z0-9]+","_").replaceAll("_+$","").replaceAll("^_+","");
    }

    private String tc(String i) {
        String[] w = i.toLowerCase().split("[\\s_]+");
        StringBuilder sb = new StringBuilder();
        for (String x : w) if (!x.isEmpty()) sb.append(Character.toUpperCase(x.charAt(0))).append(x.substring(1)).append(" ");
        return sb.toString().trim();
    }

    private String hdesc(String f) {
        switch (f) {
            case "CLIENT NUMBER":           return "Unique identifier and name of the client.";
            case "ACCOUNT NUMBER":          return "Account number associated with the client.";
            case "RESPONSIBLE OFFICE":      return "Office code and location responsible for processing.";
            case "COMBINED PROCESSING CODE":return "Code indicating combined processing grouping.";
            case "DATA SETS":               return "Identifier for the data set type.";
            case "CARRIER CODE":            return "Code identifying the insurance carrier.";
            case "DATE PROCESSED":          return "Date on which the data file was processed.";
            case "TIME PROCESSED":          return "Time at which the data file was processed.";
            case "ACTIVITY PERIOD":         return "Date range representing the activity period.";
            case "CLAIM PERIOD":            return "Date range representing the claim period.";
            case "CURRENT ACTIVITY ONLY":   return "Flag indicating current activity only (Y/N).";
            default:                        return "Value of " + tc(f) + " from the control sheet.";
        }
    }

    private String hdt(String f) {
        switch (f) {
            case "DATE PROCESSED":        return "DATE";
            case "TIME PROCESSED":        return "TIME";
            case "CURRENT ACTIVITY ONLY": return "CHAR";
            default:                      return "VARCHAR";
        }
    }

    private String hlen(String f) {
        switch (f) {
            case "DATE PROCESSED":        return "10";
            case "TIME PROCESSED":        return "8";
            case "CURRENT ACTIVITY ONLY": return "1";
            case "ACCOUNT NUMBER":        return "20";
            default:                      return "50";
        }
    }

    private String hfmt(String f) {
        switch (f) {
            case "DATE PROCESSED":        return "MM/DD/YYYY";
            case "TIME PROCESSED":        return "HH:MM";
            case "ACTIVITY PERIOD":       return "MM/DD/YYYY - MM/DD/YYYY";
            case "CLAIM PERIOD":          return "MM/DD/YYYY - MM/DD/YYYY";
            case "CURRENT ACTIVITY ONLY": return "Y or N";
            default:                      return "";
        }
    }

    private boolean hreq(String f) {
        return f.equals("CLIENT NUMBER") || f.equals("ACCOUNT NUMBER") || f.equals("DATE PROCESSED") || f.equals("TIME PROCESSED");
    }

    private String fdesc(String f) {
        String u = f.toUpperCase();
        if (u.contains("PAID TO DATE"))   return "Cumulative paid to date.";
        if (u.contains("PAID CURRENT"))   return "Amount paid current period.";
        if (u.contains("INCURRED"))       return "Total incurred amount.";
        if (u.contains("RECOVERED"))      return "Total recovered amount.";
        if (u.contains("REIMBURSED"))     return "Total reimbursed amount.";
        if (u.contains("CONTROL TOTAL"))  return "Control total for data integrity.";
        return "Financial amount for " + tc(f) + ".";
    }
}

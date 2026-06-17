package com.metaextract;
import com.metaextract.mcp.McpServerMain;
import picocli.CommandLine;
import com.metaextract.cli.MetaExtractCommand;
public class Main {
    public static void main(String[] args) {
        for (String a : args) { if ("--mcp".equals(a)) { McpServerMain.start(); return; } }
        System.exit(new CommandLine(new MetaExtractCommand()).execute(args));
    }
}

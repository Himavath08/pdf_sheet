package com.metaextract.llm;

/**
 * Stage 3 (support) — Prompt Builder.
 *
 * Constructs the system + user prompt that instructs the NVIDIA LLM to
 * analyse the extracted PDF text and return a structured JSON object
 * matching the metadata schema (Entity Sheet + Attribute Sheet).
 *
 * Prompt-engineering notes:
 *  - System message constrains the model to JSON-only output so Jackson
 *    can parse the response without stripping markdown fences.
 *  - The user message embeds the full PDF text and the exact JSON schema
 *    the model must follow, with field descriptions and example values.
 *  - Temperature is kept very low (0.1) in NvidiaApiClient to maximise
 *    determinism on a structured extraction task.
 */
public class LlmPromptBuilder {

    public static final String SYSTEM_PROMPT = """
        You are a data-modelling expert. Your only task is to analyse report documents
        and extract metadata in a strict JSON format. You must respond with valid JSON only
        — no markdown, no code fences, no explanation. Any deviation will break the downstream
        parser.
        """;

    /**
     * Builds the user prompt by embedding the extracted PDF text into the
     * metadata-extraction instruction template.
     *
     * @param pdfText plain text extracted from the PDF
     * @return formatted user prompt string
     */
    public static String build(String pdfText) {
        return """
        Analyse the report document text below and extract its metadata.

        Return a single JSON object with exactly two keys:
          - "entity"     : one object describing the report as a data entity
          - "attributes" : array of objects, one per field/data element in the report

        JSON schema (follow exactly — do not add or remove fields):

        {
          "entity": {
            "entityPhysicalName":  "<UPPER_SNAKE_CASE table name>",
            "entityLogicalName":   "<Human readable name>",
            "entityDescription":   "<One sentence describing the entity's purpose>",
            "type":                "Table"
          },
          "attributes": [
            {
              "entityPhysicalName":    "<same as entity.entityPhysicalName>",
              "attributePhysicalName": "<UPPER_SNAKE_CASE column name>",
              "attributeLogicalName":  "<Human readable column name>",
              "attributeDescription":  "<One sentence describing the column>",
              "datatype":              "<VARCHAR | INTEGER | DECIMAL | DATE | TIME | CHAR>",
              "lengthPrecision":       "<max length or numeric precision, e.g. 50 or 15>",
              "columnOrder":           <integer starting at 1>,
              "scale":                 "<decimal scale for DECIMAL fields, else empty string>",
              "nullable":              "<Y or N>",
              "dataFormat":            "<example format or mask, e.g. MM/DD/YYYY, 9,999,999.99>",
              "defaultValue":          "<default value if visible in the report, else empty string>",
              "checkConstraint":       "<allowed values if constrained, e.g. Y, N  — else empty>",
              "keyType":               "<PK | FK | empty string>",
              "primaryTableName":      "<referenced table if FK, else empty string>",
              "primaryColumnName":     "<referenced column if FK, else empty string>"
            }
          ]
        }

        Rules:
        1. Create one attribute per distinct label or data field visible in the report.
        2. For financial amount fields use datatype DECIMAL, lengthPrecision 15, scale 2.
        3. For record-count fields use datatype INTEGER, lengthPrecision 10.
        4. For date fields use DATE; for time fields use TIME.
        5. For flag/indicator fields (Y/N) use CHAR, lengthPrecision 1, checkConstraint "Y, N".
        6. Set nullable N for header/key fields, Y for optional or zero-value fields.
        7. Assign columnOrder sequentially starting from 1.
        8. Do not invent fields that are not present in the document.

        REPORT DOCUMENT TEXT:
        ---
        %s
        ---

        Respond with the JSON object only. No other text.
        """.formatted(pdfText);
    }
}

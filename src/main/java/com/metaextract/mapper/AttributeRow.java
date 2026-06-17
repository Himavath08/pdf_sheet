package com.metaextract.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stage 4 — Attribute POJO.
 *
 * Represents one row in the "Attribute Sheet" of the metadata template.
 * Field names mirror the JSON keys returned by the LLM and the column
 * headers in the Excel template.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeRow {

    @JsonProperty("entityPhysicalName")
    private String entityPhysicalName;

    @JsonProperty("attributePhysicalName")
    private String attributePhysicalName;

    @JsonProperty("attributeLogicalName")
    private String attributeLogicalName;

    @JsonProperty("attributeDescription")
    private String attributeDescription;

    @JsonProperty("datatype")
    private String datatype;

    @JsonProperty("lengthPrecision")
    private String lengthPrecision;

    @JsonProperty("columnOrder")
    private int columnOrder;

    @JsonProperty("scale")
    private String scale;

    @JsonProperty("nullable")
    private String nullable;

    @JsonProperty("dataFormat")
    private String dataFormat;

    @JsonProperty("defaultValue")
    private String defaultValue;

    @JsonProperty("checkConstraint")
    private String checkConstraint;

    @JsonProperty("keyType")
    private String keyType;

    @JsonProperty("primaryTableName")
    private String primaryTableName;

    @JsonProperty("primaryColumnName")
    private String primaryColumnName;

    // ---- Constructor --------------------------------------------------------

    public AttributeRow() {}

    // ---- Getters / Setters --------------------------------------------------

    public String getEntityPhysicalName()               { return entityPhysicalName; }
    public void   setEntityPhysicalName(String v)       { this.entityPhysicalName = v; }

    public String getAttributePhysicalName()            { return attributePhysicalName; }
    public void   setAttributePhysicalName(String v)    { this.attributePhysicalName = v; }

    public String getAttributeLogicalName()             { return attributeLogicalName; }
    public void   setAttributeLogicalName(String v)     { this.attributeLogicalName = v; }

    public String getAttributeDescription()             { return attributeDescription; }
    public void   setAttributeDescription(String v)     { this.attributeDescription = v; }

    public String getDatatype()                         { return datatype; }
    public void   setDatatype(String v)                 { this.datatype = v; }

    public String getLengthPrecision()                  { return lengthPrecision; }
    public void   setLengthPrecision(String v)          { this.lengthPrecision = v; }

    public int    getColumnOrder()                      { return columnOrder; }
    public void   setColumnOrder(int v)                 { this.columnOrder = v; }

    public String getScale()                            { return scale; }
    public void   setScale(String v)                    { this.scale = v; }

    public String getNullable()                         { return nullable; }
    public void   setNullable(String v)                 { this.nullable = v; }

    public String getDataFormat()                       { return dataFormat; }
    public void   setDataFormat(String v)               { this.dataFormat = v; }

    public String getDefaultValue()                     { return defaultValue; }
    public void   setDefaultValue(String v)             { this.defaultValue = v; }

    public String getCheckConstraint()                  { return checkConstraint; }
    public void   setCheckConstraint(String v)          { this.checkConstraint = v; }

    public String getKeyType()                          { return keyType; }
    public void   setKeyType(String v)                  { this.keyType = v; }

    public String getPrimaryTableName()                 { return primaryTableName; }
    public void   setPrimaryTableName(String v)         { this.primaryTableName = v; }

    public String getPrimaryColumnName()                { return primaryColumnName; }
    public void   setPrimaryColumnName(String v)        { this.primaryColumnName = v; }

    @Override
    public String toString() {
        return "AttributeRow{col=" + columnOrder +
               ", physical='" + attributePhysicalName +
               "', type='" + datatype + "'}";
    }
}

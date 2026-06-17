package com.metaextract.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stage 4 — Entity POJO.
 *
 * Represents one row in the "Entity Sheet" of the metadata template.
 * Field names mirror the JSON keys returned by the LLM.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityRow {

    @JsonProperty("entityPhysicalName")
    private String entityPhysicalName;

    @JsonProperty("entityLogicalName")
    private String entityLogicalName;

    @JsonProperty("entityDescription")
    private String entityDescription;

    @JsonProperty("type")
    private String type;

    // ---- Constructors -------------------------------------------------------

    public EntityRow() {}

    public EntityRow(String entityPhysicalName,
                     String entityLogicalName,
                     String entityDescription,
                     String type) {
        this.entityPhysicalName = entityPhysicalName;
        this.entityLogicalName  = entityLogicalName;
        this.entityDescription  = entityDescription;
        this.type               = type;
    }

    // ---- Getters / Setters --------------------------------------------------

    public String getEntityPhysicalName()              { return entityPhysicalName; }
    public void   setEntityPhysicalName(String v)      { this.entityPhysicalName = v; }

    public String getEntityLogicalName()               { return entityLogicalName; }
    public void   setEntityLogicalName(String v)       { this.entityLogicalName = v; }

    public String getEntityDescription()               { return entityDescription; }
    public void   setEntityDescription(String v)       { this.entityDescription = v; }

    public String getType()                            { return type; }
    public void   setType(String v)                    { this.type = v; }

    @Override
    public String toString() {
        return "EntityRow{physicalName='" + entityPhysicalName + "', type='" + type + "'}";
    }
}

package com.metaextract.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stage 4 — Data Mapper.
 *
 * Parses the raw JSON string returned by the NVIDIA LLM into a
 * {@link MetadataResult} containing lists of {@link EntityRow} and
 * {@link AttributeRow} objects ready for writing to Excel.
 *
 * Also applies post-processing rules to fix common LLM quirks:
 *  - null fields are replaced with empty strings
 *  - columnOrder is re-assigned sequentially if the LLM duplicated numbers
 *  - entityPhysicalName on attributes is propagated from the entity if missing
 */
public class MetadataMapper {

    private final ObjectMapper jackson = new ObjectMapper();

    /**
     * Maps the LLM JSON response to a {@link MetadataResult}.
     *
     * @param llmJson raw JSON string from {@code NvidiaApiClient.complete()}
     * @return populated MetadataResult
     * @throws IOException if parsing fails
     */
    public MetadataResult map(String llmJson) throws IOException {

        JsonNode root = jackson.readTree(llmJson);

        // --- Entity ---
        JsonNode entityNode = root.path("entity");
        if (entityNode.isMissingNode()) {
            throw new IOException("LLM response missing 'entity' key. Raw JSON:\n" + llmJson);
        }
        EntityRow entity = jackson.treeToValue(entityNode, EntityRow.class);
        sanitiseEntity(entity);

        // --- Attributes ---
        JsonNode attrsNode = root.path("attributes");
        if (!attrsNode.isArray()) {
            throw new IOException("LLM response 'attributes' is not an array. Raw JSON:\n" + llmJson);
        }

        List<AttributeRow> attributes = new ArrayList<>();
        int order = 1;
        for (JsonNode attrNode : attrsNode) {
            AttributeRow attr = jackson.treeToValue(attrNode, AttributeRow.class);
            sanitiseAttribute(attr, entity.getEntityPhysicalName(), order++);
            attributes.add(attr);
        }

        return new MetadataResult(List.of(entity), attributes);
    }

    // -------------------------------------------------------------------------
    // Sanitisation helpers
    // -------------------------------------------------------------------------

    /** Replaces any null fields in EntityRow with safe defaults. */
    private void sanitiseEntity(EntityRow e) {
        e.setEntityPhysicalName(nvl(e.getEntityPhysicalName(), "UNKNOWN_ENTITY"));
        e.setEntityLogicalName(nvl(e.getEntityLogicalName(),   "Unknown Entity"));
        e.setEntityDescription(nvl(e.getEntityDescription(),   ""));
        e.setType(nvl(e.getType(), "Table"));
    }

    /**
     * Replaces null fields in AttributeRow with safe defaults and applies
     * the sequential column-order override.
     */
    private void sanitiseAttribute(AttributeRow a, String entityPhysicalName, int order) {
        // Propagate entity name if the LLM left it blank
        if (a.getEntityPhysicalName() == null || a.getEntityPhysicalName().isBlank()) {
            a.setEntityPhysicalName(entityPhysicalName);
        }

        a.setAttributePhysicalName(nvl(a.getAttributePhysicalName(), "COLUMN_" + order));
        a.setAttributeLogicalName(nvl(a.getAttributeLogicalName(),   a.getAttributePhysicalName()));
        a.setAttributeDescription(nvl(a.getAttributeDescription(),   ""));
        a.setDatatype(nvl(a.getDatatype(),             "VARCHAR"));
        a.setLengthPrecision(nvl(a.getLengthPrecision(),""));
        a.setScale(nvl(a.getScale(),                   ""));
        a.setNullable(nvl(a.getNullable(),             "Y"));
        a.setDataFormat(nvl(a.getDataFormat(),         ""));
        a.setDefaultValue(nvl(a.getDefaultValue(),     ""));
        a.setCheckConstraint(nvl(a.getCheckConstraint(),""));
        a.setKeyType(nvl(a.getKeyType(),               ""));
        a.setPrimaryTableName(nvl(a.getPrimaryTableName(),""));
        a.setPrimaryColumnName(nvl(a.getPrimaryColumnName(),""));

        // Always override with sequential order to prevent LLM duplicates
        a.setColumnOrder(order);
    }

    /** Null-safe default: returns {@code fallback} if {@code value} is null or blank. */
    private String nvl(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.strip();
    }
}

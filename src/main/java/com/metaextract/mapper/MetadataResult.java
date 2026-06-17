package com.metaextract.mapper;

import java.util.List;

/**
 * Stage 4 — Result container.
 *
 * Holds the parsed lists of entity and attribute rows after the LLM JSON
 * has been mapped to Java objects. Passed directly to the ExcelWriter.
 */
public record MetadataResult(
        List<EntityRow>    entities,
        List<AttributeRow> attributes
) {}

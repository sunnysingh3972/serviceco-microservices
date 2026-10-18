
package com.serviceco.serviceco_matching_service.model.dto;

import java.util.List;

public record ProviderMatchPageResponse(
        List<ProviderMatchResponse> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
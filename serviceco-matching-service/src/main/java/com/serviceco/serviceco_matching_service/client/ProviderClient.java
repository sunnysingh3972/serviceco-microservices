
package com.serviceco.serviceco_matching_service.client;

import com.serviceco.serviceco_matching_service.model.dto.ProviderSearchPageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "serviceco-provider-service", fallbackFactory = ProviderClientFallbackFactory.class)
public interface ProviderClient {

    @GetMapping("/api/providers/search")
    ProviderSearchPageResponse searchProviders(
            @RequestParam("location") String location,
            @RequestParam("skill") String skill,
            @RequestParam("status") String status,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}
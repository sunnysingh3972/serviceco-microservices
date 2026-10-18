
package com.serviceco.serviceco_matching_service.services;

import com.serviceco.serviceco_matching_service.client.ProviderClient;
import com.serviceco.serviceco_matching_service.model.dto.ProviderMatchPageResponse;
import com.serviceco.serviceco_matching_service.model.dto.ProviderMatchResponse;
import com.serviceco.serviceco_matching_service.model.dto.ProviderSearchPageResponse;
import com.serviceco.serviceco_matching_service.model.dto.ProviderSearchResponse;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service

public class ProviderMatchingService {

    private static final int CANDIDATE_PAGE_SIZE = 100;

    private final ProviderClient providerClient;
   public ProviderMatchingService(ProviderClient providerClient) {
        this.providerClient = providerClient;
    }

    public ProviderMatchPageResponse findMatchingProviders(
            String location,
            String skill,
            String status,
            BigDecimal maxHourlyRate,
            int page,
            int size) {

        // 1. Retrieve all matching candidates in batches
        List<ProviderSearchResponse> candidates =
                fetchAllCandidates(location, skill, status);

        // 2. Exclude providers outside the customer's budget
        List<ProviderMatchResponse> rankedProviders =
                candidates.stream()
                        .filter(provider ->
                                maxHourlyRate == null
                                || (provider.hourlyRate() != null
                                && provider.hourlyRate()
                                        .compareTo(maxHourlyRate) <= 0))
                        .map(provider ->
                                calculateMatch(provider, maxHourlyRate))
                        .sorted(
                                Comparator.comparingInt(
                                        ProviderMatchResponse::matchScore
                                ).reversed()
                                .thenComparing(
                                        ProviderMatchResponse::hourlyRate,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                                .thenComparing(
                                        ProviderMatchResponse::providerId
                                )
                        )
                        .toList();

        // 3. Apply pagination AFTER scoring and ranking
        long totalElements = rankedProviders.size();

        int totalPages = totalElements == 0
                ? 0
                : (int) ((totalElements + size - 1) / size);

        long startLong = (long) page * size;
        int start = (int) Math.min(startLong, totalElements);
        int end = (int) Math.min(startLong + size, totalElements);

        List<ProviderMatchResponse> content =
                rankedProviders.subList(start, end);

        return new ProviderMatchPageResponse(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                totalPages == 0 || page >= totalPages - 1
        );
    }

    private List<ProviderSearchResponse> fetchAllCandidates(
            String location,
            String skill,
            String status) {

        ProviderSearchPageResponse firstPage =
                providerClient.searchProviders(
                        location,
                        skill,
                        status,
                        0,
                        CANDIDATE_PAGE_SIZE
                );

        List<ProviderSearchResponse> candidates =
                new ArrayList<>(firstPage.content());

        for (int currentPage = 1;
             currentPage < firstPage.totalPages();
             currentPage++) {

            ProviderSearchPageResponse nextPage =
                    providerClient.searchProviders(
                            location,
                            skill,
                            status,
                            currentPage,
                            CANDIDATE_PAGE_SIZE
                    );

            candidates.addAll(nextPage.content());
        }

        return candidates;
    }

    private ProviderMatchResponse calculateMatch(
            ProviderSearchResponse provider,
            BigDecimal maxHourlyRate) {

        double rating = provider.rating() == null
                || !Double.isFinite(provider.rating())
                ? 0.0
                : Math.max(0.0, Math.min(5.0, provider.rating()));

        int matchScore;
        String matchReason;

        if (maxHourlyRate == null) {

            // Without a budget, use rating as the score
            matchScore = (int) Math.round(rating / 5.0 * 100);

            matchReason = rating >= 4.5
                    ? "Highly rated provider for the requested skill"
                    : "Matches requested location and skill";

        } else {

            // Rating contributes 70% of the score
            double ratingScore = (rating / 5.0) * 70.0;

            // Remaining budget contributes up to 30%
            double savingsRatio =
                    1.0 - provider.hourlyRate().doubleValue()
                            / maxHourlyRate.doubleValue();

            double budgetScore =
                    Math.max(0.0, Math.min(1.0, savingsRatio)) * 30.0;

            matchScore = (int) Math.round(
                    ratingScore + budgetScore
            );

            boolean wellWithinBudget =
                    provider.hourlyRate().compareTo(
                            maxHourlyRate.multiply(
                                    new BigDecimal("0.80")
                            )
                    ) <= 0;

            if (rating >= 4.5 && wellWithinBudget) {
                matchReason = "Highly rated and well within budget";
            } else if (rating >= 4.5) {
                matchReason = "High rating and within budget";
            } else {
                matchReason = "Within budget and matches requested skill/location";
            }
        }

        return new ProviderMatchResponse(
                provider.id(),
                provider.name(),
                provider.location(),
                provider.hourlyRate(),
                provider.rating(),
                provider.status(),
                matchScore,
                matchReason
        );
    }
}
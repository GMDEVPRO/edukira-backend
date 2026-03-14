package com.edukira.service;

import com.edukira.dto.response.RankingResponse;

import java.util.List;
import java.util.UUID;

public interface RankingService {

    List<RankingResponse> getNationalRanking(String countryCode, String year, String period);

    List<RankingResponse> getGlobalRanking(String year, String period);

    RankingResponse getSchoolRanking(UUID schoolId, String year, String period);

    void computeRankings(String year, String period);
}

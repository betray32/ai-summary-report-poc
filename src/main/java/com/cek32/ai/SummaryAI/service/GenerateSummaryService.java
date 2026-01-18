package com.cek32.ai.SummaryAI.service;

import com.cek32.ai.SummaryAI.model.RequestSummaryAnalysis;
import com.cek32.ai.SummaryAI.model.ResponseSummaryAnalysis;

public interface GenerateSummaryService {

    ResponseSummaryAnalysis analyze(RequestSummaryAnalysis request);

}

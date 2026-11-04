package com.spring_calendario.ai;

import com.spring_calendario.ai.dto.InsightRequest;
import com.spring_calendario.ai.dto.InsightResponse;
import com.spring_calendario.ai.dto.PredictionRequest;
import com.spring_calendario.ai.dto.PredictionResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de IA: consejos (POST /api/ai/insight) y predicción (POST /api/ai/prediction). */
@RestController
@RequestMapping("/api/ai")
public class InsightController {

    private final InsightService insightService;
    private final PredictionService predictionService;

    public InsightController(InsightService insightService, PredictionService predictionService) {
        this.insightService = insightService;
        this.predictionService = predictionService;
    }

    @PostMapping("/insight")
    public InsightResponse insight(@Valid @RequestBody InsightRequest request) {
        return insightService.generate(request);
    }

    /** Predicción del próximo periodo con IA: POST /api/ai/prediction */
    @PostMapping("/prediction")
    public PredictionResponse prediction(@Valid @RequestBody PredictionRequest request) {
        return predictionService.predict(request);
    }
}

package com.spring_calendario.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import java.util.List;

/**
 * Datos para que la IA prediga el próximo periodo. Sin nombre ni correo.
 *
 * @param profile  respuestas del cuestionario inicial
 * @param cycles   ciclos registrados (inicio, días de sangrado, duración)
 * @param baseline cálculo simple de la app, usado como referencia y respaldo

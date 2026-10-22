package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Un día de menstruación registrado (tabla "dias_periodo"). */
@Entity
@Table(name = "dias_periodo")
public class DiaPeriodo {


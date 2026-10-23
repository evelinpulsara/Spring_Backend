package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Un síntoma registrado en un día (tabla "sintomas"). */
@Entity
@Table(name = "sintomas")
public class Sintoma {

    @Id

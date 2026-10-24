package com.spring_calendario.registros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Una relación sexual registrada y su protección (tabla "relaciones"). */
@Entity
@Table(name = "relaciones")
public class Relacion {


package com.spring_calendario.usuarios;

import com.spring_calendario.registros.DiaPeriodoRepository;
import com.spring_calendario.registros.RelacionRepository;
import com.spring_calendario.registros.SintomaRepository;
import com.spring_calendario.seguridad.TokenService;
import com.spring_calendario.usuarios.dto.AuthRequests;
import com.spring_calendario.usuarios.dto.UserDto;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

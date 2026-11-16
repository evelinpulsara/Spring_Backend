package com.spring_calendario;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.yaml.snakeyaml.Yaml;

/**
 * Contract-first: el contrato static/openapi.yaml es la fuente única de verdad.
 * Este test falla si el código tiene un endpoint que no está en el contrato,
 * o si el contrato promete un endpoint que el código no tiene.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:contract-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "gemini.api-key=",
})
class ContractTest {

    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "delete", "patch");

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping mappings;

    @Test
    void elCodigoYElContratoTienenLosMismosEndpoints() throws Exception {
        assertThat(endpointsDelCodigo()).isEqualTo(endpointsDelContrato());
    }

    private Set<String> endpointsDelCodigo() {
        Set<String> result = new TreeSet<>();
        for (RequestMappingInfo info : mappings.getHandlerMethods().keySet()) {
            for (String path : info.getPatternValues()) {
                if (!path.startsWith("/api/")) continue;
                info.getMethodsCondition().getMethods()
                        .forEach(m -> result.add(m.name() + " " + path));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<String> endpointsDelContrato() throws Exception {
        Set<String> result = new TreeSet<>();
        try (InputStream in = new ClassPathResource("static/openapi.yaml").getInputStream()) {
            Map<String, Object> doc = new Yaml().load(in);
            Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) doc.get("paths");
            paths.forEach((path, ops) -> ops.keySet().stream()
                    .filter(HTTP_METHODS::contains)
                    .forEach(m -> result.add(m.toUpperCase() + " " + path)));
        }
        return result;
    }
}

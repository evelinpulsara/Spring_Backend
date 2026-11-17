package com.spring_calendario.ai;

import com.spring_calendario.ai.dto.InsightRequest.CycleDto;
import java.util.Collection;
import java.util.function.Function;

final class PromptBuilder {

    private final StringBuilder text = new StringBuilder();

    PromptBuilder line(Object... parts) {
        for (Object part : parts) {
            text.append(part);
        }
        text.append('\n');
        return this;
    }

    PromptBuilder section(String title) {
        text.append('\n').append(title).append(":\n");
        return this;
    }

    PromptBuilder item(Object... parts) {
        text.append("- ");
        return line(parts);
    }

    <T> PromptBuilder items(Collection<T> values, String whenEmpty, Function<T, String> format) {
        if (values.isEmpty()) {
            return item(whenEmpty);
        }
        values.forEach(value -> item(format.apply(value)));
        return this;
    }

    static String cycle(CycleDto c) {
        String length = c.cycleLength() == null ? "en curso" : c.cycleLength() + " días";
        return c.startDate() + ", " + c.periodLength() + " días, " + length;
    }

    String build() {
        return text.toString();
    }
}

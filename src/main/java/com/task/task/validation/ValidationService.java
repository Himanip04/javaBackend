package com.task.task.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public Mono<Map<String, String>> validate(Map<String, Object> submissionData, Object schemaObj) {
        return Mono.fromCallable(() -> {
            Map<String, String> errors = new LinkedHashMap<>();

            if (schemaObj == null) return errors;

            Map<String, Object> schema = mapper.convertValue(schemaObj, Map.class);
            Object fieldsObj = schema.get("fields");
            if (!(fieldsObj instanceof List)) return errors;

            List<Map<String, Object>> fields = (List<Map<String, Object>>) fieldsObj;

            for (Map<String, Object> field : fields) {
                String name = (String) field.get("name");
                String type = field.getOrDefault("type", "text").toString();
                Map<String, Object> validations = (Map<String, Object>) field.get("validations");
                if (name == null) continue;

                Object rawVal = submissionData != null ? submissionData.get(name) : null;
                boolean hasValidations = validations != null && !validations.isEmpty();

                // REQUIRED
                if (field.getOrDefault("required", false).equals(true)) {
                    if (rawVal == null ||
                            (rawVal instanceof String && ((String) rawVal).trim().isEmpty()) ||
                            (rawVal instanceof Collection && ((Collection<?>) rawVal).isEmpty())) {
                        errors.put(name, "This field is required");
                        continue; // skip other checks
                    }
                }

                if (rawVal == null) continue;

                // TEXT / TEXTAREA validations
                if ("text".equals(type) || "textarea".equals(type) || rawVal instanceof String) {
                    String s = String.valueOf(rawVal);

                    if (hasValidations) {
                        if (validations.containsKey("minLength")) {
                            int min = toInt(validations.get("minLength"), -1);
                            if (min >= 0 && s.length() < min) {
                                errors.put(name, "Minimum length is " + min);
                                continue;
                            }
                        }
                        if (validations.containsKey("maxLength")) {
                            int max = toInt(validations.get("maxLength"), -1);
                            if (max >= 0 && s.length() > max) {
                                errors.put(name, "Maximum length is " + max);
                                continue;
                            }
                        }
                        if (validations.containsKey("regex")) {
                            String regex = String.valueOf(validations.get("regex"));
                            if (!regex.isBlank() && !Pattern.matches(regex, s)) {
                                errors.put(name, "Invalid format");
                                continue;
                            }
                        }
                    }
                }

                // NUMBER validations
                if ("number".equals(type) || isNumeric(rawVal)) {
                    Double value = toDouble(rawVal);
                    if (value != null && hasValidations) {
                        if (validations.containsKey("min")) {
                            Double min = toDouble(validations.get("min"));
                            if (min != null && value < min) {
                                errors.put(name, "Minimum value is " + min);
                                continue;
                            }
                        }
                        if (validations.containsKey("max")) {
                            Double max = toDouble(validations.get("max"));
                            if (max != null && value > max) {
                                errors.put(name, "Maximum value is " + max);
                                continue;
                            }
                        }
                    }
                }

                // DATE validations
                if ("date".equals(type) || looksLikeDate(rawVal)) {
                    String dateStr = String.valueOf(rawVal);
                    if (hasValidations && validations.containsKey("minDate")) {
                        String minDateStr = String.valueOf(validations.get("minDate"));
                        try {
                            LocalDate provided = LocalDate.parse(dateStr);
                            LocalDate minDate = LocalDate.parse(minDateStr);
                            if (provided.isBefore(minDate)) {
                                errors.put(name, "Date must be on or after " + minDateStr);
                                continue;
                            }
                        } catch (DateTimeParseException ex) {
                            errors.put(name, "Invalid date format (expected YYYY-MM-DD)");
                            continue;
                        }
                    }
                }

                // MULTI-SELECT validations
                if ("multi-select".equals(type) || "multiSelect".equals(type)) {
                    Collection<?> coll = rawVal instanceof Collection ? (Collection<?>) rawVal : null;
                    if (coll != null && hasValidations) {
                        if (validations.containsKey("minSelected")) {
                            int minSel = toInt(validations.get("minSelected"), -1);
                            if (minSel >= 0 && coll.size() < minSel) {
                                errors.put(name, "Select at least " + minSel + " options");
                                continue;
                            }
                        }
                        if (validations.containsKey("maxSelected")) {
                            int maxSel = toInt(validations.get("maxSelected"), -1);
                            if (maxSel >= 0 && coll.size() > maxSel) {
                                errors.put(name, "Select at most " + maxSel + " options");
                                continue;
                            }
                        }
                    }
                }
            }

            return errors;
        });
    }

    private static boolean isNumeric(Object o) {
        if (o == null) return false;
        try {
            Double.parseDouble(String.valueOf(o));
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static Double toDouble(Object o) {
        if (o == null) return null;
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception ex) {
            return null;
        }
    }

    private static int toInt(Object o, int fallback) {
        if (o == null) return fallback;
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static boolean looksLikeDate(Object o) {
        if (o == null) return false;
        try {
            LocalDate.parse(String.valueOf(o));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}



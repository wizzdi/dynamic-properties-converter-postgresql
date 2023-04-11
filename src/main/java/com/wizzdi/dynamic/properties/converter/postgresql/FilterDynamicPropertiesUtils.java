package com.wizzdi.dynamic.properties.converter.postgresql;


import javax.persistence.criteria.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.*;

public class FilterDynamicPropertiesUtils {


    public static List<Predicate> filterDynamic(Map<String, DynamicFilterItem> genericPredicates, CriteriaBuilder cb, Path<?> pathToDynamic) {
        List<Predicate> existingPredicates = new ArrayList<>();

        List<Expression<?>> pathSoFar = new ArrayList<>(Collections.singletonList(pathToDynamic));
        filterDynamic(genericPredicates, cb, pathSoFar, existingPredicates);
        return existingPredicates;
    }

    public static String getConvertFunctionSQL(String jsonNodeField, String key) {
        return "jsonb_extract_path_text(" + jsonNodeField + ",'" + key + "')";
    }


    private static void filterDynamic(Map<String, DynamicFilterItem> genericPredicates, CriteriaBuilder cb, List<Expression<?>> pathSoFar, List<Predicate> existingPredicates) {
        for (Map.Entry<String, DynamicFilterItem> entry : genericPredicates.entrySet()) {
            DynamicFilterItem dynamicFilterItem = entry.getValue();
            String key = entry.getKey();
            List<Expression<?>> newPath = new ArrayList<>(pathSoFar);
            newPath.add(cb.literal(key));
            if (dynamicFilterItem.getChildren() != null) {
                filterDynamic(dynamicFilterItem.getChildren(), cb, newPath, existingPredicates);
                return;
            }
            Expression<?>[] path = newPath.toArray(new Expression[0]);

            for (DynamicPredicate predicate : dynamicFilterItem.getPredicates()) {
                FilterType filterType = predicate.getFilterType();
                if (filterType != null) {
                    Object value = predicate.getValue();
                    Class<?> type = value == null ? null : value.getClass();
                    Expression<?> jsonb_extract_path = cb.function("jsonb_extract_path_text", String.class, path);
                    if (type != null && !String.class.equals(type)) {
                        if (Number.class.isAssignableFrom(type)) {
                            String format = getFormat((Number) value);
                            jsonb_extract_path = cb.function("to_number", type, jsonb_extract_path, cb.literal(format));
                        }
                        if (Boolean.class.isAssignableFrom(type)) {
                            value = String.valueOf(value);
                        }

                        if (value instanceof Temporal) {
                            jsonb_extract_path = cb.function("to_timestamp", type, jsonb_extract_path, cb.literal("YYYY-MM-DDTHH24:MI:SS.US9TZH:TZM"));
                        }
                        if (Collection.class.isAssignableFrom(type)) {
                            Collection<?> collection = ((Collection<?>) value).stream().map(f -> f instanceof Boolean ? String.valueOf(f) : f).toList();
                            value = collection;
                            if (!collection.isEmpty()) {
                                Object next = collection.iterator().next();

                                if (next instanceof Number) {
                                    String format = getFormat((Number) next);
                                    jsonb_extract_path = cb.function("to_number", type, jsonb_extract_path, cb.literal(format));
                                }
                                if (next instanceof Temporal) {
                                    jsonb_extract_path = cb.function("to_timestamp", type, jsonb_extract_path, cb.literal("YYYY-MM-DDTHH24:MI:SS.US9TZH:TZM"));
                                }
                            }
                        }

                    }
                    Predicate pred = switch (filterType) {

                        case EQUALS -> cb.equal(jsonb_extract_path, value);
                        case NOT_EQUALS -> cb.notEqual(jsonb_extract_path, value);
                        case IN -> jsonb_extract_path.in((Collection<?>) value);
                        case NOT_IN -> cb.not(jsonb_extract_path.in(value));
                        case CONTAINS -> cb.like((Expression<String>) jsonb_extract_path, "%" + value + "%");
                        case LESS_THAN ->
                                cb.lessThan((Expression<Comparable>) jsonb_extract_path, ((Comparable) value));
                        case LESS_THAN_OR_EQUAL ->
                                cb.lessThanOrEqualTo((Expression<Comparable>) jsonb_extract_path, ((Comparable) value));
                        case GREATER_THAN ->
                                cb.greaterThan((Expression<Comparable>) jsonb_extract_path, ((Comparable) value));
                        case GREATER_THAN_OR_EQUAL ->
                                cb.greaterThanOrEqualTo((Expression<Comparable>) jsonb_extract_path, ((Comparable) value));
                        case IS_NULL -> jsonb_extract_path.isNull();
                        case IS_NOT_NULL -> jsonb_extract_path.isNotNull();
                    };
                    existingPredicates.add(pred);

                }
            }

        }

    }

    public static String getFormat(Number value) {
        return value.toString().replaceAll("\\d", "9").replaceAll("\\.", "D").replaceAll(",", "G").replaceAll("\\[", "").replaceAll("]", "").replaceAll(" ", "");
    }
}

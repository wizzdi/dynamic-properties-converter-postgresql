package com.wizzdi.dynamic.properties.converter.postgresql;


import javax.persistence.criteria.*;

import java.util.*;

public class FilterDynamicPropertiesUtils {


    public static List<Predicate> filterDynamic(Map<String, DynamicFilterItem> genericPredicates, CriteriaBuilder cb, Path<?> pathToDynamic) {
        List<Predicate> existingPredicates = new ArrayList<>();

        List<Expression<?>> pathSoFar = new ArrayList<>(Collections.singletonList(pathToDynamic));
        filterDynamic(genericPredicates, cb,pathSoFar, existingPredicates);
        return existingPredicates;
    }

    public static String getConvertFunctionSQL(String jsonNodeField, String key) {
        return "jsonb_extract_path_text(" + jsonNodeField + ",'" + key + "')";
    }

    private static void filterDynamic(Map<String, DynamicFilterItem> genericPredicates, CriteriaBuilder cb,  List<Expression<?>> pathSoFar,List<Predicate> existingPredicates) {
        for (Map.Entry<String, DynamicFilterItem> entry : genericPredicates.entrySet()) {
            DynamicFilterItem dynamicFilterItem = entry.getValue();
            String key = entry.getKey();
            List<Expression<?>> newPath = new ArrayList<>(pathSoFar);
            newPath.add(cb.literal(key));
            if (dynamicFilterItem .getChildren()!=null) {
                filterDynamic(dynamicFilterItem.getChildren(), cb, newPath,existingPredicates);
                return;
            }
            if(dynamicFilterItem.getFilterType()!=null) {
                Expression<?>[] path=newPath.toArray(new Expression[0]);
                Object value = dynamicFilterItem.getValue();
                Class<?> type = value == null ? null : value.getClass();
                Expression<?> jsonb_extract_path = cb.function("jsonb_extract_path_text", String.class, path);
                if(type!=null&&!String.class.equals(type)){
                    if(Number.class.isAssignableFrom(type)){
                        jsonb_extract_path=cb.function("to_number",type,jsonb_extract_path,cb.literal("99G999D9S"));
                    }
                    if(Boolean.class.isAssignableFrom(type)){
                        value+="";
                    }

                }
                Predicate pred= switch (dynamicFilterItem.getFilterType()){

                    case EQUALS -> cb.equal(jsonb_extract_path, value);
                    case NOT_EQUALS -> cb.notEqual(jsonb_extract_path, value);
                    case IN -> jsonb_extract_path.in((Collection<?>) value);
                    case NOT_IN -> cb.not(jsonb_extract_path.in(value));
                    case CONTAINS -> cb.like((Expression<String>)jsonb_extract_path, "%" + value + "%");
                    case LESS_THAN -> cb.lessThan((Expression<Comparable>)jsonb_extract_path, ((Comparable) value));
                    case LESS_THAN_OR_EQUAL ->  cb.lessThanOrEqualTo((Expression<Comparable>)jsonb_extract_path, ((Comparable) value));
                    case GREATER_THAN -> cb.greaterThan((Expression<Comparable>)jsonb_extract_path, ((Comparable) value));
                    case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo((Expression<Comparable>)jsonb_extract_path, ((Comparable) value));
                    case IS_NULL -> jsonb_extract_path.isNull();
                    case IS_NOT_NULL -> jsonb_extract_path.isNotNull();
                };
                existingPredicates.add(pred);

            }
        }

    }
}

package com.wizzdi.dynamic.properties.converter.postgresql;

public class DynamicPredicate {

    private FilterType filterType;
    private Object value;

    public DynamicPredicate(FilterType filterType, Object value) {
        this.filterType = filterType;
        this.value = value;
    }

    public DynamicPredicate() {
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public <T extends DynamicPredicate> T setFilterType(FilterType filterType) {
        this.filterType = filterType;
        return (T) this;
    }

    public Object getValue() {
        return value;
    }

    public <T extends DynamicPredicate> T setValue(Object value) {
        this.value = value;
        return (T) this;
    }
}

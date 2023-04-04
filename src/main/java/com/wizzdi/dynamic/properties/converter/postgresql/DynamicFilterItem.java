package com.wizzdi.dynamic.properties.converter.postgresql;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;


public class DynamicFilterItem {

    private FilterType filterType;
    private Object value;
    private Map<String,DynamicFilterItem> children=null;

    public DynamicFilterItem() {
    }

    public DynamicFilterItem(FilterType filterType, Object value) {
        this.filterType = filterType;
        this.value = value;
    }

    public DynamicFilterItem(Map<String, DynamicFilterItem> children) {
        this.children = children;
    }

    @JsonAnyGetter
    public Map<String, DynamicFilterItem> getChildren() {
        return children;
    }

    public <T extends DynamicFilterItem> T setChildren(Map<String, DynamicFilterItem> children) {
        this.children = children;
        return (T) this;
    }

    @JsonAnySetter
    public void add(String key, DynamicFilterItem value) {
        if(children==null){
            children=new HashMap<>();
        }
        children.put(key, value);
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public <T extends DynamicFilterItem> T setFilterType(FilterType filterType) {
        this.filterType = filterType;
        return (T) this;
    }

    public Object getValue() {
        return value;
    }

    public <T extends DynamicFilterItem> T setValue(Object value) {
        this.value = value;
        return (T) this;
    }
}

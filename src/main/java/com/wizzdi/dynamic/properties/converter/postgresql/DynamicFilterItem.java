package com.wizzdi.dynamic.properties.converter.postgresql;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DynamicFilterItem {

    private List<DynamicPredicate> predicates=new ArrayList<>();

    private Map<String,DynamicFilterItem> children=null;

    public DynamicFilterItem() {
    }

    public static DynamicFilterItem isNull(){
        return new DynamicFilterItem(List.of(new DynamicPredicate(FilterType.IS_NULL,null)));
    }

    public static DynamicFilterItem isNotNull(){
        return new DynamicFilterItem(List.of(new DynamicPredicate(FilterType.IS_NOT_NULL,null)));
    }
    public static DynamicFilterItem of(FilterType filterType,Object value){
        return new DynamicFilterItem(List.of(new DynamicPredicate(filterType,value)));
    }

    public static DynamicFilterItem of(String filterType,DynamicFilterItem dynamicFilterItem){
        return new DynamicFilterItem(Map.of(filterType,dynamicFilterItem));
    }

    public static DynamicFilterItem of(String filterType1,DynamicFilterItem dynamicFilterItem1,String filterType2,DynamicFilterItem dynamicFilterItem2){
        return new DynamicFilterItem(Map.of(filterType1,dynamicFilterItem1,filterType2,dynamicFilterItem2));
    }



    public DynamicFilterItem(List<DynamicPredicate> predicates) {
        this.predicates = predicates;
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

    public List<DynamicPredicate> getPredicates() {
        return predicates;
    }

    public <T extends DynamicFilterItem> T setPredicates(List<DynamicPredicate> predicates) {
        this.predicates = predicates;
        return (T) this;
    }
}

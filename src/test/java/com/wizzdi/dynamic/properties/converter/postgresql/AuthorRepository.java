package com.wizzdi.dynamic.properties.converter.postgresql;

import com.wizzdi.dynamic.properties.converter.DynamicPropertiesUtils;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class AuthorRepository {

    @Autowired
    private EntityManager em;


    @Transactional
    public Author merge(Author author){
       return em.merge(author);
    }

    public List<Author> getAuthors(Map<String, DynamicFilterItem> filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Author> q = cb.createQuery(Author.class);
        Root<Author> r = q.from(Author.class);
        List<Predicate> preds = FilterDynamicPropertiesUtils.filterDynamic(filter,cb,r.get("dynamicProperties"));

        q.select(r).where(preds.toArray(Predicate[]::new));
        TypedQuery<Author> query = em.createQuery(q);
        return query.getResultList();
    }
}

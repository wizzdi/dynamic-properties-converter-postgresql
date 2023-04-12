package com.wizzdi.dynamic.properties.converter.postgresql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = App.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class DynamicFilterTest {

    @Autowired
    private AuthorService authorService;
    private final OffsetDateTime baseDate = OffsetDateTime.now();



    @Test
    @Order(1)
    public void testCreate() {
        for (int i = 0; i < 10; i++) {
            Map<String, Object> dynamic = Map.of("surName", "van " + i, "location", Map.of("cityName", "city " + i, "capital", i % 2 == 0), "age", i * 10, "familiy", "kuku", "books", List.of("first book " + i, "second book " + i), "birthDate", baseDate.minusYears(i * 10));
            authorService.createAuthor("van " + i, dynamic);
        }

        for (int i = 0; i < 10; i++) {
            Map<String, Object> dynamic = Map.of("surName", "kuku " + i, "location", Map.of("cityName", "city " + i, "capital", i % 2 == 0), "age", i * 10, "books", List.of("first book " + i, "second book " + i), "birthDate", baseDate.minusYears(i * 10));
            authorService.createAuthor("kuku " + i, dynamic);
        }


    }

    @Test
    @Order(2)
    public void testEQ() {
        List<Author> surName = authorService.getAuthors(Map.of("surName", DynamicFilterItem.of(FilterType.EQUALS, "van 1")));
        Assertions.assertEquals(1, surName.size());

    }

    @Test
    @Order(2)
    public void testEQBool() {
        List<Author> surName = authorService.getAuthors(Map.of("location", DynamicFilterItem.of("capital", DynamicFilterItem.of(FilterType.EQUALS, true))));
        Assertions.assertEquals(10, surName.size());

    }

    @Test
    @Order(3)
    public void testNEQ() {
        List<Author> surName = authorService.getAuthors(Map.of("surName", DynamicFilterItem.of(FilterType.NOT_EQUALS, "van 1")));
        Assertions.assertEquals(19, surName.size());

    }

    @Test
    @Order(3)
    public void testNEQNested() {
        List<Author> surName = authorService.getAuthors(Map.of("location", DynamicFilterItem.of("cityName", DynamicFilterItem.of(FilterType.NOT_EQUALS, "city 0"))));
        Assertions.assertEquals(18, surName.size());

    }

    @Test
    @Order(4)
    public void testContains() {
        List<Author> surName = authorService.getAuthors(Map.of("surName", DynamicFilterItem.of(FilterType.CONTAINS, "ku")));
        Assertions.assertEquals(10, surName.size());


    }

    @Test
    @Order(5)
    public void testIn() {
        List<Author> surName = authorService.getAuthors(Map.of("surName", DynamicFilterItem.of(FilterType.IN, new ArrayList<>(List.of("van 1", "van 2")))));
        Assertions.assertEquals(2, surName.size());


    }

    @Test
    @Order(5)
    public void testInNumber() {
        List<Author> surName = authorService.getAuthors(Map.of("age", DynamicFilterItem.of(FilterType.IN, new ArrayList<>(List.of(10,20)))));
        Assertions.assertEquals(4, surName.size());


    }

    @Test
    @Order(6)
    public void testInBoolean() {
        List<Author> surName = authorService.getAuthors(Map.of("location", DynamicFilterItem.of("capital", DynamicFilterItem.of(FilterType.IN, List.of(true,false)))));
        Assertions.assertEquals(20, surName.size());


    }

    @Test
    @Order(6)
    public void testLT() {
        List<Author> surName = authorService.getAuthors(Map.of("age", DynamicFilterItem.of(FilterType.LESS_THAN, 30)));
        Assertions.assertEquals(6, surName.size());


    }

    @Test
    @Order(6)
    public void testLTDate() {
        List<Author> surName = authorService.getAuthors(Map.of("birthDate", DynamicFilterItem.of(FilterType.LESS_THAN, baseDate.minusYears(50))));
        Assertions.assertEquals(10, surName.size());


    }

    @Test
    @Order(7)
    public void testLTE() {
        List<Author> surName = authorService.getAuthors(Map.of("age", DynamicFilterItem.of(FilterType.LESS_THAN_OR_EQUAL, 30)));
        Assertions.assertEquals(8, surName.size());


    }

    @Test
    @Order(8)
    public void testGT() {
        List<Author> surName = authorService.getAuthors(Map.of("age", DynamicFilterItem.of(FilterType.GREATER_THAN, 30)));
        Assertions.assertEquals(12, surName.size());


    }


    @Test
    @Order(8)
    public void testGTPercision() {
        List<Author> surName = authorService.getAuthors(Map.of("age", DynamicFilterItem.of(FilterType.GREATER_THAN, 0.99)));
        Assertions.assertEquals(18, surName.size());


    }

    @Test
    @Order(8)
    public void testGTDate() {
        OffsetDateTime date = baseDate.minusYears(50);
        List<Author> surName = authorService.getAuthors(Map.of("birthDate", DynamicFilterItem.of(FilterType.GREATER_THAN, date)));
        Assertions.assertEquals(10, surName.size());
        System.out.println("authors over date " + date + " are " +surName.stream().map(f->f.getName()+" at "+f.getDynamicProperties().get("birthDate")).collect(Collectors.joining(",")) );


    }

    @Test
    @Order(9)
    public void testGTE() {
        List<Author> surName = authorService.getAuthors(Map.of("age", DynamicFilterItem.of(FilterType.GREATER_THAN_OR_EQUAL, 30)));
        Assertions.assertEquals(14, surName.size());


    }



    @Test
    @Order(10)
    public void testNull() {
        List<Author> surName = authorService.getAuthors(Map.of("familiy", DynamicFilterItem.isNull()));
        Assertions.assertEquals(10, surName.size());


    }

    @Test
    @Order(10)
    public void testNotNull() {
        List<Author> surName = authorService.getAuthors(Map.of("familiy", DynamicFilterItem.isNotNull()));
        Assertions.assertEquals(10, surName.size());


    }

    @Test
    @Order(11)
    public void testSerialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Map<String, DynamicFilterItem> filter = Map.of("location", DynamicFilterItem.of("capital", DynamicFilterItem.of(FilterType.EQUALS, true)));
        objectMapper.writeValueAsString(filter);
        System.out.println(filter);


    }

    @Test
    @Order(12)
    public void testDeserialization() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String s = """
                {
                    "location": {
                        "capital": {
                            "predicates": [{
                                    "type": "EQUALS",
                                    "value": true
                                }
                            ]
                        }
                                
                    }
                }
                """;
        TypeReference<Map<String, DynamicFilterItem>> typeRef = new TypeReference<>() {
        };
        Map<String, DynamicFilterItem> map = objectMapper.readValue(s, typeRef);
        Assertions.assertNotNull(map.get("location"));
        Assertions.assertNotNull(map.get("location").getChildren());
        Assertions.assertNotNull(map.get("location").getChildren().get("capital"));

        Assertions.assertInstanceOf(Boolean.class, map.get("location").getChildren().get("capital").getPredicates().get(0).getValue());


    }



}

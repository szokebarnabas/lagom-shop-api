package io.dworkin.product.impl;

import akka.japi.Pair;
import com.github.pgasync.ConnectionPool;
import com.github.pgasync.Row;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.toList;

/**
 * Created by yakov on 19.03.2017.
 */
public class ProductRepository {

    private final ConnectionPool connectionPool;

    public ProductRepository(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public CompletionStage<Optional<ProductEntity>> getByCode(String code) {
        CompletableFuture<Optional<ProductEntity>> future = new CompletableFuture<>();

        String query = "select * from product where code = $1";

        connectionPool.query(query, Arrays.asList(code), result -> {
            if (result.size() > 0)
                future.complete(Optional.of(
                        new ProductEntity(result.row(0).getString("code"), result.row(0).getString("displayName"),
                                result.row(0).getBigDecimal("price").doubleValue(), result.row(0).getString("description"),
                                result.row(0).getString("imageUrl"))));
            else
                future.complete(Optional.empty());
        }, future::completeExceptionally);

        return future;
    }

    public CompletionStage<List<ProductEntity>> listByCategoryNameAndPropertyValues(String category, List<List<String>> propertyValues,
                                                                                    Integer first, Integer max, String orderProperty, Boolean isAsc) {

        final CompletableFuture<List<ProductEntity>> future = new CompletableFuture<>();

        final StringBuilder queryBuilder = new StringBuilder("select * from product as prod where category_id=(select id from category where name='").append(category).append("')");

        buildPropertyValuesSubqueries(propertyValues, queryBuilder);
        queryBuilder.append(" order by ").append(orderProperty);
        if (!isAsc)
            queryBuilder.append(" desc");
        queryBuilder.append(" limit ").append(max).append(" offset ").append(first);

        connectionPool.query(queryBuilder.toString(),
                queryRes -> {
                    List<ProductEntity> products = new ArrayList<>();
                    queryRes.forEach(row -> products.add(new ProductEntity(row.getString("code"), row.getString("displayName"),
                            row.getBigDecimal("price").doubleValue(), row.getString("description"), row.getString("imageUrl"))));
                    future.complete(products);
                }, future::completeExceptionally);

        return future;
    }

    public CompletionStage<List<PropertyWithCount>> countPropertyValuesByCategoryIdAndFilter(String category, String property, List<Pair<String, List<String>>> propertyValues) {

        final CompletableFuture<List<PropertyWithCount>> future = new CompletableFuture<>();

        final StringBuilder queryBuilder = new StringBuilder("select prop.name as prop_name, prop.displayname as prop_displayname,")
                .append(" propval.name as propval_name, propval.displayname as propval_displayname, count(*) from product as prod")
                .append(" inner join product_property_value as ppv on product_id=prod.id")
                .append(" inner join property_value as propval on propval.id=ppv.propertyvalues_id")
                .append(" inner join property as prop on prop.id=propval.property_id")
                .append(" where prod.category_id=(select id from category where name='").append(category).append("')");

        if (property != null) {
            queryBuilder.append(" and prop.name = ").append(property);

            final List<List<String>> propertyValuesFiltered = propertyValues.stream()
                    .filter(pair -> !pair.first().equals(property)).map(Pair::second).collect(toList());

            buildPropertyValuesSubqueries(propertyValuesFiltered, queryBuilder);
        } else
            buildPropertyValuesSubqueries(propertyValues.stream().map(Pair::second).collect(toList()), queryBuilder);

        final List<String> flatPropertyValues = propertyValues.stream().flatMap(pair->pair.second().stream()).collect(toList());
        if (!flatPropertyValues.isEmpty()) {
            queryBuilder.append(" and propval.name not in (");
            for (int i = 0; i < flatPropertyValues.size(); i++) {
                queryBuilder.append(flatPropertyValues.get(i));
                if (i != flatPropertyValues.size() - 1)
                    queryBuilder.append(",");
            }
            queryBuilder.append(")");
        }

        queryBuilder.append(" group by prop.name, prop.displayname, propval.name, propval.displayname, ppv.propertyvalues_id order by prop.displayname, propval.displayname");

        connectionPool.query(queryBuilder.toString(),
                queryRes -> {
                    final List<PropertyWithCount> result = new ArrayList<>();
                    PropertyWithCount resultItem = null;
                    for (Row row : queryRes) {
                        if (resultItem == null || !resultItem.name.equals(row.getString("prop_name"))) {
                            resultItem = new PropertyWithCount(row.getString("prop_name"), row.getString("prop_displayname"));
                            result.add(resultItem);
                        }

                        resultItem.propertyValues.add(new PropertyValueWithCount(row.getString("propval_name"),
                                row.getString("propval_displayname"), row.getLong("count")));
                    }

                    future.complete(result);
                }, future::completeExceptionally);

        return future;
    }

    private void buildPropertyValuesSubqueries(List<List<String>> propertyValues, StringBuilder queryBuilder) {
        propertyValues.forEach(names -> {
            queryBuilder.append(" and exists (select * from product_property_value where prod.id=product_id and propertyvalues_id in (");
            for (int i = 0; i < names.size(); i++) {
                queryBuilder.append("(select id from property_value where name='").append(names.get(i)).append("')");
                if (i != names.size() - 1)
                    queryBuilder.append(",");
            }
            queryBuilder.append("))");
        });
    }

    public static class PropertyWithCount {
        public final String name;
        public final String diaplayName;

        public final List<PropertyValueWithCount>propertyValues;

        public PropertyWithCount(String name, String diaplayName) {
            this.name = name;
            this.diaplayName = diaplayName;
            this.propertyValues = new ArrayList<>();
        }
    }

    public static class PropertyValueWithCount {
        public final String name;
        public final String diaplayName;
        public final Long count;

        public PropertyValueWithCount(String name, String diaplayName, Long count) {
            this.name = name;
            this.diaplayName = diaplayName;
            this.count = count;
        }
    }
}

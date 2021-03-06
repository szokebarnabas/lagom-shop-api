package io.dworkin.product.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.pcollections.PSequence;

import javax.annotation.concurrent.Immutable;

/**
 * Created by yakov on 19.03.2017.
 */
@Immutable
@JsonDeserialize
public final class ListFilteredRequest {
    public final String category;
    public final PSequence<PropertyRequest> properties;
    public final Integer first;
    public final Integer max;
    public final String orderBy;
    public final Boolean isAsc;
    public final String searchText;

    @JsonCreator
    public ListFilteredRequest(@JsonProperty("category") String category, @JsonProperty("properties") PSequence<PropertyRequest> properties,
                               @JsonProperty("first") Integer first, @JsonProperty("max") Integer max,
                               @JsonProperty("orderBy") String orderBy, @JsonProperty("isAsc") Boolean isAsc,
                               @JsonProperty("searchText") String searchText) {
        this.category = Preconditions.checkNotNull(category);
        this.properties = properties;
        this.first = first;
        this.max = max;
        this.orderBy = orderBy;
        this.isAsc = isAsc;
        this.searchText = searchText;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("category", category)
                .add("properties", properties)
                .add("max", max)
                .add("orderBy", orderBy)
                .add("isAsc", isAsc)
                .add("searchText", searchText)
                .toString();
    }
}

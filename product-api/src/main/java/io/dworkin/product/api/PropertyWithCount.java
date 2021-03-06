package io.dworkin.product.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.pcollections.PSequence;

/**
 * Created by yakov on 27.03.2017.
 */
@Immutable
@JsonSerialize
public class PropertyWithCount {

    public final String name;
    public final String displayName;
    public final Boolean isAdditional;
    public final PSequence<PropertyValueWithCount> propertyValues;

    @JsonCreator
    public PropertyWithCount(@JsonProperty("name") String name, @JsonProperty("displayName") String displayName,
                             @JsonProperty("propertyValues") PSequence<PropertyValueWithCount> propertyValues, Boolean isAdditional) {
        this.name = name;
        this.displayName = displayName;
        this.propertyValues = propertyValues;
        this.isAdditional = isAdditional;
    }

    @Immutable
    @JsonSerialize
    public static final class PropertyValueWithCount {
        public final String name;
        public final String displayName;
        public final Boolean selected;
        public final Long count;

        @JsonCreator
        public PropertyValueWithCount(@JsonProperty("name") String name, @JsonProperty("displayName") String displayName,
                                      @JsonProperty("count") Long count, Boolean selected) {
            this.name = name;
            this.displayName = displayName;
            this.count = count;
            this.selected = selected;
        }
    }
}



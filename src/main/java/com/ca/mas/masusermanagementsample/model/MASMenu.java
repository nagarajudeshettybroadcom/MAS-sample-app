
package com.ca.mas.masusermanagementsample.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "list"
})
public class MASMenu {

    @JsonProperty("list")
    private java.util.List<com.ca.mas.masusermanagementsample.model.List> list = null;

    @JsonProperty("list")
    public java.util.List<com.ca.mas.masusermanagementsample.model.List> getList() {
        return list;
    }

    @JsonProperty("list")
    public void setList(java.util.List<com.ca.mas.masusermanagementsample.model.List> list) {
        this.list = list;
    }

}

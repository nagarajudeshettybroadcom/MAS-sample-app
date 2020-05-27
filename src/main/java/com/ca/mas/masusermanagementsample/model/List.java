
package com.ca.mas.masusermanagementsample.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "submenu"
})
public class List {

    @JsonProperty("name")
    private String name;
    @JsonProperty("submenu")
    private java.util.List<Submenu> submenu = null;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("submenu")
    public java.util.List<Submenu> getSubmenu() {
        return submenu;
    }

    @JsonProperty("submenu")
    public void setSubmenu(java.util.List<Submenu> submenu) {
        this.submenu = submenu;
    }

}

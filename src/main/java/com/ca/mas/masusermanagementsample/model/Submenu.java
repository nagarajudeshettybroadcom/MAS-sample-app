package com.ca.mas.masusermanagementsample.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "menu",
        "type",
        "inputTextRequired",
        "inputTextRequiredTwo",
        "showOutPut",
        "hint",
        "hintTwo"
})
public class Submenu {

    @JsonProperty("menu")
    private String menu;
    @JsonProperty("type")
    private String type;
    @JsonProperty("inputTextRequired")
    private boolean inputTextRequired;
    @JsonProperty("inputTextRequiredTwo")
    private boolean inputTextRequiredTwo;
    @JsonProperty("showOutPut")
    private boolean showOutPut;
    @JsonProperty("hint")
    private String hint;
    @JsonProperty("hintTwo")
    private String hintTwo;
    @JsonProperty("message")
    private String message;

    @JsonProperty("menu")
    public String getMenu() {
        return menu;
    }

    @JsonProperty("menu")
    public void setMenu(String menu) {
        this.menu = menu;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("inputTextRequired")
    public boolean getInputTextRequired() {
        return inputTextRequired;
    }

    @JsonProperty("inputTextRequired")
    public void setInputTextRequired(boolean inputTextRequired) {
        this.inputTextRequired = inputTextRequired;
    }

    @JsonProperty("inputTextRequiredTwo")
    public boolean getInputTextRequiredTwo() {
        return inputTextRequiredTwo;
    }

    @JsonProperty("inputTextRequiredTwo")
    public void setInputTextRequiredTwo(boolean inputTextRequiredTwo) {
        this.inputTextRequiredTwo = inputTextRequiredTwo;
    }

    @JsonProperty("showOutPut")
    public boolean getShowOutPut() {
        return showOutPut;
    }

    @JsonProperty("showOutPut")
    public void setShowOutPut(boolean showOutPut) {
        this.showOutPut = showOutPut;
    }

    @JsonProperty("hint")
    public String getHint() {
        return hint;
    }

    @JsonProperty("hint")
    public void setHint(String hint) {
        this.hint = hint;
    }

    @JsonProperty("hintTwo")
    public String getHintTwo() {
        return hintTwo;
    }

    @JsonProperty("hintTwo")
    public void setHintTwo(String hintTwo) {
        this.hintTwo = hintTwo;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }
}

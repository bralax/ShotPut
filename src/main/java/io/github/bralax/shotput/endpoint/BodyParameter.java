package io.github.bralax.shotput.endpoint;

import java.util.HashMap;
import java.util.Map;

public class BodyParameter {
    private Parameter param;
    private Map<String, BodyParameter> children;

    public BodyParameter(Parameter param) {
        this.param = param;
        this.children = new HashMap<>();
    }

    public void setParam(Parameter param) {
        this.param = param;
    }

    public Parameter getParam() {
        return this.param;
    }

    public void addChild(Parameter param, int level) {
        String[] splitName = param.getName().split(".");
        int index = 0;
        for (int i = level; i < splitName.length; i++) {
            String name = splitName[i];
            if (name.equals(this.param.getName())) {
                index = level;
                break;
            }
        }
        if (splitName.length == index+2) {
            if (this.children.containsKey(splitName[index+1])) {
                this.children.get(splitName[index+1]).setParam(param);
            } else {
                this.children.put(splitName[index+1], new BodyParameter(param));
            }
        } else if (splitName.length > index+2) {
            if (this.children.containsKey(splitName[index+1])) {
                this.children.get(splitName[index+1]).addChild(param, level+1);//setParam(param);
            } else {
                BodyParameter body = new BodyParameter(new Parameter(splitName[index+1], null));
                body.addChild(param, level+1);
                this.children.put(splitName[index+1], body);
            }
        }
    }

    @Override
    public String toString() {
        return "{param : {type:"+this.param+  "\n" +
            "children: " + this.children + "}";
    }
}

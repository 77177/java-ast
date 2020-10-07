package com.parts.method;

import com.JavaParser;
import com.parts.Declaration;
import guru.nidi.graphviz.model.MutableNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class If implements Declaration {

    Expression init;
    Body body;

    public If(String If) {
        int f = If.indexOf("(");
        int l = If.indexOf(")");
        init = new Expression(If.substring(f + 1, l));
        body = new Body(If.substring(If.indexOf("{")));
    }

    @Override
    public MutableNode getNode() {
        MutableNode If = JavaParser.getNode("if");
        If.addLink(init.getNode());
        If.addLink(body.getNode());
        return If;
    }
}

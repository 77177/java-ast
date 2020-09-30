package com.parts;

import com.JavaParser;
import guru.nidi.graphviz.model.MutableNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportDeclaration {
    private String name;

    public MutableNode getNode(){
        return JavaParser.getNode(name);
    }
}

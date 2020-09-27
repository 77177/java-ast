package com;

import com.parts.ClassDeclaration;
import com.parts.ImportDeclaration;
import com.parts.ImportStaticDeclaration;
import com.parts.PackageDeclaration;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

public class JavaParser {
    public static void parse(String[] args) throws IOException {
        Path javaFilePath = null;
        if (args.length == 0) {
            javaFilePath = Paths.get("java");
        }
        Files.list(requireNonNull(javaFilePath))
                .filter(path -> path.getFileName().toString().endsWith(".java"))
                .map(Path::getFileName)
                .forEach(JavaParser::createAST);
    }

    @SneakyThrows
    private static void createAST(Path javaFile) {

        List<String> codeLines = Files.lines(Paths.get("java", javaFile.toString())).collect(
                collectingAndThen(joining()
                        , fileCode -> stream(fileCode.split(";")).collect(toList())));

        List<PackageDeclaration> packageDeclarations = codeLines.stream()
                .filter(line -> line.contains("package"))
                .map(line -> new PackageDeclaration(line.replace("package", "")))
                .collect(toList());

        List<ImportDeclaration> importDeclarations = codeLines.stream().filter(line -> line.contains("import") && !line.contains("static"))
                .map(line -> new ImportDeclaration(line.replace("import", "")))
                .collect(toList());

        List<ImportStaticDeclaration> importStaticDeclarations = codeLines.stream()
                .filter(line -> line.contains("import") && line.contains("static"))
                .map(line -> new ImportStaticDeclaration(line.replace("import static", "")))
                .collect(toList());

        List<ClassDeclaration> classDeclarations = getClasses(codeLines);


        String ASTFileName = "AST_" + javaFile.toString().replace(".java", "") + ".dot";

        MutableNode file = mutNode("File");

        MutableNode packaje = mutNode("Package");
        MutableNode impord = mutNode("Import");
        MutableNode importStatic = mutNode("Import Static");
        MutableNode classes = mutNode("Classes");

        packageDeclarations.forEach(packageDeclaration -> packaje.addLink(packageDeclaration.getNode()));
        importDeclarations.forEach(importDeclaration -> impord.addLink(importDeclaration.getNode()));
        importStaticDeclarations
                .forEach(importStaticDeclaration -> importStatic.addLink(importStaticDeclaration.getNode()));

        classDeclarations.forEach(classDeclaration -> classes.addLink(classDeclaration.getNode()));

        file.addLink(packaje, impord, importStatic, classes);

        MutableGraph mutableGraph = mutGraph(javaFile.toString()).add(file.setName("File"));
        Graphviz.fromGraph(mutableGraph).render(Format.DOT).toFile(Paths.get("ASTs",ASTFileName).toFile());
    }

    private static List<ClassDeclaration> getClasses(List<String> codeLines) {
        List<String> classLines = codeLines.stream()
                .filter(line -> !line.contains("package") && !line.contains("import") && !line.contains("import static"))
                .collect(Collectors.toList());


        String classes = join("", classLines);
        classes = classes.replace("class", "|class");
        classes = classes.replace("}}", "}}|");
        return attachModifiers(stream(classes.split("\\|")).collect(toList()))
                .stream()
                .map(JavaParser::getClassDeclaration)
                .collect(toList());
    }

    private static ClassDeclaration getClassDeclaration(String clazz) {
        List<String> modifiersNameBodyOfClass = stream(clazz.split("class")).collect(toList());
        String modifiers = modifiersNameBodyOfClass.get(0);
        String nameAndBody = modifiersNameBodyOfClass.get(1);
        int i = nameAndBody.indexOf("{");
        String name = nameAndBody.substring(0, i);
        String body = nameAndBody.substring(i);
        List<String> modifierList = stream(modifiers.split(" ")).collect(toList());
        return new ClassDeclaration(name, modifierList, body);

    }

    private static List<String> attachModifiers(List<String> classesAndModifiers) {
        for (int i = 0; i < classesAndModifiers.size(); i++) {
            String potentialModifers = classesAndModifiers.get(i);
            if (!potentialModifers.contains("class")) {
                String clazz = classesAndModifiers.get(i + 1);
                clazz = potentialModifers + clazz;
                classesAndModifiers.set(i + 1, clazz);
            }

        }
        return classesAndModifiers.stream().filter(line -> line.contains("class")).collect(toList());
    }
}

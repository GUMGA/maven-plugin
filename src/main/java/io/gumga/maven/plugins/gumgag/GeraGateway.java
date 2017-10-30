package io.gumga.maven.plugins.gumgag;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author munif
 */
@Mojo(name = "gateway", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraGateway extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade
     */
    @Parameter(property = "entidade", defaultValue = "all")
    private String nomeCompletoEntidade;
    private String nomePacoteGateway;
    private String nomePacoteDto;
    private String nomePacoteTranslator;
    private String nomePacoteBase;
    private String nomeEntidade;
    private String pastaGateway;
    private String pastaDto;
    private String pastaTranslator;
    private URLClassLoader classLoader;
    private Class classeEntidade;
    private Field[] atributos;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());
        System.out.println("nomeCompletoEntidade:"+nomeCompletoEntidade);
        nomePacoteBase = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf("."));
        System.out.println(nomePacoteBase);
        nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

        nomePacoteGateway = nomePacoteBase + ".gateway";
        nomePacoteDto = nomePacoteGateway + ".dto";
        nomePacoteTranslator = nomePacoteGateway + ".translator";

        pastaGateway = Util.windowsSafe(project.getCompileSourceRoots().get(0)) + "/".concat(nomePacoteGateway.replaceAll("\\.", "/"));
        pastaDto = Util.windowsSafe(project.getCompileSourceRoots().get(0)) + "/".concat(nomePacoteDto.replaceAll("\\.", "/"));
        pastaTranslator = Util.windowsSafe(project.getCompileSourceRoots().get(0)) + "/".concat(nomePacoteTranslator.replaceAll("\\.", "/"));
        getLog().info("Iniciando plugin Gerador de Gateway");
        getLog().info("Gerando para " + nomeEntidade);

        incializaClassLoader();
        try {
            classeEntidade = classLoader.loadClass(nomeCompletoEntidade);
            atributos = classeEntidade.getDeclaredFields(); //TODO Pensar em Herança
            geraDto();
            geraGateway();
            geraTranslator();
        } catch (ClassNotFoundException ex) {
            getLog().error(ex);
        }

    }

    private void geraGateway() {
        File f = new File(pastaGateway);
        f.mkdirs();
        File arquivoClasse = new File(pastaGateway + "/" + nomeEntidade + "Gateway.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + "package " + nomePacoteGateway + ";\n"
                    + "\n"
                    + "import org.springframework.stereotype.Component;\n"
                    + "\n"
                    + "import io.gumga.presentation.GumgaGateway;\n"
                    + "\n"
                    + "@Component\n"
                    + "public class " + nomeEntidade + "Gateway extends GumgaGateway<" + nomeCompletoEntidade + ", String, " + nomePacoteDto + "." + nomeEntidade + "DTO> {\n"
                    + "\n"
                    + "}\n"
                    + "\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void geraDto() {
        File f = new File(pastaDto);
        f.mkdirs();
        File arquivoClasse = new File(pastaDto + "/" + nomeEntidade + "DTO.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + "package " + nomePacoteDto + ";\n"
                    + "\n"
                    + "public class " + nomeEntidade + "DTO {\n"
                    + "\n"
                    + "    public java.lang.Long id;\n"
                    + "\n");

            declaraAtributos(fw, atributos);
            declaraGettersSetters(fw, atributos);

            fw.write("\n"
                    + "    public java.lang.Long getId() {\n"
                    + "        return id;\n"
                    + "    }\n"
                    + "\n"
                    + "    public void setId(java.lang.Long id) {\n"
                    + "        this.id = id;\n"
                    + "    }\n"
                    + "\n"
                    + "}\n"
                    + "\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    public void declaraAtributos(FileWriter fw, Field[] atributos) throws IOException {
        for (Field atributo : atributos) {
            if (atributo.getType().getCanonicalName().startsWith("java") || atributo.getType().isPrimitive()) {
                criaAtributo(atributo, fw);
            }
        }
    }

    public void criaAtributo(Field f, FileWriter fw) throws IOException {
        String tipo = f.getType().getCanonicalName();
        String nome = f.getName();
        fw.write("    public " + tipo + " " + nome + ";\n");
        fw.write("\n");
    }

    private void declaraGettersSetters(FileWriter fw, Field[] atributos) throws Exception {
        for (Field atributo : atributos) {
            if (atributo.getType().getCanonicalName().startsWith("java") || atributo.getType().isPrimitive()) {
                criaGet(fw, atributo);
                criaSet(fw, atributo);
            }
        }
    }

    private void criaGet(FileWriter fw, Field atributo) throws Exception {
        String tipo = atributo.getType().getCanonicalName();
        String nome = atributo.getName();
        fw.write(""
                + "    public " + tipo + " get" + Util.primeiraMaiuscula(nome) + "() {\n"
                + "        return " + nome + ";\n"
                + "    }\n"
                + "\n");
        if (atributo.getType().equals(Boolean.class) || atributo.getType().equals(Boolean.TYPE)) { //Declara duas vezes para ser mais compatível com frameworks que utilizam is ou get para boolean
            fw.write(""
                    + "    public " + tipo + " is" + Util.primeiraMaiuscula(nome) + "() {\n"
                    + "        return " + nome + ";\n"
                    + "    }\n"
                    + "\n");

        }

    }

    private void criaSet(FileWriter fw, Field atributo) throws Exception {
        String tipo = atributo.getType().getCanonicalName();
        String nome = atributo.getName();
        fw.write(""
                + "    public void set" + Util.primeiraMaiuscula(nome) + "(" + tipo + " " + nome + ") {\n"
                + "        this." + nome + " = " + nome + ";\n"
                + "    }\n"
                + "\n");
    }

    private void geraTranslator() {
        File f = new File(pastaTranslator);
        f.mkdirs();
        File arquivoClasse = new File(pastaTranslator + "/" + nomeEntidade + "Translator.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + ""
                    + "package " + nomePacoteTranslator + ";\n"
                    + "\n"
                    + "import io.gumga.presentation.GumgaTranslator;\n"
                    + "\n"
                    + "import org.springframework.stereotype.Component;\n"
                    + "\n"
                    + "import " + nomeCompletoEntidade + ";\n"
                    + "import " + nomePacoteDto + "." + nomeEntidade + "DTO;\n"
                    + "\n"
                    + "@Component\n"
                    + "public class " + nomeEntidade + "Translator extends GumgaTranslator<" + nomeEntidade + ", " + nomeEntidade + "DTO, String> {\n"
                    + "\n"
                    + "    @Override\n"
                    + "    public " + nomeEntidade + " to(" + nomeEntidade + "DTO dto) {\n"
                    + "        " + nomeEntidade + " entidade = new " + nomeEntidade + "();\n"
                    + "\n");

            geraCopia(fw, atributos, "entidade", "dto");

            fw.write(""
                    + "\n"
                    + "        return entidade;\n"
                    + "    }\n"
                    + "\n"
                    + "    @Override\n"
                    + "    public " + nomeEntidade + "DTO from(" + nomeEntidade + " entidade) {\n"
                    + "        " + nomeEntidade + "DTO dto = new " + nomeEntidade + "DTO();\n"
                    + "\n");
            geraCopia(fw, atributos, "dto", "entidade");
            fw.write("\n"
                    + "        return dto;\n"
                    + "    }\n"
                    + "\n"
                    + "}"
                    + ""
                    + "\n"
            );

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void incializaClassLoader() {
        try {
            List elementos = new ArrayList();
            elementos.addAll(project.getRuntimeClasspathElements());
            elementos.addAll(project.getTestClasspathElements());

            URL[] runtimeUrls = new URL[elementos.size()];
            for (int i = 0; i < elementos.size(); i++) {
                String element = (String) elementos.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }
            classLoader = new URLClassLoader(runtimeUrls,
                    Thread.currentThread().getContextClassLoader());

        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void geraCopia(FileWriter fw, Field[] atributos, String destino, String origem) throws IOException {
        for (Field atributo : atributos) {
            if (atributo.getType().getCanonicalName().startsWith("java") || atributo.getType().isPrimitive()) {
                String nome = Util.primeiraMaiuscula(atributo.getName());
                fw.write("        " + destino + ".set" + nome + "(" + origem + ".get" + nome + "());\n");
            }
        }
    }

}
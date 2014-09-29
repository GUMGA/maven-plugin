/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
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
@Mojo(name = "entidade", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraEntidade extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade a ser criada
     */
    @Parameter(property = "entidade", defaultValue = "nada")
    private String nomeCompletoEntidade;

    @Parameter(property = "atributos", defaultValue = "")
    private String parametroAtributos;

    private File diretorioBase;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if ("nada".equals(nomeCompletoEntidade)) {
            Scanner entrada = new Scanner(System.in);
            System.out.print("Nome completo da entidade a ser gerada:");
            nomeCompletoEntidade = entrada.next();
        }
        diretorioBase = project.getBasedir();
        String nomePacote = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf('.'));
        String nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);
        String pastaClasse = project.getCompileSourceRoots().get(0) + "/".concat(nomePacote.replaceAll("\\.", "/"));

        getLog().info("Iniciando plugin Gerador de Entidade GUMGA ");
        getLog().info("Gerando " + nomePacote + "." + nomeEntidade);
        File f = new File(pastaClasse);
        f.mkdirs();
        File arquivoClasse = new File(pastaClasse + "/" + nomeEntidade + ".java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write("package " + nomePacote + ";\n\n");
            fw.write("import gumga.framework.domain.GumgaModel;\n"
                    + "import java.io.Serializable;\n"
                    + "import java.util.*;\n"
                    + "import javax.persistence.Entity;\n"
                    + "\n");

            fw.write("@Entity\n");
            fw.write("public class " + nomeEntidade + " extends GumgaModel<Long> implements Serializable {\n\n");

            escreveAtributos(fw);

            fw.write("}\n");
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void escreveAtributos(FileWriter fw) throws Exception {
        if (parametroAtributos == null || parametroAtributos.isEmpty()) {
            return;
        }
        String atributos[] = parametroAtributos.split(",");
        declaraAtributos(atributos, fw);
        declaraGettersESetters(atributos, fw);
    }

    public void declaraGettersESetters(String[] atributos, FileWriter fw) throws Exception {
        for (String atributo : atributos) {
            criaGet(fw, atributo);
            criaSet(fw, atributo);
        }
    }

    public void declaraAtributos(String[] atributos, FileWriter fw) throws IOException {
        for (String atributo : atributos) {
            String partes[] = atributo.split(":");
            fw.write("    private " + partes[1] + " " + partes[0] + ";\n");
        }
        fw.write("\n\n");
    }

    private void criaGet(FileWriter fw, String atributo) throws Exception {
        String partes[] = atributo.split(":");
        fw.write(""
                + "    public " + partes[1] + " get" + Util.primeiraMaiuscula(partes[0]) + "() {\n"
                + "        return " + partes[0] + ";\n"
                + "    }\n"
                + "\n");
    }

    private void criaSet(FileWriter fw, String atributo) throws Exception {
        String partes[] = atributo.split(":");
        fw.write(""
                + "    public void set" + Util.primeiraMaiuscula(partes[0]) + "(" + partes[1] + " " + partes[0] + ") {\n"
                + "        this." + partes[0] + " = " + partes[0] + ";\n"
                + "    }\n"
                + "\n");
    }

}

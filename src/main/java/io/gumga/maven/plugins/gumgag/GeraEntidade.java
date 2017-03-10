package io.gumga.maven.plugins.gumgag;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import io.gumga.freemarker.Attribute;
import io.gumga.freemarker.ConfigurationFreeMarker;
import io.gumga.freemarker.TemplateFreeMarker;

/**
 *
 * @author munif
 */
@Mojo(name = "entidade", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraEntidade extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade
     */
    @Parameter(property = "entidade", defaultValue = "nada")
    private String nomeCompletoEntidade;

    @Parameter(property = "atributos", defaultValue = "")
    private String parametroAtributos;

    @Parameter(property = "super", defaultValue = "GumgaModel<Long>")
    private String superClasse;
    private String nomeEntidade;
    private String nomePacote;
    private String pastaClasse;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());

        if ("nada".equals(nomeCompletoEntidade)) {
            Scanner entrada = new Scanner(System.in);
            System.out.print("Nome completo da entidade a ser gerada:");
            nomeCompletoEntidade = entrada.next();
        }

        nomePacote = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf('.'));
        nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);
        pastaClasse = Util.windowsSafe(project.getCompileSourceRoots().get(0))
                + "/".concat(nomePacote.replaceAll("\\.", "/"));

        getLog().info("Iniciando plugin Gerador de Entidade GUMGA ");
        getLog().info("Gerando " + nomePacote + "." + nomeEntidade);
        File f = new File(pastaClasse);
        f.mkdirs();
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker("entity.ftl", config);
            template.add("package", this.nomePacote);
            template.add("entityName", this.nomeEntidade);
            template.add("superClass", this.superClasse);

            List<Attribute> attributes = new ArrayList<>();
            for (String attribute : this.getAttributes()) {
                String[] parts = attribute.split(":");

                attributes.add(createAttribute(parts));
            }

            template.add("attributes", attributes);
            template.generateTemplate(this.pastaClasse + "/" + this.nomeEntidade + ".java");

        } catch (Exception ex) {
            getLog().error(ex);
        }
    }
    @Enumerated(EnumType.STRING)
    private Attribute createAttribute(String[] parts) {
        Boolean oneToMany = false;
        Boolean oneToOne = false;
        Boolean manyToOne = false;
        Boolean manyToMany = false;
        Boolean required = false;
        Boolean enumString = false;
        Boolean enumOrdinal = false;

        if (parts.length > 2) {
            for (int i = 2; i < parts.length; i++) {
                if (this.isRequired(parts[i])) {
                    required = Boolean.valueOf(parts[i]);
                } else if (this.isOneToMany(parts[i])) {
                    oneToMany = true;
                } else if (this.isOneToOne(parts[i])) {
                    oneToOne = true;
                } else if (this.isManyToOne(parts[i])) {
                    manyToOne = true;
                } else if (this.isManyToMany(parts[i])) {
                    manyToMany = true;
                } else if (this.isEnumString(parts[i])) {
                    enumString = true;
                } else if (this.isEnumOrdinal(parts[i])) {
                    enumOrdinal = true;
                }
            }
        }

        return new Attribute(parts[0], parts[1], Util.primeiraMaiuscula(parts[0]), oneToMany, oneToOne, manyToOne, manyToMany, required, enumString, enumOrdinal);
    }

    private boolean isEnumString(String enumString) {
        return enumString.trim().equalsIgnoreCase("@Enumerated(EnumType.STRING)") ? true : false;
    }

    private boolean isEnumOrdinal(String enumOrdinal) {
        return enumOrdinal.trim().equalsIgnoreCase("@Enumerated(EnumType.ORDINAL)") || enumOrdinal.trim().equalsIgnoreCase("@Enumerated") ? true : false;
    }

    private boolean isOneToMany(String oneToMany) {
        return oneToMany.trim().equalsIgnoreCase("@OneToMany") ? true : false;
    }

    private boolean isOneToOne(String oneToOne) {
        return oneToOne.trim().equalsIgnoreCase("@OneToOne") ? true : false;
    }

    private boolean isManyToOne(String manyToOne) {
        return manyToOne.trim().equalsIgnoreCase("@ManyToOne") ? true : false;
    }

    private boolean isManyToMany(String manyToMany) {
        return manyToMany.trim().equalsIgnoreCase("@ManyToMany") ? true : false;
    }

    private boolean isRequired(String required) {
        return isEqualsTrue(required) || isEqualsFalse(required) ? true : false;
    }

    private boolean isEqualsFalse(String required) {
        return "false".trim().equalsIgnoreCase(required);
    }

    private boolean isEqualsTrue(String required) {
        return "true".trim().equalsIgnoreCase(required);
    }

    private String[] getAttributes() {
        if (this.parametroAtributos == null || this.parametroAtributos.isEmpty()) {
            return new String[]{};
        }

        return this.parametroAtributos.split(",");
    }

    private void escreveAtributos(FileWriter fw) throws Exception {
        if (parametroAtributos == null || parametroAtributos.isEmpty()) {
            return;
        }
        String atributos[] = parametroAtributos.split(",");
        declaraAtributos(atributos, fw);
        declaraConstrutor(fw);
        declaraGettersESetters(atributos, fw);
    }

    private void declaraConstrutor(FileWriter fw) throws IOException {
        fw.write("" + Util.IDENTACAO04 + "public " + nomeEntidade + "(){\n" + Util.IDENTACAO04 + "}\n" + "\n");
    }

    public void declaraGettersESetters(String[] atributos, FileWriter fw) throws Exception {
        for (String atributo : atributos) {
            criaGet(fw, atributo);
            criaSet(fw, atributo);
        }
    }

    public void declaraAtributos(String[] atributos, FileWriter fw) throws IOException {
        for (String atributo : atributos) {
            System.out.println("---->" + atributo);
            fw.write(Util.IDENTACAO04 + "//@Field //Descomente para ser utilizado na busca multientidades\n");
            String partes[] = atributo.split(":");
            if (partes[1].trim().endsWith("GumgaAddress")) {
                fw.write("" + "     @Columns(columns = {\n" + "     @Column(name = \"" + partes[0] + "_zip_code\"),\n"
                        + "     @Column(name = \"" + partes[0] + "_premisse_type\"),\n" + "     @Column(name = \""
                        + partes[0] + "_premisse\"),\n" + "     @Column(name = \"" + partes[0] + "_number\"),\n"
                        + "     @Column(name = \"" + partes[0] + "_information\"),\n" + "     @Column(name = \""
                        + partes[0] + "_neighbourhood\"),\n" + "     @Column(name = \"" + partes[0]
                        + "_localization\"),\n" + "     @Column(name = \"" + partes[0] + "_state\"),\n"
                        + "     @Column(name = \"" + partes[0] + "_country\")\n" + "     })" + "\n");
            }
            if (partes[1].trim().endsWith("GumgaTime")) {

            }
            if (partes[1].trim().endsWith("GumgaGeoLocation")) {
                fw.write("     @Columns(columns = {\n" + "     @Column(name = \"" + partes[0] + "_latitude\"),\n"
                        + "     @Column(name = \"" + partes[0] + "_longitude\")\n" + "     })" + "\n");
            }
            if (partes[1].trim().endsWith("GumgaFile") || partes[1].trim().endsWith("GumgaImage")) {
                fw.write("     @Columns(columns = {\n" + "     @Column(name = \"" + partes[0] + "_name\"),\n"
                        + "     @Column(name = \"" + partes[0] + "_size\"),\n" + "     @Column(name = \"" + partes[0]
                        + "_type\"),\n" + "     @Column(name = \"" + partes[0] + "_bytes\",length = 50*1024*1024)\n"
                        + "     })" + "\n");
            }
            if (partes.length > 2) {
                // Verifica se eh required
                if ("true".equalsIgnoreCase(partes[2]) || "false".equalsIgnoreCase(partes[2])) {
                    Boolean required = Boolean.valueOf(partes[2]);
                    if (required) {
                        fw.write("    @NotNull\n");
                    }
                } else {
                    // Adiciona o mapeamento
                    fw.write("    " + partes[2]);

                    if (partes[2].trim().equals("@OneToMany")) {
                        fw.write("(cascade = CascadeType.ALL, orphanRemoval = true)");
                    }

                    fw.write("\n");
                    if (partes[2].trim().contains("mappedBy")) {
                        fw.write("    @JsonIgnore\n");
                    }
                }
            }
            fw.write(Util.IDENTACAO04 + "private " + partes[1] + " " + partes[0] + ";\n");
        }
        fw.write("\n");
    }

    private void criaGet(FileWriter fw, String atributo) throws Exception {
        String partes[] = atributo.split(":");
        fw.write("" + "    public " + partes[1] + " get" + Util.primeiraMaiuscula(partes[0]) + "() {\n"
                + "        return " + partes[0] + ";\n" + "    }\n" + "\n");
        if ("boolean".equals(partes[1].trim()) || "Boolean".equals(partes[1].trim())) {
            fw.write("" + "    public " + partes[1] + " is" + Util.primeiraMaiuscula(partes[0]) + "() {\n"
                    + "        return " + partes[0] + ";\n" + "    }\n" + "\n");
        }
    }

    private void criaSet(FileWriter fw, String atributo) throws Exception {
        String partes[] = atributo.split(":");
        fw.write("" + "    public void set" + Util.primeiraMaiuscula(partes[0]) + "(" + partes[1] + " " + partes[0]
                + ") {\n" + "        this." + partes[0] + " = " + partes[0] + ";\n" + "    }\n" + "\n");
    }

}

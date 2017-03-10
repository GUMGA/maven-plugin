package io.gumga.maven.plugins.gumgag;

import io.gumga.freemarker.Attribute;
import io.gumga.freemarker.ConfigurationFreeMarker;
import io.gumga.freemarker.TemplateFreeMarker;
import io.gumga.domain.domains.GumgaImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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
@Mojo(name = "api", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraAPI extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * Entidade
     */
    @Parameter(property = "entidade", defaultValue = "all")
    private String nomeCompletoEntidade;
    private String nomePacoteBase;
    private String nomeEntidade;
    private String nomePacoteApi;
    private String pastaApi;

    private Class classeEntidade;

    private Set<Field> gumgaImages;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());

        try {
            nomePacoteBase = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf(".domain"));
            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);
            nomePacoteApi = nomePacoteBase + ".api";
            pastaApi = Util.windowsSafe(project.getCompileSourceRoots().get(0)) + "/".concat(nomePacoteApi.replaceAll("\\.", "/"));
            getLog().info("Iniciando plugin Gerador de API ");
            getLog().info("Gerando para " + nomeEntidade);
            classeEntidade = Util.getClassLoader(project).loadClass(nomeCompletoEntidade);
            gumgaImages = new HashSet<>();
            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                if (GumgaImage.class.equals(atributo.getType())) {
                    gumgaImages.add(atributo);
                }
            }
            geraApi();
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void geraApi() {
        File f = new File(pastaApi);
        f.mkdirs();
        try {
            ConfigurationFreeMarker config = new ConfigurationFreeMarker();
            TemplateFreeMarker template = new TemplateFreeMarker("api.ftl", config);

            template.add("packageName", this.nomePacoteApi);
            template.add("packageBase", this.nomePacoteBase);
            template.add("entityName", this.nomeEntidade);
            template.add("completeNameEntity", this.nomeCompletoEntidade);
            template.add("uriBase", "/api/" + this.nomeEntidade.toLowerCase());
            template.add("gumgaImage", !gumgaImages.isEmpty());

            List<String> attributes = new ArrayList<>();
            for (Field field : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if ((field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) && attributes.isEmpty()) {
                    attributes.add(this.nomeEntidade);
                }
            }

            template.add("attributes", attributes);

            List<Attribute> attributesImage = new ArrayList<>();
            for (Field field : gumgaImages) {
                attributesImage.add(new Attribute(field.getName(), "", Util.primeiraMaiuscula(field.getName()), false, false, false, false, false, false, false));
            }
            template.add("gumgaImages", attributesImage);

            template.generateTemplate(this.pastaApi + "/" + this.nomeEntidade + "API.java");

        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void sobrecarregaLoad(FileWriter fw) throws IOException {
        List<Field> todosAtributos = Util.getTodosAtributosNaoEstaticos(classeEntidade);
        for (Field f : todosAtributos) {
            if (f.isAnnotationPresent(OneToMany.class) || f.isAnnotationPresent(ManyToMany.class)) {
                fw.write(""
                        + "    @Override\n"
                        + "    public " + nomeEntidade + " load(@PathVariable Long id) {\n"
                        + "        return ((" + nomeEntidade + "Service)service).load" + nomeEntidade + "Fat(id);\n"
                        + "    }\n"
                        + "\n");
                return;
            }
        }

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.io.FileWriter;
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
@Mojo(name = "apresentacao", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraPresentation extends AbstractMojo {

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
    private String nomePacoteWeb;
    private String pastaApi;
    private String pastaWeb;
    private String pastaJSP;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        nomePacoteBase = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf(".domain"));
        nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

        nomePacoteApi = nomePacoteBase + ".presentation.api";
        nomePacoteWeb = nomePacoteBase + ".presentation.web";

        pastaApi = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteApi.replaceAll("\\.", "/"));
        pastaWeb = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteWeb.replaceAll("\\.", "/"));
        pastaJSP = project.getFile().getParent()+"/src/main/webapp/WEB-INF/views/crud/" + (nomeEntidade.toLowerCase());

        getLog().info("Iniciando plugin Gerador de Classes de Apresentação ");
        getLog().info("Gerando para " + nomeEntidade);

        geraWeb();
        geraApi();
        geraJSPs();

    }

    private void geraApi() {
        File f = new File(pastaApi);
        f.mkdirs();
        File arquivoClasse = new File(pastaApi + "/" + nomeEntidade + "API.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + "package " + nomePacoteApi + ";\n"
                    + "\n"
                    + "import " + nomeCompletoEntidade + ";\n"
                    + "import gumga.framework.application.GumgaService;\n"
                    + "import gumga.framework.presentation.GumgaAPI;\n"
                    + "\n"
                    + "import org.springframework.beans.factory.annotation.Autowired;\n"
                    + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                    + "import org.springframework.web.bind.annotation.RestController;\n"
                    + "\n"
                    + "@RestController\n"
                    + "@RequestMapping(\"/api/" + nomeEntidade.toLowerCase() + "\")\n"
                    + "public class " + nomeEntidade + "API extends GumgaAPI<" + nomeEntidade + ", Long> {\n"
                    + "\n"
                    + "    @Autowired\n"
                    + "    public " + nomeEntidade + "API(GumgaService<" + nomeEntidade + ", Long> service) {\n"
                    + "        super(service);\n"
                    + "    }\n"
                    + "\n"
                    + "}"
                    + "\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void geraWeb() {
        File f = new File(pastaWeb);
        f.mkdirs();
        File arquivoClasse = new File(pastaWeb + "/" + nomeEntidade + "Controller.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + "package " + nomePacoteWeb + ";\n"
                    + "\n"
                    + "import gumga.framework.presentation.GumgaCRUDController;\n"
                    + "import org.springframework.stereotype.Controller;\n"
                    + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                    + "\n"
                    + "@Controller\n"
                    + "@RequestMapping(\"/" + nomeEntidade.toLowerCase() + "\")\n"
                    + "public class " + nomeEntidade + "Controller extends GumgaCRUDController {\n"
                    + "\n"
                    + "	@Override\n"
                    + "	public String path() {\n"
                    + "		return \"crud/" + nomeEntidade + "\";\n"
                    + "	}\n"
                    + "\n"
                    + "}\n"
                    + ""
                    + "\n");

            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void geraJSPs() {
        getLog().info(pastaJSP);
        File f = new File(pastaJSP);
        f.mkdirs();

        try {
            File arquivoBase = new File(pastaJSP + "/base.jsp");
            FileWriter fwBase = new FileWriter(arquivoBase);
            fwBase.write(""
                    + ""
                    + "<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%>\n"
                    + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\"%>\n"
                    + "\n"
                    + "<g:basetemplate init=\"app/produto/module\" title=\"Cadastro de " + nomeEntidade + "\" openMenu=\"" + nomeEntidade.toLowerCase() + "\">\n"
                    + "	<div ui-view></div>\n"
                    + "</g:basetemplate>"
                    + "");
            fwBase.close();

            File arquivoForm = new File(pastaJSP + "/form.jsp");
            FileWriter fwForm = new FileWriter(arquivoForm);
            fwForm.write(""
                    + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\" %>\n"
                    + "<g:form>\n"
                    + "\n"
                    + "	<div class=\"form-group\" gumga-form-group=\"descricao\">\n"
                    + "		<label class=\"control-label\">Descricao</label>\n"
                    + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity.descricao\" required=\"true\" autofocus />\n"
                    + "		<gumga:input:errors field=\"descricao\"></gumga:input:errors>\n"
                    + "	</div>\n"
                    + "	\n"
                    + "</g:form>"
                    + "");
            fwForm.close();

            File arquivoList = new File(pastaJSP + "/list.jsp");
            FileWriter fwList = new FileWriter(arquivoList);
            fwList.write(""
                    + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\" %>\n"
                    + "\n"
                    + "<g:grid values=\"list.values\">\n"
                    + "    <jsp:attribute name=\"searchFields\">\n"
                    + "        <gumga:search:field field=\"descricao\" label=\"Descricao\" selected=\"true\"></gumga:search:field>\n"
                    + "        </jsp:attribute>\n"
                    + "\n"
                    + "    <jsp:attribute name=\"gridColumns\">\n"
                    + "        <gumga:column sort-field=\"descricao\" label=\"Descrição\">{{$value.descricao}}</gumga:column>\n"
                    + "        <gumga:column label=\"\">\n"
                    + "            <div class=\"text-right\">\n"
                    + "                <a href=\"#/edit/{{$value.id}}\" class=\"btn btn-primary\" title=\"Editar\">\n"
                    + "                    <i class=\"glyphicon glyphicon-pencil\"></i>\n"
                    + "                </a>\n"
                    + "            </div>\n"
                    + "        </gumga:column>\n"
                    + "    </jsp:attribute>\n"
                    + "</g:grid>"
                    + "");
            fwList.close();

        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
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

    private Class classeEntidade;
    private String pastaScripts;
    private String pastaResources;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            nomePacoteBase = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf(".domain"));
            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

            nomePacoteApi = nomePacoteBase + ".presentation.api";
            nomePacoteWeb = nomePacoteBase + ".presentation.web";

            pastaApi = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteApi.replaceAll("\\.", "/"));
            pastaWeb = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteWeb.replaceAll("\\.", "/"));
            pastaJSP = project.getFile().getParent() + "/src/main/webapp/WEB-INF/views/crud/" + (nomeEntidade.toLowerCase());
            pastaScripts = project.getFile().getParent() + "/src/main/webapp/WEB-INF/static/scripts/app//" + (nomeEntidade.toLowerCase());
            pastaResources = project.getFile().getParent() + "/src/main/resources/";

            getLog().info("Iniciando plugin Gerador de Classes de Apresentação ");
            getLog().info("Gerando para " + nomeEntidade);

            classeEntidade = Util.getClassLoader(project).loadClass(nomeCompletoEntidade);

            geraWeb();
            geraApi();
            geraJSPs();
            geraScripts();
            adicionaAoMenu();
        } catch (Exception ex) {
            getLog().error(ex);
        }

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
                    + "		return \"crud/" + nomeEntidade.toLowerCase() + "\";\n"
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
                    + "<g:basetemplate init=\"app/" + nomeEntidade.toLowerCase() + "/module\" title=\"Cadastro de " + nomeEntidade + "\" openMenu=\"" + nomeEntidade.toLowerCase() + "\">\n"
                    + "	<div ui-view></div>\n"
                    + "</g:basetemplate>"
                    + "");
            fwBase.close();

            File arquivoForm = new File(pastaJSP + "/form.jsp");
            FileWriter fwForm = new FileWriter(arquivoForm);
            fwForm.write(""
                    + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\" %>\n"
                    + "<g:form>\n"
                    + "\n");

            boolean primeiro = true;
            for (Field atributo : classeEntidade.getDeclaredFields()) {

                Class<?> type = atributo.getType();
                String nomeAtributo = atributo.getName();
                String etiqueta = Util.primeiraMaiuscula(nomeAtributo);

                boolean requerido = true;

                if (Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
                    fwForm.write(""
                            + "    <div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                            + "        <label><input type=\"checkbox\" name=\"" + nomeAtributo + "\" ng-model=\"entity." + nomeAtributo + "\" /> " + etiqueta + "</label>\n"
                            + "        <gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                            + "    </div>"
                            + ""
                    );
                } else if (BigDecimal.class.equals(type)) {
                    fwForm.write(""
                            + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                            + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                            + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + "\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " gumga-number decimal-places=\"2\" />\n"
                            + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                            + "	</div>\n");
                } else {
                    fwForm.write(""
                            + "	<div class=\"form-group\" gumga-form-group=\"" + nomeAtributo + "\">\n"
                            + "		<label class=\"control-label\">" + etiqueta + "</label>\n"
                            + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"entity." + nomeAtributo + "\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
                            + "		<gumga:input:errors field=\"" + nomeAtributo + "\"></gumga:input:errors>\n"
                            + "	</div>\n");
                }
                primeiro = false;
            }

            fwForm.write(""
                    + "	\n"
                    + "</g:form>"
                    + "");
            fwForm.close();

            File arquivoList = new File(pastaJSP + "/list.jsp");
            FileWriter fwList = new FileWriter(arquivoList);
            Field primeiroAtributo = classeEntidade.getDeclaredFields()[0];
            String nomeAtributo = primeiroAtributo.getName();
            String etiqueta = Util.primeiraMaiuscula(nomeAtributo);

            fwList.write(""
                    + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\" %>\n"
                    + "\n"
                    + "<g:grid values=\"list.values\">\n"
                    + "    <jsp:attribute name=\"searchFields\">\n"
                    + "        <gumga:search:field field=\"" + nomeAtributo + "\" label=\"" + etiqueta + "\" selected=\"true\"></gumga:search:field>\n"
                    + "        </jsp:attribute>\n"
                    + "\n"
                    + "    <jsp:attribute name=\"gridColumns\">\n"
                    + "        <gumga:column sort-field=\"" + nomeAtributo + "\" label=\"" + etiqueta + "\">{{$value." + nomeAtributo + "}}</gumga:column>\n"
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

    private void geraScripts() {
        try {
            File f = new File(pastaScripts);
            f.mkdirs();
            File arquivoModule = new File(pastaScripts + "/module.js");
            FileWriter fwModule = new FileWriter(arquivoModule);

            fwModule.write(""
                    + "define(function(require) {\n"
                    + "	\n"
                    + "	require('gumga-components');\n"
                    + "	require('app-commons/modules/crud-module').constant('baseTemplateURL', '" + nomeEntidade.toLowerCase() + "');\n"
                    + "	\n"
                    + "	return require('angular')\n"
                    + "		.module('app." + nomeEntidade.toLowerCase() + "', [\"app.base.crud\", 'gumga.components'])\n"
                    + "		\n"
                    + "		.service('EntityService', require('app/" + nomeEntidade.toLowerCase() + "/service'))\n"
                    + "		\n"
                    + "		.controller(\"ListController\", require('app/" + nomeEntidade.toLowerCase() + "/controllers/list'))\n"
                    + "		.controller(\"FormController\", require('app/" + nomeEntidade.toLowerCase() + "/controllers/form'));\n"
                    + "	\n"
                    + "});\n"
                    + "");

            fwModule.close();

            File arquivoService = new File(pastaScripts + "/service.js");
            FileWriter fwService = new FileWriter(arquivoService);

            fwService.write(""
                    + "define([\n"
                    + "		'gumga-class',\n"
                    + "		'gumga/services/basic-crud-service'\n"
                    + "	], function(GumgaClass, BasicCrudService) {\n"
                    + "\n"
                    + "	\n"
                    + "	function " + nomeEntidade + "Service($http, $q) {\n"
                    + "		" + nomeEntidade + "Service.super.constructor.call(this, $http, $q, \"api/" + nomeEntidade.toLowerCase() + "\");\n"
                    + "	}\n"
                    + "\n"
                    + "	return GumgaClass.create({\n"
                    + "		constructor : " + nomeEntidade + "Service,\n"
                    + "		extends : BasicCrudService\n"
                    + "	});\n"
                    + "	\n"
                    + "});\n"
                    + "");

            fwService.close();

            String pastaControllers = pastaScripts + "/controllers";
            f = new File(pastaControllers);
            f.mkdirs();

            File arquivoForm = new File(pastaControllers + "/form.js");
            FileWriter fwForm = new FileWriter(arquivoForm);

            fwForm.write(""
                    + "define(function(require) {\n"
                    + "\n"
                    + "	return require('angular-class').create({\n"
                    + "		extends : require('app-commons/controllers/basic-form-controller'),\n"
                    + "		prototype : {\n"
                    + "\n"
                    + "			initialize : function() {\n"
                    + "				// Inicialização do controller\n"
                    + "			}\n"
                    + "	\n"
                    + "			// Demais métodos do controller\n"
                    + "\n"
                    + "		}\n"
                    + "	});\n"
                    + "});\n"
                    + "");

            fwForm.close();

            File arquivoList = new File(pastaControllers + "/list.js");
            FileWriter fwList = new FileWriter(arquivoList);

            fwList.write(""
                    + "define(function(require) {\n"
                    + "\n"
                    + "	return require('angular-class').create({\n"
                    + "		$inject : [],\n"
                    + "		extends : require('app-commons/controllers/basic-list-controller'),\n"
                    + "		prototype : {\n"
                    + "\n"
                    + "			initialize : function() {\n"
                    + "				// Inicialização do controller\n"
                    + "			}\n"
                    + "\n"
                    + "			// Demais métodos do controller\n"
                    + "\n"
                    + "		}\n"
                    + "	});\n"
                    + "});\n"
                    + "");

            fwList.close();

        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void adicionaAoMenu() {
        try {
            File arquivoMenu = new File(pastaResources + "/menu.config");
            System.out.println("---------------------"+arquivoMenu.getAbsolutePath());
            FileWriter fwMenu = new FileWriter(arquivoMenu, true);

            fwMenu.write("" + nomeEntidade + " { url=\"" + nomeEntidade.toLowerCase() + "\" id=\"" + nomeEntidade.toLowerCase() + "\" }\n");
            fwMenu.close();

        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import gumga.framework.domain.domains.GumgaAddress;
import gumga.framework.domain.domains.GumgaBarCode;
import gumga.framework.domain.domains.GumgaBoolean;
import gumga.framework.domain.domains.GumgaCEP;
import gumga.framework.domain.domains.GumgaCNPJ;
import gumga.framework.domain.domains.GumgaCPF;
import gumga.framework.domain.domains.GumgaEMail;
import gumga.framework.domain.domains.GumgaFile;
import gumga.framework.domain.domains.GumgaGeoLocation;
import gumga.framework.domain.domains.GumgaIP4;
import gumga.framework.domain.domains.GumgaIP6;
import gumga.framework.domain.domains.GumgaImage;
import gumga.framework.domain.domains.GumgaMoney;
import gumga.framework.domain.domains.GumgaMultiLineString;
import gumga.framework.domain.domains.GumgaPhoneNumber;
import gumga.framework.domain.domains.GumgaTime;
import gumga.framework.domain.domains.GumgaURL;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

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
    @Parameter(property = "override", defaultValue = "false")
    private boolean override;

    private String nomeEntidade;

    private Class classeEntidade;

    private Set<Class> dependenciasManyToOne;
    private Set<Class> dependenciasOneToMany;
    private Set<Class> dependenciasManyToMany;
    private Set<Field> atributosGumgaImage;
    private Set<Class> dependenciasEnums;

    private String pastaApp;
    private String pastaControllers;
    private String pastaServices;
    private String pastaViews;
    private String pastaI18n;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());
        if (override) {
            System.out.println("NAO ADICIONANDO AO MENU NEM AO INTERNACIONALIZACAO");
        }

        try {
            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

            pastaApp = Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/modules/" + (nomeEntidade.toLowerCase());

            getLog().info("Iniciando plugin Gerador de Html e JavaScript de Apresentação oi");
            getLog().info("Gerando para " + nomeEntidade);

            File f = new File(pastaApp);
            f.mkdirs();
            pastaControllers = pastaApp + "/controllers";
            pastaServices = pastaApp + "/services";
            pastaViews = pastaApp + "/views";
            pastaI18n = Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/i18n/";
            new File(pastaControllers).mkdirs();
            new File(pastaServices).mkdirs();
            new File(pastaViews).mkdirs();
            new File(pastaI18n).mkdirs();

            classeEntidade = Util.getClassLoader(project).loadClass(nomeCompletoEntidade);

            dependenciasManyToOne = new HashSet<>();
            dependenciasManyToMany = new HashSet<>();
            dependenciasOneToMany = new HashSet<>();
            atributosGumgaImage = new HashSet<>();
            dependenciasEnums = new HashSet<>();

            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                if (atributo.isAnnotationPresent(OneToOne.class)) {
                    dependenciasManyToOne.add(atributo.getType());
                }
                if (atributo.isAnnotationPresent(ManyToOne.class)) {
                    dependenciasManyToOne.add(atributo.getType());
                }
                if (atributo.isAnnotationPresent(ManyToMany.class)) {
                    dependenciasManyToMany.add(Util.getTipoGenerico(atributo));
                }
                if (atributo.isAnnotationPresent(OneToMany.class)) {
                    dependenciasOneToMany.add(Util.getTipoGenerico(atributo));
                }
                if (atributo.getType().isEnum()) {
                    dependenciasEnums.add(atributo.getType());
                }
                if (atributo.getType().equals(GumgaImage.class)) {
                    atributosGumgaImage.add(atributo);
                }

            }
            geraModule();
            geraServices();
            geraControllers();
            geraViews();

            if (!override) {
                geraI18n();
            }
            if (!override) {
                adicionaAoMenu();
            }
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void adicionaAoMenu() throws IOException {

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/gumga-menu.json", "{",
                "    {\n"
                + "        \"label\": \"" + nomeEntidade + "\",\n"
                + "        \"URL\": \"" + nomeEntidade.toLowerCase() + ".list\",\n"
                + "        \"key\": \"CRUD-" + nomeEntidade + "\",\n"
                + "        \"icon\": \"glyphicon glyphicon-user\",\n"
                + "        \"icon_color\": \"\",\n"
                + "        \"imageUrl\": \"\",\n"
                + "        \"imageWidth\": \"\",\n"
                + "        \"imageHeight\": \"\",\n"
                + "        \"filhos\": [\n"
                + "             {\n"
                + "             \"label\": \" Inserir \",\n"
                + "             \"URL\": \"" + nomeEntidade.toLowerCase() + ".insert\",\n"
                + "             \"key\": \"CRUD-" + nomeEntidade + "\",\n"
                + "             \"icon\": \"glyphicon glyphicon-user\",\n"
                + "             \"filhos\": []\n"
                + "             }\n"
                + "         ]\n"
                + "    },");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMROUTE", ""
                + Util.IDENTACAO04 + Util.IDENTACAO04 + ".state('" + nomeEntidade.toLowerCase() + "', {\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + "data: {\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO04 + "id: 1\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + "}, \n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO04 + "url: '/" + nomeEntidade.toLowerCase() + "',\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO04 + "templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/base.html'\n"
                + Util.IDENTACAO04 + Util.IDENTACAO04 + "})\n"
                + "");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMREQUIRE", Util.IDENTACAO04 + "require('app/modules/" + nomeEntidade.toLowerCase() + "/module');");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMINJECTIONS", Util.IDENTACAO04 + Util.IDENTACAO04 + ",'app." + nomeEntidade.toLowerCase() + "'");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/keys.json", "]", ",\"CRUD-" + nomeEntidade + "\"");

    }

    private void geraControllers() {
        try {
            File arquivoModule = new File(pastaControllers + "/module.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define(function (require) {\n"
                    + "    var angular = require('angular');\n"
                    + "    require('app/modules/" + nomeEntidade.toLowerCase() + "/services/module');\n"
                    + "    require('angular-ui-router');\n"
                    + "\n"
                    + "    return angular.module('app." + nomeEntidade.toLowerCase() + ".controllers', ['app." + nomeEntidade.toLowerCase() + ".services','ui.router'])\n"
                    + "        .controller('" + nomeEntidade + "FormController', require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/" + nomeEntidade + "FormController'))\n"
                    + "        .controller('" + nomeEntidade + "ListController', require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/" + nomeEntidade + "ListController'));\n"
                    + "});"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            File arquivoModule = new File(pastaControllers + "/" + nomeEntidade + "ListController.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define([], function() {\n"
                    + "\n"
                    + "  " + nomeEntidade + "ListController.$inject = ['$scope', '" + nomeEntidade + "Service', 'gumgaController'];\n"
                    + "\n"
                    + "  function " + nomeEntidade + "ListController($scope, " + nomeEntidade + "Service, gumgaController) {\n"
                    + "\n"
                    + "    gumgaController.createRestMethods($scope, " + nomeEntidade + "Service, '" + nomeEntidade.toLowerCase() + "');\n"
                    + "    " + nomeEntidade + "Service.resetDefaultState();\n\n"
                    + "    $scope." + nomeEntidade.toLowerCase() + ".execute('get');\n"
                    + "\n"
                    + "    $scope.tableConfig = {\n"
                    + "      columns: '" + Util.primeiroAtributo(classeEntidade).getName() + ",button',\n"
                    + "      checkbox: true,\n"
                    + "      columnsConfig: [{\n"
                    + "        name: '" + Util.primeiroAtributo(classeEntidade).getName() + "',\n"
                    + "        title: '<span gumga-translate-tag=\"" + nomeEntidade.toLowerCase() + "." + Util.primeiroAtributo(classeEntidade).getName() + "\">" + Util.primeiroAtributo(classeEntidade).getName() + "</span>',\n"
                    + "        content: '{{$value." + Util.primeiroAtributo(classeEntidade).getName() + "}}',\n"
                    + "        sortField: '" + Util.primeiroAtributo(classeEntidade).getName() + "'\n"
                    + "      }, {\n"
                    + "        name: 'button',\n"
                    + "        title: ' ',\n"
                    + "        content: '<span class=\"pull-right\"><a class=\"btn btn-primary btn-sm\" ui-sref=\"" + nomeEntidade.toLowerCase() + ".edit({id: {{$value.id}} })\"><i class=\"glyphicon glyphicon-pencil\"></i></a></span>'\n"
                    + "      }]\n"
                    + "    };\n"
                    + "\n"
                    + "  };\n"
                    + "  return " + nomeEntidade + "ListController;\n"
                    + "});"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            File arquivoFormController = new File(pastaControllers + "/" + nomeEntidade + "FormController.js");
            FileWriter fw = new FileWriter(arquivoFormController);

            Set<Class> dependenciasManyToX = new HashSet<>();
            dependenciasManyToX.addAll(dependenciasManyToMany);
            dependenciasManyToX.addAll(dependenciasManyToOne);

            fw.write(""
                    + "define([], function() {\n"
                    + "\n"
                    + "\n"
                    + "  " + nomeEntidade + "FormController.$inject = ['" + nomeEntidade + "Service', '$state', 'entity', '$scope', 'gumgaController'" + Util.dependenciasSeparadasPorVirgula(dependenciasManyToX, "Service", true) + "];\n"
                    + "\n"
                    + "  function " + nomeEntidade + "FormController(" + nomeEntidade + "Service, $state, entity, $scope, gumgaController" + Util.dependenciasSeparadasPorVirgula(dependenciasManyToOne, "Service", false) + Util.dependenciasSeparadasPorVirgula(dependenciasManyToX, "Service", false) + ") {\n"
                    + "\n"
                    + "    gumgaController.createRestMethods($scope, " + nomeEntidade + "Service, '" + nomeEntidade.toLowerCase() + "');\n\n");

            for (Class clazz : dependenciasManyToX) {
                fw.write("    gumgaController.createRestMethods($scope, " + clazz.getSimpleName() + "Service, '" + clazz.getSimpleName().toLowerCase() + "');\n"
                        + "    $scope." + clazz.getSimpleName().toLowerCase() + ".methods.search('" + Util.primeiroAtributo(clazz).getName() + "','');\n"
                        + "\n");
            }

            /*
             <gumga-many-to-many 
             left-list="capacidadesOptions" 
             right-list="funcionario.data.capacidades" 
             left-search="servico.methods.search('nome',param)" 
             filter-parameters="nome"
             post-method="servico.methods.post(value)"
             authorize-add="true">
             <left-field>{{$value.nome}}</left-field>
             <right-field>{{$value.nome}}</right-field>
             </gumga-many-to-many>      
             */
            for (Field atributo : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if (atributo.isAnnotationPresent(ManyToMany.class)) {
                    fw.write("    $scope." + atributo.getName() + "Options=[];\n");
                }
            }

            fw.write(""
                    + "\n"
                    + "    $scope." + nomeEntidade.toLowerCase() + ".data = entity.data || {};\n"
                    + "    $scope.continue = {};\n"
                    + "\n"
                    + "    $scope." + nomeEntidade.toLowerCase() + ".on('putSuccess',function(data){\n"
                    + "      $state.go('" + nomeEntidade.toLowerCase() + ".list');\n"
                    + "    })\n"
                    + "  }\n"
                    + "\n"
                    + "  return " + nomeEntidade + "FormController;\n"
                    + "});"
                    + "");

            fw.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void geraServices() {
        try {
            File arquivoModule = new File(pastaServices + "/module.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define(function(require) {\n"
                    + "   require('angular')\n"
                    + "   .module('app." + nomeEntidade.toLowerCase() + ".services', [])\n"
                    + "   .service('" + nomeEntidade + "Service', require('app/modules/" + nomeEntidade.toLowerCase() + "/services/" + nomeEntidade + "Service'));\n"
                    + "});"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {

            File arquivoService = new File(pastaServices + "/" + nomeEntidade + "Service.js");
            FileWriter fw = new FileWriter(arquivoService);

            fw.write(""
                    + "define(['app/apiLocations'], function(APILocation) {\n"
                    + "\n"
                    + "  " + nomeEntidade + "Service.$inject = ['GumgaRest'];\n"
                    + "\n"
                    + "  function " + nomeEntidade + "Service(GumgaRest) {\n"
                    + "    var Service = new GumgaRest(APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "');\n"
                    + "\n"
                    + "    return Service;\n"
                    + "  }\n"
                    + "\n"
                    + "  return " + nomeEntidade + "Service;\n"
                    + "});"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void geraViews() {
        try {
            File arquivoModule = new File(pastaViews + "/base.html");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write("<gumga-nav multi-entity=\"true\" put-url=\"http://www.gumga.com.br/security-api/publicoperations/token/\" title=\"" + nomeEntidade + "\" state=\"login.log\"></gumga-nav>\n"
                    + "<gumga-menu menu-url=\"gumga-menu.json\" keys-url=\"keys.json\"  image=\"resources/images/gumga.png\"></gumga-menu>\n"
                    + "<div class=\"gumga-container\">\n"
                    + "    <h3 style=\"margin-top: 0\" gumga-translate-tag=\"" + nomeEntidade.toLowerCase() + ".title\"></h3>\n"
                    + "    <div class=\"col-md-12\" ui-view></div>\n"
                    + "</div>\n"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            File arquivoModule = new File(pastaViews + "/form.html");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "<form name=\"" + nomeEntidade + "Form\" gumga-form novalidate>\n"
                    + "\n");

            geraCampos(fw, this.classeEntidade);
            fw.write(""
                    + "\n"
                    + "    <gumga-errors label=\"Lista de erros\" title=\"Lista de erros\"></gumga-errors>\n"
                    + "    <gumga-form-buttons\n"
                    + "            back=\"" + nomeEntidade.toLowerCase() + ".list\"\n"
                    + "            submit=\"" + nomeEntidade.toLowerCase() + ".methods.put(" + nomeEntidade.toLowerCase() + ".data)\"\n"
                    + "            position=\"right\"\n"
                    + "            valid=\"" + nomeEntidade + "Form.$valid\"\n"
                    + "            confirm-dirty=\"true\"\n"
                    + "            continue=\"continue\">\n"
                    + "    </gumga-form-buttons>\n"
                    + "</form>"
            );
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            File arquivoModule = new File(pastaViews + "/list.html");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "<div class=\"col-md-5\" style=\"padding-left: 0\">\n"
                    + "  <a ui-sref=\"" + nomeEntidade.toLowerCase() + ".insert\" class=\"btn btn-primary\">\n"
                    + "    <i class=\"glyphicon glyphicon-plus\"></i>\n"
                    + "    Novo\n"
                    + "  </a>\n"
                    + "  <button type=\"button\" class=\"btn btn-danger\" ng-click=\"" + nomeEntidade.toLowerCase() + ".methods.delete(selectedValues)\">\n"
                    + "    <i class=\"glyphicon glyphicon-trash\"></i>\n"
                    + "    Remover\n"
                    + "  </button>\n"
                    + "</div>\n"
                    + "<div class=\"col-md-7\" style=\"padding-right: 0\">\n"
                    + "  <gumga-search fields=\"" + Util.todosAtributosSeparadosPorVirgula(classeEntidade) + "\"\n"
                    + "                advanced=\"true\"\n"
                    + "                get-queries=\"" + nomeEntidade.toLowerCase() + ".methods.getQuery(page)\"\n"
                    + "                search-method=\"" + nomeEntidade.toLowerCase() + ".methods.search(field,param)\"\n"
                    + "                advanced-method=\"" + nomeEntidade.toLowerCase() + ".methods.advancedSearch(param)\"\n"
                    + "                translate-entity=\"" + nomeEntidade.toLowerCase() + "\">\n");

            for (Field atributo : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                fw.write("    <advanced-field name=\"" + atributo.getName().toLowerCase() + "\" type=\"" + converteTipoParaAdvanced(atributo.getType()) + "\"></advanced-field>\n");
            }

            fw.write(""
                    + "  </gumga-search>\n"
                    + "</div>\n"
                    + "\n"
                    + "<div class=\"full-width-without-padding\">\n"
                    + "    <gumga-list sort=\"" + nomeEntidade.toLowerCase() + ".methods.sort(field, dir)\"\n"
                    + "            class=\"table-striped table-condensed\"\n"
                    + "            data=\"" + nomeEntidade.toLowerCase() + ".data\"\n"
                    + "            configuration=\"tableConfig\">\n"
                    + "    </gumga-list>\n"
                    + "</div>\n"
                    + "\n"
                    + "<pagination ng-model=\"page\"\n"
                    + "max-size=\"10\"\n"
                    + "boundary-links=\"true\"\n"
                    + "previous-text=\"‹\"\n"
                    + "next-Text=\"›\"\n"
                    + "first-text=\"«\"\n"
                    + "last-text=\"»\"\n"
                    + "items-per-page=\"" + nomeEntidade.toLowerCase() + ".pageSize\"\n"
                    + "total-items=\"" + nomeEntidade.toLowerCase() + ".count\"\n"
                    + "ng-change=\"" + nomeEntidade.toLowerCase() + ".methods.get(page)\n"
                    + "\">\n"
                    + "</pagination>"
            );
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (Class classe : dependenciasOneToMany) {
            try {
                File arquivoModalHtml = new File(pastaViews + "/modal" + classe.getSimpleName() + ".html");
                FileWriter fw = new FileWriter(arquivoModalHtml);
                fw.write("<form name=\"Modal\">\n"
                        + "<div class=\"modal-header\">\n"
                        + "    <h3 class=\"modal-title\" gumga-translate-tag=\" " + classe.getSimpleName().toLowerCase() + ".title\"></h3>\n"
                        + "</div>\n"
                        + "<div class=\"modal-body\" style=\"overflow: auto\">\n");

                geraCampos(fw, classe);

                fw.write("</div>\n"
                        + "<div class=\"clearfix\"></div>\n"
                        + "<div class=\"modal-footer\">\n"
                        + "    <button type=\"button\" class=\"btn btn-primary\" ng-click=\"ok(entity)\" ng-disabled=\"Modal.$invalid\">OK</button>\n"
                        + "    <button type=\"button\" class=\"btn btn-warning\" ng-click=\"cancel()\">Cancel</button>\n"
                        + "</div>\n"
                        + "</form>\n");

                fw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    public void geraCampos(FileWriter fw, Class classeEntidade) throws IOException {
        boolean primeiro;
        for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
            //COLOCAR OS TIPOS

            if (atributo.isAnnotationPresent(ManyToOne.class) || atributo.isAnnotationPresent(OneToOne.class)) {
                fw.write(""
                        + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                        + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                        + Util.IDENTACAO12 + "<gumga-many-to-one\n"
                        + Util.IDENTACAO16 + "input-name=\"" + atributo.getName() + "\"\n"
                        + Util.IDENTACAO16 + "value=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\"\n"
                        + Util.IDENTACAO16 + "search-method=\"" + atributo.getType().getSimpleName().toLowerCase() + ".methods.asyncSearch('" + Util.primeiroAtributo(atributo.getType()).getName() + "',param)\"\n"
                        + Util.IDENTACAO16 + "field=\"" + Util.primeiroAtributo(atributo.getType()).getName() + "\"\n"
                        + Util.IDENTACAO16 + "authorize-add=\"true\"\n"
                        + Util.IDENTACAO16 + "add-method=\"" + atributo.getType().getSimpleName().toLowerCase() + ".methods.asyncPost(value,'" + Util.primeiroAtributo(atributo.getType()).getName() + "')\">\n"
                        + Util.IDENTACAO12 + "</gumga-many-to-one>\n"
                        + Util.IDENTACAO08 + "</div>\n");

            } else if (atributo.isAnnotationPresent(ManyToMany.class
            )) {
                fw.write(
                        ""
                        + "        <div class=\"col-md-6\">\n"
                        + "            <label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".title\"></label>\n"
                        + "        </div>\n"
                        + "        <div class=\"col-md-6\">\n"
                        + "            <label for=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\" gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n"
                        + "        </div>"
                        + "\n");

                fw.write(Util.IDENTACAO08
                        + "<div class=\"full-width-without-padding\">\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO08 + "<gumga-many-to-many \n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "left-list=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".data\" \n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "right-list=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\" \n"
                        //+ Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "left-search=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".methods.search('" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "',param)\" \n"

                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "left-search=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".methods.advancedSearch('obj." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + " like\\''+param+'%\\'')\"\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "filter-parameters=\"" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "\"\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "post-method=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ".methods.save(value)\"\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "authorize-add=\"true\">\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "    <left-field>{{$value." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</left-field>\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO04 + Util.IDENTACAO08 + "    <right-field>{{$value." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</right-field>\n"
                        + Util.IDENTACAO04 + Util.IDENTACAO08 + "</gumga-many-to-many>\n\n"
                        + Util.IDENTACAO08 + "</div>\n"
                        + "");

                /*
                 <gumga-many-to-many 
                 left-list="capacidadesOptions" 
                 right-list="funcionario.data.capacidades" 
                 left-search="servico.methods.search('nome',param)" 
                 filter-parameters="nome"
                 post-method="servico.methods.post(value)"
                 authorize-add="true">
                 <left-field>{{$value.nome}}</left-field>
                 <right-field>{{$value.nome}}</right-field>
                 </gumga-many-to-many>      
                 */
            } else if (atributo.isAnnotationPresent(OneToMany.class
            )) {
                fw.write(
                        "<div class=\"full-width-without-padding\">");
                fw.write(Util.IDENTACAO04
                        + Util.IDENTACAO04 + "<label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n");
                fw.write(
                        "<gumga-one-to-many\n"
                        + "     children=\"entity." + atributo.getName() + "\"\n"
                        + "     template-url=\"app/modules/" + atributo.getDeclaringClass().getSimpleName().toLowerCase() + "/views/modal" + Util.getTipoGenerico(atributo).getSimpleName() + ".html\"\n"
                        + "     displayable-property=\"" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName().toLowerCase() + "\"\n"
                        + "     controller=\"Modal" + Util.getTipoGenerico(atributo).getSimpleName() + "Controller\">"
                        + "</gumga-one-to-many>\n"
                        + "</div>"
                        + "\n");

            } else {
                if ("gumgaCustomFields".equals(atributo.getName())) {
                    fw.write(""
                            + Util.IDENTACAO08 + "\n<gumga-custom-fields fields=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data\"></gumga-custom-fields>\n\n"
                    );
                } else if (GumgaAddress.class.equals(atributo.getType())) {
                    fw.write(""
                            + Util.IDENTACAO08 + "<div class=\"row\">\n"
                            + Util.IDENTACAO12 + "<div class=\"col-md-12\">\n"
                            + Util.IDENTACAO12 + Util.IDENTACAO04 + "<gumga-address name=\"" + atributo.getName() + "\" value=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\"> </gumga-address>\n"
                            + Util.IDENTACAO12 + "</div>\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaBarCode.class.equals(atributo.getType())) {
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error  type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaCEP.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-mask=\"99999-999\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\"/>\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaCNPJ.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "        <input id=\"" + atributo.getName() + "\" gumga-mask=\"99.999.999/9999-99\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\"  />\n"
                            + Util.IDENTACAO08 + "        </div>"
                            + "\n");
                } else if (GumgaCPF.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-mask=\"999.999.999-99\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaIP4.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaIP6.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaFile.class.equals(atributo.getType())) {//TODO SUBSTITUIR PELA DIRETIVA DE IMPORTAÇÃO E ARQUIVO QUANDO ESTIVER PRONTA

                } else if (GumgaImage.class.equals(atributo.getType())) {
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<gumga-upload attribute=\"" + nomeEntidade.toLowerCase() + ".data." + atributo.getName() + "\"\n"
                            + Util.IDENTACAO16 + "upload-method=\"" + nomeEntidade.toLowerCase() + ".methods.postImage(image)\"\n"
                            + Util.IDENTACAO16 + "delete-method=\"" + nomeEntidade.toLowerCase() + ".methods.deleteImage(image)\">\n"
                            + Util.IDENTACAO12 + "</gumga-upload>\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");

                } else if (GumgaEMail.class.equals(atributo.getType())) {
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error type=\"email\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaMoney.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<input gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" ui-money-mask=\"2\"/>\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaPhoneNumber.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<input id=\"" + atributo.getName() + "\" gumga-error type=\"text\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" ui-br-phone-number/>\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaURL.class.equals(atributo.getType())) {
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<input gumga-error type=\"url\" name=\"" + atributo.getName() + "\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (GumgaTime.class.equals(atributo.getType())) {
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<timepicker ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\"  show-meridian=\"false\"></timepicker>\n"
                            + Util.IDENTACAO08 + "</div>"
                            + "");
                } else if (GumgaMultiLineString.class.equals(atributo.getType())) {
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<textarea ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" " + geraValidacoesDoBenValidator(atributo) + " class=\"form-control\" placeholder=\"Digite " + Util.etiqueta(atributo) + ".\" rows=\"4\" cols=\"50\"></textarea>\n\n"
                            + Util.IDENTACAO08 + "</div>"
                            + "\n");
                } else if (GumgaGeoLocation.class.equals(atributo.getType())) { //TODO SUBSTITUIR PELO COMPONENTE GUMGAMAPS QUANDO ELE ESTIVER PRONTO
                    fw.write(""
                            + Util.IDENTACAO04 + "<div class=\"row\">\n"
                            + Util.IDENTACAO08 + "<div class=\"col-md-12\">\n"
                            + Util.IDENTACAO12 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO12 + "<div gumga-form-class=\"" + atributo.getName() + "latitude\">\n"
                            + Util.IDENTACAO16 + "<label gumga-translate-tag=\"gumga.latitude\">Latitude</label>\n"
                            + Util.IDENTACAO16 + "<input gumga-error type=\"text\" name=\"" + atributo.getName() + "latitude\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".latitude\" class=\"form-control\" />\n"
                            + Util.IDENTACAO12 + "</div>\n"
                            + Util.IDENTACAO12 + "<div gumga-form-class=\"" + atributo.getName() + "longitude\">\n"
                            + Util.IDENTACAO16 + "<label gumga-translate-tag=\"gumga.longitude\">Longitude</label>\n"
                            + Util.IDENTACAO16 + "<input gumga-error type=\"text\" name=\"" + atributo.getName() + "longitude\" " + geraValidacoesDoBenValidator(atributo) + " ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".longitude\" class=\"form-control\" />\n"
                            + Util.IDENTACAO12 + "</div>\n"
                            + Util.IDENTACAO12 + "<a class=\"btn btn-default\" ng-href=\"http://maps.google.com/maps?q={{" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".latitude + ',' + " + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".longitude}}\" target=\"_blank\"> <p class=\"glyphicon glyphicon-globe\"></p> GOOGLE MAPS</a>\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + Util.IDENTACAO04 + "</div>\n"
                            + "\n");
                } else if (GumgaBoolean.class.equals(atributo.getType())) {
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<input style=\"width:15px\" gumga-error type=\"checkbox\" " + geraValidacoesDoBenValidator(atributo) + " name=\"" + atributo.getName() + "\" ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");
                } else if (Date.class.equals(atributo.getType())) {
                    String varOpened = "opened" + Util.primeiraMaiuscula(atributo.getName());
                    fw.write(""
                            + Util.IDENTACAO08 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO12 + "<input type=\"text\" name=\"" + atributo.getName() + "\" class=\"form-control\" " + geraValidacoesDoBenValidator(atributo) + " datepicker-popup=\"fullDate\" ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\" is-open=\"" + varOpened + "\" ng-click=\"" + varOpened + "= !" + varOpened + "\" close-text=\"Close\" />\n"
                            + Util.IDENTACAO08 + "</div>\n"
                            + "\n");

                } else if (atributo.getType().isEnum()) {
                    Object[] constants = atributo.getType().getEnumConstants();
                    fw.write(Util.IDENTACAO08 + "<select class='form-control' gumga-error name=\"" + atributo.getName() + "\" ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\" >\n");
                    fw.write(Util.IDENTACAO12 + "<option  ng-selected=\"value.value === entity." + atributo.getName() + "\"  value=\"{{value.value}}\" ng-repeat=\"value in value" + atributo.getType().getSimpleName() + "\">{{value.label}}</option>");
                    fw.write(Util.IDENTACAO08 + "</select>\n");
                } else {
                    fw.write(""
                            + Util.IDENTACAO04 + "<div gumga-form-class=\"" + atributo.getName() + "\">\n"
                            + Util.IDENTACAO08 + "<label gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\">" + atributo.getName() + "</label>\n"
                            + Util.IDENTACAO08 + "<input gumga-error type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"" + classeEntidade.getSimpleName().toLowerCase() + ".data." + atributo.getName() + "\"" + geraValidacoesDoBenValidator(atributo) + "  class=\"form-control\" />\n"
                            + Util.IDENTACAO04 + "</div>\n\n"
                    );

                }

            }
            primeiro = false;
        }
    }

    private String geraValidacoesDoBenValidator(Field atributo) {
        String aRetornar = "";

        /*
         ConstraintComposition 	Boolean operator that is applied to all constraints of a composing constraint annotation.
         CreditCardNumber 	The annotated element has to represent a valid credit card number.
         CreditCardNumber.List 	Defines several @CreditCardNumber annotations on the same element.
         Email 	The string has to be a well-formed email address.
         Email.List 	Defines several @Email annotations on the same element.
         Length 	Validate that the string is between min and max included.
         Length.List 	Defines several @Length annotations on the same element.
         NotBlank 	Validate that the annotated string is not null or empty.
         NotBlank.List 	Defines several @NotBlank annotations on the same element.
         NotEmpty 	Asserts that the annotated string, collection, map or array is not null or empty.
         NotEmpty.List 	Defines several @NotEmpty annotations on the same element.
         Range 	The annotated element has to be in the appropriate range.
         Range.List 	Defines several @Range annotations on the same element.
         SafeHtml 	Validate a rich text value provided by the user to ensure that it contains no malicious code, such as embedded <script> elements.
         SafeHtml.List 	Defines several @WebSafe annotations on the same element.
         ScriptAssert 	A class-level constraint, that evaluates a script expression against the annotated element.
         ScriptAssert.List 	Defines several @ScriptAssert annotations on the same element.
         URL 	Validate that the string is a valid URL.
         URL.List 	Defines several @URL annotations on the same element.
         */
        if (atributo.isAnnotationPresent(NotNull.class
        )
                || atributo.isAnnotationPresent(NotEmpty.class
                )
                || atributo.isAnnotationPresent(NotBlank.class
                )) {
            aRetornar += " gumga-required gumga-min-length=\"1\"";
        }

        return aRetornar;
    }

    private void geraModule() {
        try {
            File arquivoModule = new File(pastaApp + "/module.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define(function(require) {\n"
                    + "\n"
                    + "  var APILocation = require('app/apiLocations');\n"
                    + "  require('angular-ui-router');\n"
                    + "  require('app/modules/" + nomeEntidade.toLowerCase() + "/services/module');\n"
                    + "  require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/module');\n"
                    + "\n"
                    + "  return require('angular')\n"
                    + "    .module('app." + nomeEntidade.toLowerCase() + "', [\n"
                    + "      'ui.router',\n"
                    + "      'app." + nomeEntidade.toLowerCase() + ".controllers',\n"
                    + "      'app." + nomeEntidade.toLowerCase() + ".services',\n"
                    + "      'gumga.core'\n"
                    + "    ])\n"
                    + "    .config(function($stateProvider, $httpProvider) {\n"
                    + "      $stateProvider\n"
                    + "        .state('" + nomeEntidade.toLowerCase() + ".list', {\n"
                    + "          url: '/list',\n"
                    + "          templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/list.html',\n"
                    + "          controller: '" + nomeEntidade + "ListController'\n"
                    + "        })\n"
                    + "        .state('" + nomeEntidade.toLowerCase() + ".insert', {\n"
                    + "          url: '/insert',\n"
                    + "          templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/form.html',\n"
                    + "          controller: '" + nomeEntidade + "FormController',\n"
                    + "          resolve: {\n"
                    + "            entity: ['$stateParams', '$http', function($stateParams, $http) {\n"
                    + "              return $http.get(APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "/new');\n"
                    + "            }]\n"
                    + "          }\n"
                    + "        })\n"
                    + "        .state('" + nomeEntidade.toLowerCase() + ".edit', {\n"
                    + "          url: '/edit/:id',\n"
                    + "          templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/form.html',\n"
                    + "          controller: '" + nomeEntidade + "FormController',\n"
                    + "          resolve: {\n"
                    + "            entity: ['$stateParams', '$http', function($stateParams, $http) {\n"
                    + "              return $http.get(APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "/' + $stateParams.id);\n"
                    + "            }]\n"
                    + "          }\n"
                    + "        });\n"
                    + "    })\n"
                    + "\n"
                    + "});"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void geraI18n() {
        try {
            String texto = Util.IDENTACAO04 + ",\"" + nomeEntidade.toLowerCase() + "\":{\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + "\"title\":\"" + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"menulabel\": \"" + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"edit\": \"Editar " + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"insert\": \"Inserir " + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"list\": \"Consulta " + Util.primeiraMaiuscula(nomeEntidade) + "\"\n"
                    + Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"id\": \"id\"\n";
            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                texto += Util.IDENTACAO04 + Util.IDENTACAO04 + ",\"" + atributo.getName().toLowerCase() + "\":\"" + Util.primeiraMaiuscula(atributo.getName()) + "\"\n";
            }
            texto += Util.IDENTACAO04 + "}\n";
            Util.adicionaLinha(pastaI18n + "/pt-br.json", ",\"address\":", texto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String converteTipoParaAdvanced(Class<?> type) {  //TODO OUTROS TIPOS JAVA
        if (type.equals(String.class
        )) {

            return "string";
        }

        if (type.equals(BigDecimal.class
        )) {

            return "number";
        }

        if (type.equals(Double.class
        )) {

            return "number";
        }

        if (type.equals(Integer.class
        )) {

            return "number";
        }

        if (type.equals(Long.class
        )) {

            return "number";
        }

        if (type.equals(Byte.class
        )) {

            return "number";
        }

        if (type.equals(Boolean.class
        )) {

            return "boolean";
        }

        if (type.equals(GumgaBoolean.class
        )) {

            return "boolean";
        }

        if (type.equals(Date.class
        )) {

            return "date";
        }

        if (type.equals(GumgaMoney.class
        )) {

            return "money";
        }
        return "string";
    }

}

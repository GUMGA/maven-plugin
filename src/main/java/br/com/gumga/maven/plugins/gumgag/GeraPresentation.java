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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
    private String nomeEntidade;

    private Class classeEntidade;

    private Set<Class> dependenciasManyToOne;
    private Set<Class> dependenciasOneToMany;
    private Set<Class> dependenciasManyToMany;

    private String pastaApp;
    private String pastaControllers;
    private String pastaServices;
    private String pastaViews;
    private String pastaI18n;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Util.geraGumga(getLog());

        try {
            nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);

            pastaApp = Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/modules/" + (nomeEntidade.toLowerCase());

            getLog().info("Iniciando plugin Gerador de Html e JavaScript de Apresentação oi ");
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

            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                if (atributo.isAnnotationPresent(ManyToOne.class)) {
                    dependenciasManyToOne.add(atributo.getType());
                }
                if (atributo.isAnnotationPresent(ManyToMany.class)) {
                    dependenciasManyToMany.add(Util.getTipoGenerico(atributo));
                }
                if (atributo.isAnnotationPresent(OneToMany.class)) {
                    dependenciasOneToMany.add(Util.getTipoGenerico(atributo));
                }
            }
            geraControllers();
            geraServices();
            geraViews();
            geraModule();
            geraI18n();
            adicionaAoMenu();
        } catch (Exception ex) {
            getLog().error(ex);
        }

    }

    private void adicionaAoMenu() throws IOException {

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/gumga-menu.json", "{", "    {\n"
                + "        \"label\": \"" + nomeEntidade + "\",\n"
                + "        \"URL\": \"" + nomeEntidade.toLowerCase() + ".list\",\n"
                + "        \"key\": \"CRUD-" + nomeEntidade + "\",\n"
                + "        \"filhos\": []\n"
                + "    },");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMROUTE", ""
                + Util.IDENTACAO + Util.IDENTACAO + ".state('" + nomeEntidade.toLowerCase() + "', {\n"
                + Util.IDENTACAO + Util.IDENTACAO + "data: {\n"
                + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "id: 1\n"
                + Util.IDENTACAO + Util.IDENTACAO + "}, \n"
                + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "url: '/" + nomeEntidade.toLowerCase() + "',\n"
                + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/base.html'\n"
                + Util.IDENTACAO + Util.IDENTACAO + "})\n"
                + "");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMREQUIRE", Util.IDENTACAO + "require('app/modules/" + nomeEntidade.toLowerCase() + "/module');");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/app/app.js", "//FIMINJECTIONS", Util.IDENTACAO + Util.IDENTACAO + ",'app." + nomeEntidade.toLowerCase() + "'");

        Util.adicionaLinha(Util.windowsSafe(project.getFile().getParent()) + "/src/main/webapp/keys.json", "]", ",\"CRUD-" + nomeEntidade + "\"");

    }

    private void geraControllers() {
        try {
            File arquivoModule = new File(pastaControllers + "/module.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define(function (require) {\n"
                    + "    var angular = require('angular');\n"
                    + "     require('gumga-core')/\n"
                    + "    require('app/modules/" + nomeEntidade.toLowerCase() + "/services/module');\n"
                    + "    require('angular-ui-router');\n"
                    + "\n"
                    + "    return angular.module('app." + nomeEntidade.toLowerCase() + ".controllers', ['app." + nomeEntidade.toLowerCase() + ".services','ui.router','gumga.core'])\n"
                    + "        .controller('" + nomeEntidade + "FormController', require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/" + nomeEntidade + "FormController'))\n"
                    + "        .controller('" + nomeEntidade + "ListController', require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/" + nomeEntidade + "ListController'))\n");

            for (Class classe : dependenciasOneToMany) {
                fw.write("        .controller('Modal" + classe.getSimpleName() + "Controller', require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/Modal" + classe.getSimpleName() + "Controller'))\n");
            }

            fw.write(""
                    + "});\n"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            File arquivoModule = new File(pastaControllers + "/" + nomeEntidade + "ListController.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define([], function () {\n"
                    + "\n"
                    + "    " + nomeEntidade + "ListController.$inject = ["
                    + "'$scope',"
                    + " '" + nomeEntidade + "Service' "
                    + "];\n"
                    + "    function " + nomeEntidade + "ListController("
                    + "$scope, "
                    + nomeEntidade + "Service "
                    + ") {\n"
                    + "\n"
                    + "        " + nomeEntidade + "Service.resetDefaultState();\n"
                    + "        $scope.content = {};\n"
                    + "        $scope.page = 1;\n"
                    + "\n"
                    + "        function update(values) {\n"
                    + "            $scope.content = values;\n"
                    + "        }\n"
                    + "\n"
                    + "        $scope.get = function () {\n"
                    + "            " + nomeEntidade + "Service.get($scope.page).success(function (values) {\n"
                    + "                $scope.content = values;\n"
                    + "            });\n"
                    + "        };\n"
                    + "\n"
                    + "        $scope.sort = function (field, way) {\n"
                    + "            $scope.page = 1;\n"
                    + "            " + nomeEntidade + "Service.doSort(field, way)\n"
                    + "                .success(update);\n"
                    + "        };\n"
                    + "\n"
                    + "        $scope.del = function (entities) {\n"
                    + "            $scope.page = 1;\n"
                    + "            " + nomeEntidade + "Service.doRemove(entities)\n"
                    + "                .then(function(data){\n"
                    + "                     " + nomeEntidade + "Service.getAfterSearch()"
                    + "                      .success(update);\n"
                    + "                });\n"
                    + "\n"
                    + "        };\n"
                    + "\n"
                    + "        $scope.search = function (field, param) {\n"
                    + "            $scope.page = 1;\n"
                    + "            " + nomeEntidade + "Service.getSearch(field, param)\n"
                    + "                .success(update);\n"
                    + "        };\n"
                    + "\n"
                    + "        $scope.advancedSearch = function (param) {\n"
                    + "            $scope.page = 1;\n"
                    + "            " + nomeEntidade + "Service.advancedSearch(param)\n"
                    + "              .success(update);\n"
                    + "        };\n"
                    + "        $scope.get();\n"
                    + "    }\n"
                    + "\n"
                    + "    return " + nomeEntidade + "ListController;\n"
                    + "\n"
                    + "});"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            File arquivoModule = new File(pastaControllers + "/" + nomeEntidade + "FormController.js");
            FileWriter fw = new FileWriter(arquivoModule);

            Set<Class> dependencias = new HashSet<>();
            dependencias.addAll(dependenciasManyToMany);
            dependencias.addAll(dependenciasManyToOne);

            fw.write(""
                    + "define([], function () {\n"
                    + "\n"
                    + "    " + nomeEntidade + "FormController.$inject = ['" + nomeEntidade + "Service', '$state','entity','$scope'");

            for (Class classe : dependencias) {
                fw.write(",'" + classe.getSimpleName() + "Service'");
            }

            fw.write(""
                    + "];\n"
                    + "\n"
                    + "    function " + nomeEntidade + "FormController(" + nomeEntidade + "Service, $state,entity,$scope");

            for (Class classe : dependencias) {
                fw.write("," + classe.getSimpleName() + "Service");
            }

            fw.write(""
                    + ") {\n");
            for (Class classe : dependencias) {
                fw.write("        " + classe.getSimpleName() + "Service.resetDefaultState();\n");
            }

            fw.write("        " + nomeEntidade + "Service.resetDefaultState();\n");  //TODO REMOVER DUPLICIDADE EM AUTORELACIONAMENTOS QUANDO EXISTEREM

            fw.write("        $scope.entity = angular.copy(entity.data);\n"
                    + "       $scope.continue = {};"
                    + "\n"
                    + "        $scope.update = function (entity) {\n"
                    + "            " + nomeEntidade + "Service.update(entity)\n"
                    + "                .success(function () {\n"
                    + "                     if(!$scope.continue.value){\n"
                    + "                         $state.go('" + nomeEntidade.toLowerCase() + ".list');\n"
                    + "                     }else{\n"
                    + "                         $scope.entity = angular.copy(entity.data);\n"
                    + "                     }"
                    + "                });\n"
                    + "        };\n"
                    + "\n");
            for (Field atributo : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
                if (atributo.isAnnotationPresent(ManyToMany.class)) {
                    fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");

                    fw.write(""
                            + "        $scope." + atributo.getName() + "Availables = [];\n"
                            + "        " + Util.getTipoGenerico(atributo).getSimpleName() + "Service.get().success(function (data) {\n"
                            + "            $scope." + atributo.getName() + "Availables = data.values;\n"
                            + "        });\n"
                            + "\n");

                    fw.write(""
                            + "        $scope." + atributo.getName() + "Search = function(param){\n"
                            + "            " + Util.getTipoGenerico(atributo).getSimpleName() + "Service.getSearch('" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "', param).then(function(data){\n"
                            + "                $scope." + atributo.getName() + "Availables = data.data.values;\n"
                            + "            })\n"
                            + "        }\n"
                            + "");

                }

                if (atributo.isAnnotationPresent(OneToMany.class)) {
                    fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");
                }
                if (atributo.isAnnotationPresent(ManyToOne.class)) {
                    fw.write(""
                            + Util.IDENTACAO + Util.IDENTACAO + "$scope." + atributo.getName() + "List = [];\n\n"
                            + Util.IDENTACAO + Util.IDENTACAO + "" + atributo.getType().getSimpleName() + "Service.get().success(function(data){\n"
                            + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "$scope." + atributo.getName() + "List = data.values;\n"
                            + Util.IDENTACAO + Util.IDENTACAO + "});\n\n"
                            + Util.IDENTACAO + Util.IDENTACAO + "$scope.searchManyToOne" + Util.primeiraMaiuscula(atributo.getName()) + " = function (param) {\n"
                            + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "return " + atributo.getType().getSimpleName() + "Service.getSearch('" + Util.primeiroAtributo(atributo.getType()).getName() + "', param).then(function(data){\n"
                            + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "$scope." + atributo.getName() + "List = data.data.values;\n"
                            + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "})\n"
                            + Util.IDENTACAO + Util.IDENTACAO + "};\n"
                            + "\n");
                }

            }

            fw.write("    }\n"
                    + "\n"
                    + "    return " + nomeEntidade + "FormController;\n"
                    + "});\n"
                    + ""
            );
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (Class classe : dependenciasOneToMany) {
            try {
//                A zica tá aqui nas dependências Munif, a gente não sabe controlar certinho essas dependências,
//                vê como é o jeito certo, o resto tá explicando no papel.
                Set<Class> dependencias = new HashSet<>();
                for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classe)) {
                    if (atributo.isAnnotationPresent(ManyToOne.class)) {
                        dependencias.add(atributo.getType());
                    }
                    if (atributo.isAnnotationPresent(ManyToMany.class)) {
                        dependencias.add(Util.getTipoGenerico(atributo));
                    }
                    if (atributo.isAnnotationPresent(OneToMany.class)) {
                        dependencias.add(Util.getTipoGenerico(atributo));
                    }
                }

                File f = new File(pastaControllers + "/Modal" + classe.getSimpleName() + "Controller.js");
                FileWriter fw = new FileWriter(f);

                fw.write(""
                        + "define([], function () {\n"
                        + "\n"
                        + "    " + classe.getSimpleName() + "ModalController.$inject = ['" + nomeEntidade + "Service', '$state','$modalInstance','entity','$scope'");

                for (Class classe_ : dependencias) {
                    fw.write(",'" + classe_.getSimpleName() + "Service'");
                }

                fw.write(""
                        + "];\n"
                        + "\n"
                        + "    function " + classe.getSimpleName() + "ModalController(" + nomeEntidade + "Service, $state,$modalInstance,entity,$scope");

                for (Class classe_ : dependencias) {
                    fw.write("," + classe_.getSimpleName() + "Service");
                }

                fw.write(""
                        + ") {\n");

                fw.write(
                        "		entity = entity || {};\n"
                        + "             $scope.entity = angular.copy(entity)\n"
                        + "");
                for (Field atributo : Util.getTodosAtributosNaoEstaticos(classe)) {
                    if (atributo.isAnnotationPresent(ManyToMany.class)) {
                        fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");

                        fw.write(""
                                + "        $scope." + atributo.getName() + "Availables = [];\n"
                                + "        " + Util.getTipoGenerico(atributo).getSimpleName() + "Service.get().success(function (data) {\n"
                                + "            $scope." + atributo.getName() + "Availables = data.values;\n"
                                + "        });\n"
                                + "\n");

                        fw.write(""
                                + "        $scope." + atributo.getName() + "Search = function(param){\n"
                                + "            " + Util.getTipoGenerico(atributo).getSimpleName() + "Service.getSearch('" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "', param).then(function(data){\n"
                                + "                $scope." + atributo.getName() + "Availables = data.data.values;\n"
                                + "            })\n"
                                + "        }\n"
                                + "");

                    }

                    if (atributo.isAnnotationPresent(OneToMany.class)) {
                        fw.write("        $scope.entity." + atributo.getName() + " = $scope.entity." + atributo.getName() + "  || [];\n\n");
                    }
                    if (atributo.isAnnotationPresent(ManyToOne.class)) {
                        fw.write(""
                                + Util.IDENTACAO + Util.IDENTACAO + "$scope." + atributo.getName() + "List = [];\n\n"
                                + Util.IDENTACAO + Util.IDENTACAO + "" + atributo.getType().getSimpleName() + "Service.get().success(function(data){\n"
                                + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "$scope." + atributo.getName() + "List = data.values;\n"
                                + Util.IDENTACAO + Util.IDENTACAO + "});\n\n"
                                + Util.IDENTACAO + Util.IDENTACAO + "$scope.searchManyToOne" + Util.primeiraMaiuscula(atributo.getName()) + " = function (param) {\n"
                                + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "return " + atributo.getType().getSimpleName() + "Service.getSearch('" + Util.primeiroAtributo(atributo.getType()).getName() + "', param).then(function(data){\n"
                                + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "$scope." + atributo.getName() + "List = data.data.values;\n"
                                + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "})\n"
                                + Util.IDENTACAO + Util.IDENTACAO + "};\n"
                                + "\n");
                    }

                }

                fw.write("		$scope.ok = function (obj) {\n"
                        + "			$modalInstance.close(obj);\n"
                        + "		};\n"
                        + "\n"
                        + "		$scope.cancel = function () {\n"
                        + "			$modalInstance.dismiss('cancel');\n"
                        + "		};\n"
                        + "\n"
                        + "	}\n"
                        + "	return " + classe.getSimpleName() + "ModalController;\n"
                        + "})\n"
                        + "\n");

                fw.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    private void geraServices() {
        try {
            File arquivoModule = new File(pastaServices + "/" + nomeEntidade + "Service.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define(['api-locations'], function (APILocation) {\n"
                    + "\n"
                    + "    " + nomeEntidade + "Service.$inject = ['GumgaBase', '$stateParams'];\n"
                    + "\n"
                    + "    function " + nomeEntidade + "Service(GumgaBase, $stateParams) {\n"
                    + "        var url = APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "';\n"
                    + "        var query = {};\n"
                    + "        query.params = {\n"
                    + "            start: 0,\n"
                    + "            pageSize: 10\n"
                    + "        };\n"
                    + "\n"
                    + "        this.get = function (page) {\n"
                    + "            if (page) {\n"
                    + "                query.params.start = (page - 1) * query.params.pageSize;\n"
                    + "                if (page < 1) throw 'Invalid page';\n"
                    + "            }\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.getById = function(id) {\n"
                    + "            return GumgaBase.getById(id);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.getNew = function(){\n"
                    + "            return GumgaBase.getNew(url);\n"
                    + "        }\n"
                    + "\n"
                    + "        this.getAfterSearch = function(){\n"
                    + "            this.resetDefaultState();\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        }\n"
                    + "\n"
                    + "        this.getSearch = function (field, param) {\n"
                    + "            if (!param) param = '';\n"
                    + "            query.params = {};\n"
                    + "            query.params.q = param;\n"
                    + "            query.params.start = 0;\n"
                    + "            query.params.pageSize = 10;\n"
                    + "            query.params.searchFields = field;\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.doSort = function (field, way) {\n"
                    + "            query.params.start = 0;\n"
                    + "            query.params.sortField = field;\n"
                    + "            query.params.sortDir = way;\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.doRemove = function (entities) {\n"
                    + "            return GumgaBase.deleteAll(url,entities);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.update = function (entity) {\n"
                    + "            if (entity.id) {\n"
                    + "                return GumgaBase.update(url,entity);\n"
                    + "            }\n"
                    + "            return GumgaBase.save(url,entity);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.advancedSearch = function (param) {\n"
                    + "            query.params = {};\n"
                    + "            query.params.aq = param;\n"
                    + "            return GumgaBase.get(url,query);\n"
                    + "        };\n"
                    + "\n"
                    + "        this.resetDefaultState = function(){\n"
                    + "            query.params = {\n"
                    + "                start: 0,\n"
                    + "                pageSize: 10\n"
                    + "            };\n"
                    + "        };\n"
                    + "    }\n"
                    + "\n"
                    + "    return " + nomeEntidade + "Service;\n"
                    + "});\n"
                    + ""
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            File arquivoModule = new File(pastaServices + "/module.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "/**\n"
                    + " * Created by igorsantana on " + Util.hoje() + ".\n"
                    + " */\n"
                    + "\n"
                    + "define(function(require){\n"
                    + "    require('gumga-core');\n"
                    + "    var angular = require('angular');"
                    + "    var " + nomeEntidade + "Service = require('app/modules/" + nomeEntidade.toLowerCase() + "/services/" + nomeEntidade + "Service');\n"
                    + "    return angular.module('app." + nomeEntidade.toLowerCase() + ".services',['gumga.core'])\n"
                    + "        .service('" + nomeEntidade + "Service'," + nomeEntidade + "Service);\n"
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
            fw.write("<gumga-nav title=\"" + nomeEntidade + "\"></gumga-nav>\n"
                    + "<gumga-menu menu-url=\"gumga-menu.json\" keys-url=\"keys.json\"  image=\"resources/images/gumga.png\"></gumga-menu>\n"
                    + "<div class=\"gumga-container\" gumga-alert>\n"
                    + "    <gumga-breadcrumb></gumga-breadcrumb>\n"
                    + "    <div class=\"col-md-12\">\n"
                    + "        <h3 style=\"margin-top: 0\" gumga-translate-tag=\"" + nomeEntidade.toLowerCase() + ".title\"></h3>\n"
                    + "    </div>\n"
                    + "    <div class=\"col-md-12\" ui-view style=\"margin-right:0;margin-left:0\">\n"
                    + "\n"
                    + "    </div>\n"
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
                    + "<form name=\"forms\" novalidate>\n"
                    + "    <div class=\"col-md-12\">\n"
                    + "\n");

            boolean primeiro = true;
            geraCampos(fw, this.classeEntidade);
            fw.write(""
                    + "\n"
                    + "        <gumga-form-buttons\n"
                    + "                back=\"" + nomeEntidade.toLowerCase() + ".list\"\n"
                    + "                submit=\"update(entity)\"\n"
                    + "                position=\"right\"\n"
                    + "                valid=\"forms.$valid\"\n"
                    + "                continue=\"continue\">\n"
                    + "        </gumga-form-buttons>\n"
                    + "    </div>\n"
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
                    + "<div class=\"col-md-12\">\n"
                    + "    <div class=\"col-md-5\">\n"
                    + "        <a ui-sref=\"" + nomeEntidade.toLowerCase() + ".insert\" class=\"btn btn-primary\"><i class=\"fa fa-plus\"></i> New</a>\n"
                    + "        <button type=\"button\" class=\"btn btn-danger\" ng-click=\"del(selectedEntities)\"><i class=\"fa fa-trash-o\"></i> Delete\n"
                    + "        </button>\n"
                    + "    </div>\n"
                    + "    <div class=\"col-md-7\">\n"
                    + "        <gumga-search field=\"" + Util.primeiroAtributo(classeEntidade).getName() + "\" advanced=\"true\" search-method=\"search(field,param)\"\n"
                    + "                      advanced-method=\"advancedSearch(param)\">\n"
                    + "            <advanced-field name=\"id\" type=\"number\"></advanced-field>\n");

            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                fw.write("            <advanced-field name=\"" + atributo.getName() + "\" type=\"" + converteTipoParaAdvanced(atributo.getType()) + "\"></advanced-field>\n");
            }

            fw.write(""
                    + "        </gumga-search>\n"
                    + "    </div>\n"
                    + "    <div class=\"col-md-12\" style=\"margin-top:1%\">\n"
                    + "        <gumga-table\n"
                    + "            translate-entity=\"" + nomeEntidade.toLowerCase() + "\""
                    + "            name=\"" + nomeEntidade.toLowerCase() + "\""
                    + "            values=\"content.values\"\n");

            fw.write("            columns=\"" + Util.todosAtributosSeparadosPorVirgula(classeEntidade) + "\"\n");

            fw.write(""
                    + "            sort-function=\"sort(field,way)\"\n"
                    + "            >\n"
                    + "        <buttons-column>\n"
                    + "            <a ui-sref=\"" + nomeEntidade.toLowerCase() + ".edit({id: entity.id})\" class=\"btn btn-link pull-right\">Edit</a>\n"
                    + "        </buttons-column>\n"
                    + "        </gumga-table>\n"
                    + "    </div>\n"
                    + "\n"
                    + "    <div class=\"col-md-12\">\n"
                    + "        <pagination ng-model=\"page\"\n"
                    + "                    items-per-page=\"content.pageSize\"\n"
                    + "                    total-items=\"content.count\"\n"
                    + "                    ng-change=\"get()\"></pagination>\n"
                    + "    </div>\n"
                    + "\n"
                    + "</div>\n"
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (Class classe : dependenciasOneToMany) {
            try {
                File arquivoModalHtml = new File(pastaViews + "/modal" + classe.getSimpleName() + ".html");
                FileWriter fw = new FileWriter(arquivoModalHtml);
                fw.write("<form>\n"
                        + "<div class=\"modal-header\">\n"
                        + "    <h3 class=\"modal-title\" gumga-translate-tag=\" " + classe.getSimpleName().toLowerCase() + ".title\"></h3>\n"
                        + "</div>\n"
                        + "<div class=\"modal-body\" style=\"overflow: auto\">\n");

                geraCampos(fw, classe);

                fw.write("</div>\n"
                        + "<div class=\"clearfix\"></div>\n"
                        + "<div class=\"modal-footer\">\n"
                        + "    <button type=\"button\" class=\"btn btn-primary\" ng-click=\"ok(entity)\">OK</button>\n"
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
            boolean requerido = false;

            fw.write(Util.IDENTACAO + Util.IDENTACAO + "<!--" + atributo.getName() + " " + atributo.getType() + "-->\n");

            if (atributo.isAnnotationPresent(ManyToOne.class) || atributo.isAnnotationPresent(OneToOne.class)) {
                fw.write(Util.IDENTACAO + Util.IDENTACAO + "<label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n");
                fw.write(Util.IDENTACAO + Util.IDENTACAO
                        + "<gumga-many-to-one model=\"entity." + atributo.getName() + "\"\n"
                        + "         search-method=\"searchManyToOne" + Util.primeiraMaiuscula(atributo.getName()) + "(param)\"\n"
                        + "         list=\"" + atributo.getName() + "List\"\n"
                        + "         field=\"" + Util.primeiroAtributo(atributo.getType()).getName() + "\">\n"
                        + "</gumga-many-to-one>"
                        + "");

            } else if (atributo.isAnnotationPresent(ManyToMany.class)) {
                fw.write(""
                        + "        <div class=\"col-md-6\">\n"
                        + "            <label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase()+ ".title\"></label>\n"
                        + "        </div>\n"
                        + "        <div class=\"col-md-6\">\n"
                        + "            <label for=\"cidade\" gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n"
                        + "        </div>"
                        + "\n");

                fw.write("<div class=\"full-width-without-padding\">\n"
                        + Util.IDENTACAO + Util.IDENTACAO + "<gumga-many-to-many left-list=\"" + atributo.getName() + "Availables" + "\" right-list=\"entity." + atributo.getName() + "\" "
                        + "left-search=\"" + atributo.getName() + "Search(param)\""
                        + ">\n"
                        + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "    <left-list-field>{{item." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</left-list-field>\n"
                        + Util.IDENTACAO + Util.IDENTACAO + Util.IDENTACAO + "    <right-list-field>{{item." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}</right-list-field>\n"
                        + Util.IDENTACAO + Util.IDENTACAO + "</gumga-many-to-many>\n\n"
                        + "</div>\n"
                        + "");

            } else if (atributo.isAnnotationPresent(OneToMany.class)) {
                fw.write(Util.IDENTACAO + Util.IDENTACAO + "<label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + classeEntidade.getSimpleName().toLowerCase() + "." + atributo.getName() + "\"></label>\n");
                fw.write("<div class=\"col-md-12\">\n"
                        + "<gumga-one-to-many\n"
                        + "     children=\"entity." + atributo.getName().toLowerCase() + "\"\n"
                        + "     template-url=\"app/modules/" + nomeEntidade.toLowerCase() + "/views/modal" + Util.getTipoGenerico(atributo).getSimpleName() + ".html\"\n"
                        + "     displayable-property=\"" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName().toLowerCase() + "\"\n"
                        + "     controller=\"Modal" + Util.getTipoGenerico(atributo).getSimpleName() + "Controller\">"
                        + "</gumga-one-to-many>\n"
                        + "</div>\n"
                        + "\n");

            } else {
                fw.write(Util.IDENTACAO + Util.IDENTACAO + "<label for=\"" + atributo.getName() + "\"  gumga-translate-tag=\"" + nomeEntidade.toLowerCase() + "." + atributo.getName() + "\"></label>\n");
                if (GumgaAddress.class.equals(atributo.getType())) {
                    fw.write("  <div class=\"row\">"
                            + "     <div class=\"col-md-12\">\n"
                            + "         <gumga-address name=\"" + atributo.getName() + "\" value=\"entity." + atributo.getName() + "\"> </gumga-address>\n"
                            + "     </div>\n"
                            + " </div>\n"
                            + "\n");
                } else if (GumgaBarCode.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaCEP.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-br-cep-mask/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaCNPJ.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + ".\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-br-cnpj-mask/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaCPF.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-br-cpf-mask/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaIP4.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaIP6.class.equals(atributo.getType())) { //TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR O GUMGAMAX E GUMGAMIN
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" gumga-min=\"12\" gumga-max=\"12\"/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaFile.class.equals(atributo.getType())) {//TODO SUBSTITUIR PELA DIRETIVA DE IMPORTAÇÃO E ARQUIVO QUANDO ESTIVER PRONTA

                } else if (GumgaImage.class.equals(atributo.getType())) {//TODO SUBSTITUIR PELA DIRETIVA DE IMPORTAÇÃO E ARQUIVO QUANDO ESTIVER PRONTA

                } else if (GumgaEMail.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"email\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaMoney.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-money-mask=\"2\"/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaPhoneNumber.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" ui-br-phone-number/>\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaURL.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"url\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaTime.class.equals(atributo.getType())) {//TODO INCLUIR A MASCARA PARA O INPUT QUANDO O COMPONENTE ESTIVER PRONTO E RETIRAR A DEPENDENCIA EXTERNA
                    fw.write("\n"
                            + " <div class=\"row\">\n"
                            + "     <div class=\"col-md-12\">\n"
                            + "            <label for=\"" + atributo.getName() + ".hour\">Hour</label>\n"
                            + "            <input id=\"" + atributo.getName() + ".hour\" type=\"number\" name=\"" + atributo.getName() + ".hour\" ng-model=\"entity." + atributo.getName() + ".hour\" max=\"23\" min=\"0\" class=\"form-control\"/>\n"
                            + "            <gumga-errors name=\"" + atributo.getName() + ".hour\"></gumga-errors>\n"
                            + "            <label for=\"" + atributo.getName() + ".minute\">Minute</label>\n"
                            + "            <input id=\"" + atributo.getName() + ".minute\" type=\"number\" name=\"" + atributo.getName() + ".minute\" ng-model=\"entity." + atributo.getName() + ".minute\" max=\"59\" min=\"0\" class=\"form-control\"/>\n"
                            + "            <gumga-errors name=\"" + atributo.getName() + ".minute\"></gumga-errors>\n"
                            + "            <label for=\"" + atributo.getName() + ".second\">Second</label>\n"
                            + "            <input id=\"" + atributo.getName() + ".second\" type=\"number\" name=\"" + atributo.getName() + ".second\" ng-model=\"entity." + atributo.getName() + ".second\" max=\"59\" min=\"0\" class=\"form-control\"/>\n"
                            + "            <gumga-errors name=\"" + atributo.getName() + ".second\"></gumga-errors>\n"
                            + "    </div>\n"
                            + " </div>\n"
                            + "\n");
                } else if (GumgaMultiLineString.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <textarea ng-model=\"entity." + atributo.getName() + ".value\" class=\"form-control\" placeholder=\"Digite " + Util.etiqueta(atributo) + ".\" rows=\"4\" cols=\"50\"></textarea>\n\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else if (GumgaGeoLocation.class.equals(atributo.getType())) { //TODO SUBSTITUIR PELO COMPONENTE GUMGAMAPS QUANDO ELE ESTIVER PRONTO
                    fw.write(""
                            + " <div class=\"row\">\n"
                            + "     <div class=\"col-md-12\">\n"
                            + "        <label for=\"" + atributo.getName() + ".latitude\">Latitude</label>\n"
                            + "        <input id=\"" + atributo.getName() + ".latitude\" type=\"text\" name=\"" + atributo.getName() + ".latitude\" ng-model=\"entity." + atributo.getName() + ".latitude\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + ".latitude\"></gumga-errors>\n"
                            + "        <label for=\"" + atributo.getName() + ".longitude\">Longitude</label>\n"
                            + "        <input id=\"" + atributo.getName() + ".longitude\" type=\"text\" name=\"" + atributo.getName() + ".longitude\" ng-model=\"entity." + atributo.getName() + ".longitude\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + ".longitude\"></gumga-errors>\n"
                            + "        <a class=\"btn btn-default\" ng-href=\"http://maps.google.com/maps?q={{entity." + atributo.getName() + ".latitude + ',' + entity." + atributo.getName() + ".longitude}}\" target=\"_blank\"> <p class=\"glyphicon glyphicon-globe\"></p> GOOGLE MAPS</a>\n"
                            + "     </div>\n"
                            + " </div>\n"
                            + "\n");
                } else if (GumgaBoolean.class.equals(atributo.getType())) {
                    fw.write(""
                            + "        <label><input type=\"checkbox\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + ".value\" /> <span gumga-translate-tag=\"" + nomeEntidade.toLowerCase() + "." + atributo.getName() + "\"></span></label>"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                } else {
                    fw.write(""
                            + "        <input id=\"" + atributo.getName() + "\" type=\"text\" name=\"" + atributo.getName() + "\" ng-model=\"entity." + atributo.getName() + "\" class=\"form-control\" />\n"
                            + "        <gumga-errors name=\"" + atributo.getName() + "\"></gumga-errors>\n"
                            + "\n");
                }
            }
            primeiro = false;
        }
    }

    private void geraModule() {
        try {
            File arquivoModule = new File(pastaApp + "/module.js");
            FileWriter fw = new FileWriter(arquivoModule);
            fw.write(""
                    + "define(function (require) {\n"
                    + "\n"
                    + "    var angular = require('angular');\n"
                    + "    require('angular-ui-router');\n"
                    + "    require('app/modules/" + nomeEntidade.toLowerCase() + "/services/module');\n"
                    + "    require('app/modules/" + nomeEntidade.toLowerCase() + "/controllers/module');\n"
                    + "    require('gumga-core');\n"
                    + "    var APILocation = require('api-locations');\n"
                    + "\n"
                    + "    angular.module('app." + nomeEntidade.toLowerCase() + "', ['ui.router', 'app." + nomeEntidade.toLowerCase() + ".controllers', 'app." + nomeEntidade.toLowerCase() + ".services', 'gumga.core'])\n"
                    + "        .config(function ($stateProvider, $httpProvider) {\n"
                    + "            $stateProvider\n"
                    + "                .state('" + nomeEntidade.toLowerCase() + ".list', {\n"
                    + "                    url: '/list',\n"
                    + "                    templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/list.html',\n"
                    + "                    controller: '" + nomeEntidade + "ListController',\n"
                    + "                    data: {\n"
                    + "                         id: 2 \n"
                    + "                    }\n"
                    + "                })\n"
                    + "                .state('" + nomeEntidade.toLowerCase() + ".insert', {\n"
                    + "                    url: '/insert',\n"
                    + "                    templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/form.html',\n"
                    + "                    controller: '" + nomeEntidade + "FormController',\n"
                    + "                    controllerAs: 'form',\n"
                    + "                    data: {"
                    + "                         id: 3 "
                    + "                    },"
                    + "                    resolve: {\n"
                    + "                        entity: ['$stateParams', '$http', function ($stateParams, $http) {\n"
                    + "                            var url = APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "/new'\n"
                    + "                            return $http.get(url);\n"
                    + "                        }]\n"
                    + "                    }\n"
                    + "                })\n"
                    + "                .state('" + nomeEntidade.toLowerCase() + ".edit', {\n"
                    + "                    url: '/edit/:id',\n"
                    + "                    templateUrl: 'app/modules/" + nomeEntidade.toLowerCase() + "/views/form.html',\n"
                    + "                    controller: '" + nomeEntidade + "FormController',\n"
                    + "                    data: {"
                    + "                         id: 3 "
                    + "                    },"
                    + "                    resolve: {\n"
                    + "                        entity: ['$stateParams', '$http', function ($stateParams, $http) {\n"
                    + "                            var url = APILocation.apiLocation + '/api/" + nomeEntidade.toLowerCase() + "/' + $stateParams.id;\n"
                    + "                            return $http.get(url);\n"
                    + "                        }]\n"
                    + "                    }\n"
                    + "                });\n"
                    + "        })\n"
                    + "});\n"
                    + ""
                    + "");
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void geraI18n() {
        try {
            String texto = Util.IDENTACAO + ",\"" + nomeEntidade.toLowerCase() + "\":{\n"
                    + Util.IDENTACAO + Util.IDENTACAO + "\"title\":\"" + nomeEntidade + "\"\n"
                    + Util.IDENTACAO + Util.IDENTACAO + ",\"menulabel\": \"" + nomeEntidade + "\"\n"
                    + Util.IDENTACAO + Util.IDENTACAO + ",\"edit\": \"Editar " + nomeEntidade + "\"\n"
                    + Util.IDENTACAO + Util.IDENTACAO + ",\"insert\": \"Inserir " + nomeEntidade + "\"\n"
                    + Util.IDENTACAO + Util.IDENTACAO + ",\"list\": \"Consulta " + nomeEntidade + "\"\n"
                    + Util.IDENTACAO + Util.IDENTACAO + ",\"id\": \"id\"\n";
            for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
                texto += Util.IDENTACAO + Util.IDENTACAO + ",\"" + atributo.getName().toLowerCase() + "\":\"" + atributo.getName() + "\"\n";
            }
            texto += Util.IDENTACAO + "}\n";
            Util.adicionaLinha(pastaI18n + "/pt-br.json", ",\"address\":", texto);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String converteTipoParaAdvanced(Class<?> type) {  //TODO OUTROS TIPOS JAVA
        if (type.equals(String.class)) {
            return "string";
        }
        if (type.equals(BigDecimal.class)) {
            return "number";
        }
        if (type.equals(Double.class)) {
            return "number";
        }
        if (type.equals(Integer.class)) {
            return "number";
        }
        if (type.equals(Long.class)) {
            return "number";
        }
        if (type.equals(Byte.class)) {
            return "number";
        }
        if (type.equals(Boolean.class)) {
            return "boolean";
        }
        if (type.equals(GumgaBoolean.class)) {
            return "boolean";
        }
        if (type.equals(Date.class)) {
            return "date";
        }
        if (type.equals(GumgaMoney.class)) {
            return "money";
        }
        return "string";
    }

}
/*
    
 private void geraHTMLs() {

 File f = new File(pastaHtml);
 f.mkdirs();

 try {
 File arquivoBase = new File(pastaHtml + "/base.html");
 FileWriter fwBase = new FileWriter(arquivoBase);
 fwBase.write(""
 + "\n"
 + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
 + "    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
 + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
 + "    <head>\n"
 + "\n"
 + "        <meta charset=\"UTF-8\"/>\n"
 + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
 + "        <meta name=\"viewport\" content=\"initial-scale=1, maximum-scale=1, user-scalable=no\" />\n"
 + "\n"
 + "        <title>" + nomeEntidade + "</title>\n"
 + "\n"
 + "        <link rel=\"stylesheet\" href=\"/" + project.getParent().getName() + "/static/styles/main.css\"/>\n"
 + "        <link rel=\"stylesheet\" href=\"/" + project.getParent().getName() + "/static/styles/gumga.css\" />\n"
 + "\n"
 + "    </head>\n"
 + "    <body class=\"gumga-offcanvas\">\n"
 + "\n"
 + "        <gumga-nav-bar></gumga-nav-bar>\n"
 + "\n"
 + "        <!--/.nav-collapse -->\n"
 + "\n"
 + "        <div class=\"gumga-offcanvas-sidebar\">\n"
 + "            <gumga-base-menu></gumga-base-menu>\n"
 + "        </div>\n"
 + "\n"
 + "        <div id=\"gumga-growl-container\" class='notifications top-right'></div>\n"
 + "\n"
 + "        <div class=\"gumga-offcanvas-content\">\n"
 + "            <div id=\"container\" class=\"gumga-content\">\n"
 + "                <h1 class=\"gumga-title\">" + nomeEntidade + "</h1>\n"
 + "                <div ui-view></div>\n"
 + "            </div>\n"
 + "        </div>\n"
 + "\n"
 + "        <script src=\"/" + project.getParent().getName() + "/static/scripts/vendor/require.js\"></script>\n"
 + "        <script src=\"/" + project.getParent().getName() + "/static/scripts/config.js\"></script>\n"
 + "        <script src=\"/" + project.getParent().getName() + "/static/scripts/app-config.js\"></script>\n"
 + "        <script>\n"
 + "            requirejs.config({baseUrl: '/" + project.getParent().getName() + "/static/scripts/'});\n"
 + "\n"
 + "            requirejs(['angular', 'app/" + nomeEntidade.toLowerCase() + "/module', 'gumga/components/menu', 'gumga/components/offcanvas', 'angular-locale_pt-br', 'gumga/directives'], function (angular, initModule) {\n"
 + "                var app = angular.module('app', [initModule.name, 'gumga.components.menu', 'gumga.components.offcanvas', 'ngLocale', 'basetemplate']);\n"
 + "                app.constant('contextPath', '/" + project.getParent().getName() + "');\n"
 + "                angular.bootstrap(document, [app.name]);\n"
 + "            });\n"
 + "\n"
 + "        </script>\n"
 + "    </body>\n"
 + "</html>"
 + "");

 fwBase.close();

 File arquivoForm = new File(pastaHtml + "/form.html");
 FileWriter fwForm = new FileWriter(arquivoForm);
 fwForm.write("\n\n"
 + "<div class=\"panel panel-default\">\n"
 + "    <div class=\"panel-body\">\n"
 + "        <form name=\"entityForm\" method=\"POST\" ng-submit=\"ctrl.save($event, entity)\" gumga-form-errors gumga-ng-model-errors>\n"
 + "\n");

 geraCampos(classeEntidade, fwForm, "");

 if (classeEntidade.isAnnotationPresent(Audited.class)) {
 fwForm.write(""
 + "             <div class=\"text-left\">\n"
 + "                <button ng-disabled=\"dataOlder\" type=\"button\" class=\"btn btn-info\" ng-click=\"showOlder = !showOlder\" ng-show=\"!checkboxEnable\" >Older Versions</button>\n"
 + "                <div ng-show=\"showOlder == true\">\n"
 + "                    <gumga:accordion close-others=\"false\">\n"
 + "                        <gumga:accordion:group is-open=\"isopen\" ng-repeat=\"v in older\" \n"
 + "                                               heading=\"Editado por: {{v.gumgaRevisionEntity.userLogin ? (v.gumgaRevisionEntity.userLogin) : ' sistema'}} em: {{v.gumgaRevisionEntity.moment | date :'fullDate' }} from ip: {{v.gumgaRevisionEntity.ip ? (v.gumgaRevisionEntity.ip) : ' 0.0.0.0'}}\">\n"
 + "                            {{v.object}}\n"
 + "                            \n"
 + "                        </gumga:accordion:group>\n"
 + "                    </gumga:accordion>\n"
 + "                </div>\n"
 + "            </div> \n"
 + ""
 + "\n\n");
 }

 fwForm.write(""
 + "            <div class=\"text-right\">\n"
 + "                <label ng-show=\"checkboxEnable\"><input type=\"checkbox\" name=\"continuarInserindo\" ng-model=\"entity.continuarInserindo\"/> Continuar Inserindo</label>\n"
 + "                <input type=\"submit\" value=\"Salvar\" class=\"btn btn-primary\" ng-disabled=\"ctrl.saving || entityForm.$invalid\" />\n"
 + "                <a href=\"#\" class=\"btn btn-default\">Cancelar</a>\n"
 + "            </div>\n"
 + "        </form>\n"
 + "    </div>\n"
 + "</div>"
 + "");
 fwForm.close();

 File arquivoList = new File(pastaHtml + "/list.html");
 FileWriter fwList = new FileWriter(arquivoList);
 Field primeiroAtributo = Util.getTodosAtributosMenosIdAutomatico(classeEntidade).get(0);
 String nomeAtributo = primeiroAtributo.getName();
 String etiqueta = Util.primeiraMaiuscula(nomeAtributo);

 fwList.write(""
 + "<div class=\"panel panel-default\">\n"
 + "    <div class=\"panel-body\">\n"
 + "        <div class=\"row\">\n"
 + "            <div id=\"buttons\" class=\"col-md-5\" style=\"margin-bottom: 10px;\">\n"
 + "                <a href=\"#/insert\" class=\"btn btn-primary\" id=\"btnNovo\">\n"
 + "                    <span class=\"glyphicon glyphicon-plus\"></span> \n"
 + "                    Novo\n"
 + "                </a>\n"
 + "                <button id=\"btnExcluir\" class=\"btn btn-danger\" ng-click=\"ctrl.removeSelection()\" ng-disabled=\"selection.length == 0\">\n"
 + "                    <span class=\"glyphicon glyphicon-trash\"></span> \n"
 + "                    Remover\n"
 + "                </button>\n"
 + "            </div>\n"
 + "\n");

 fwList.write(""
 + "            <div class=\"col-md-7 gumga-crud-search-simple\">\n"
 + "                <gumga:search on-search=\"ctrl.search($text, $fields)\" search-text=\"search.text\" select-fields=\"search.fields\" ng-disabled=\"search.showAdvanced\" >\n"
 + "");

 for (Field a : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
 fwList.write("                    <gumga:search:field field=\"" + a.getName() + "\" label=\"" + Util.primeiraMaiuscula(a.getName()) + "\" selected=\"true\"></gumga:search:field>\n");
 }

 fwList.write(""
 + "                </gumga:search>\n"
 + "                <button type=\"button\" btn-checkbox ng-model=\"search.showAdvanced\" class=\"btn btn-default btn-switch-filters\" tooltip=\"Pesquisa avan�ada\">\n"
 + "                    <span class=\"glyphicon glyphicon-filter\" aria-hidden=\"true\"></span>\n"
 + "                </button>\n"
 + "            </div>\n"
 + "            <div class=\"col-md-12\" ng-show=\"search.showAdvanced\">\n"
 + "                <div class=\"panel panel-default\">\n"
 + "                    <div class=\"panel-heading\">Pesquisa avançada</div>\n"
 + "                    <div class=\"panel-body\">\n"
 + "                        <gumga:filter ng-model=\"search.advanced\" size=\"sm\" is-open=\"search.isAdvancedOpen\">\n");
 for (Field a : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
 fwList.write(""
 + "                        <gumga:filter:item field=\"" + a.getName() + "\" label=\"" + Util.primeiraMaiuscula(a.getName()) + "\"></gumga:filter:item>\n");
 }
 fwList.write(""
 + "                        </gumga:filter>\n"
 + "                        <button class=\"btn btn-primary btn-search\" type=\"submit\" ng-click=\"ctrl.advancedSearch(search.advanced)\" ng-disabled=\"search.isAdvancedOpen\">\n"
 + "                            <span class=\"glyphicon glyphicon-search\"></span>\n"
 + "                        </button>\n"
 + "                    </div>\n"
 + "                </div>\n"
 + "            </div>\n"
 + "        </div>\n"
 + "\n"
 + "        <div class=\\\"col-xs-12\\\">\n"
 + "            <gumga:table\n"
 + "                on-sort=\"doSort($column, $direction)\""
 + "                values=\"list.values\"\n"
 + "                class=\"table-condensed table-striped\"\n"
 + "                selectable=\"multiple\"\n"
 + "                selection=\"selection\"\n"
 + "                sort-by=\"sort.field\"\n"
 + "                sort-direction=\"sort.direction\"\n"
 + "                empty-values-message=\"Sem resultados\">\n");

 for (Field a : Util.getTodosAtributosMenosIdAutomatico(classeEntidade)) {
 fwList.write(""
 + "                <gumga:column sort-field=\"" + a.getName() + "\" label=\"" + Util.primeiraMaiuscula(a.getName()) + "\">{{$value." + a.getName() + "}}</gumga:column>\n");
 }

 fwList.write(""
 + "                <gumga:column label=\"\">\n"
 + "                    <div class=\"text-right\">\n"
 + "                        <a href=\"#/edit/{{$value.id}}\" class=\"btn btn-primary\" title=\"Editar\">\n"
 + "                            <i class=\"glyphicon glyphicon-pencil\"></i>\n"
 + "                        </a>\n"
 + "                    </div>\n"
 + "                </gumga:column>\n"
 + "            </gumga:table>\n"
 + "\n"
 + "            <gumga:pagination ng-show=\"numberOfPages > 1\" items-per-page=\"list.pageSize\" total-items=\"list.count\" ng-model=\"page\" num-pages=\"numberOfPages\" boundary-links=\"true\"></gumga:pagination>\n"
 + "        </div>\n"
 + "\n"
 + "    </div>\n"
 + "</div>"
 + ""
 + "");
 fwList.close();

 } catch (Exception ex) {
 getLog().error(ex);
 }
 }

 public void geraCampos(Class classe, FileWriter fwForm, String controller) throws IOException {
 boolean primeiro = true;
 for (Field atributo : Util.getTodosAtributosMenosIdAutomatico(classe)) {
 boolean requerido = false; // VERIFICAR
 fwForm.write("\n\n<!--" + atributo.getType() + "-->\n");
 if (atributo.isAnnotationPresent(ManyToOne.class)) {
 geraEntradaToOne(fwForm, atributo, requerido, primeiro, controller);
 } else if (atributo.isAnnotationPresent(OneToOne.class)) {
 geraEntradaToOne(fwForm, atributo, requerido, primeiro, controller);
 } else if (atributo.isAnnotationPresent(OneToMany.class)) {
 geraEntradaOneToMany(fwForm, atributo, requerido, primeiro, controller);
 } else if (atributo.isAnnotationPresent(ManyToMany.class)) {
 geraEntradaManyToMany(fwForm, atributo, requerido, primeiro, controller);
 } else if (Boolean.class.equals(atributo.getType()) || Boolean.TYPE.equals(atributo.getType())) {
 geraEntradaBoolean(fwForm, atributo, requerido, primeiro, controller);
 } else if (BigDecimal.class.equals(atributo.getType())) {
 geraEntradaBigDecimal(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaAddress.class.equals(atributo.getType())) {
 geraEntradaGumgaAddress(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaBoolean.class.equals(atributo.getType())) {
 geraEntradaGumgaBoolean(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaCEP.class.equals(atributo.getType())) {
 geraEntradaGumgaCEP(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaCNPJ.class.equals(atributo.getType())) {
 geraEntradaGumgaCNPJ(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaCPF.class.equals(atributo.getType())) {
 geraEntradaGumgaCPF(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaEMail.class.equals(atributo.getType())) {
 geraEntradaGumgaEmail(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaFile.class.equals(atributo.getType())) {
 geraEntradaGumgaFile(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaGeoLocation.class.equals(atributo.getType())) {
 geraEntradaGumgaGeoLocation(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaIP4.class.equals(atributo.getType())) {
 geraEntradaGumgaIP4(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaIP6.class.equals(atributo.getType())) {
 geraEntradaGumgaIP6(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaImage.class.equals(atributo.getType())) {
 geraEntradaGumgaImage(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaMoney.class.equals(atributo.getType())) {
 geraEntradaGumgaMoney(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaMultiLineString.class.equals(atributo.getType())) {
 geraEntradaGumgaMultiLine(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaPhoneNumber.class.equals(atributo.getType())) {
 geraEntradaPhoneNumber(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaTime.class.equals(atributo.getType())) {
 geraEntradaGumgaTime(fwForm, atributo, requerido, primeiro, controller);
 } else if (GumgaURL.class.equals(atributo.getType())) {
 geraEntradaURL(fwForm, atributo, requerido, primeiro, controller);
 } else if (Date.class.equals(atributo.getType())) {
 geraEntradaDate(fwForm, atributo, requerido, primeiro, controller);
 } else {
 geraEntradaGenerica(fwForm, atributo, requerido, primeiro, controller);
 }
 primeiro = false;
 }
 }

 public void geraEntradaOneToMany(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 String nomeArquivoModal = geraModalOneToMany(atributo);

 fwForm.write(""Novo
 + ""
 + "        <gumga:children list=\"entity." + atributo.getName() + "\" modal-template-url=\"" + atributo.getName() + "-modal.html\"  modal-controller=\"" + Util.primeiraMaiuscula(atributo.getName()) + "ModalController as ctrl\">\n"
 + "            <label class=\"control-label\">" + Util.primeiraMaiuscula(atributo.getName()) + " </label>\n"
 + "            <button type=\"button\" class=\"btn btn-primary navbar-btn\" ng-click=\"gumgaChildren.openForm({servico:{}})\">\n"
 + "                <span class=\"glyphicon glyphicon-plus\"></span>\n"
 + "                Novo\n"
 + "            </button>\n"
 + "            <div class=\"list-group\">\n"
 + "                <a ng-repeat=\"item in gumgaChildren.list\" class=\"list-group-item\" ng-click=\"gumgaChildren.openForm(item)\">\n"
 + "                    {{item." + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "}}\n"
 + "                    <button ng-click=\"gumgaChildren.remove(item)\" class=\"btn btn-danger btn-xs pull-right\">\n"
 + "                        <span class=\"glyphicon glyphicon-remove\"></span>\n"
 + "                        Remover\n"
 + "                    </button>\n"
 + "                </a>\n"
 + "            </div>\n"
 + "        </gumga:children>\n"
 + "\n"
 + "    <script type=\"text/ng-template\" id=\"" + atributo.getName() + "-modal.html\">\n"
 + "        <%@ include file=\"" + nomeArquivoModal + "\" %>\n"
 + "    </script>"
 + ""
 );
 }

 public void geraEntradaManyToMany(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 ManyToMany mm = atributo.getAnnotation(ManyToMany.class);
 if (mm.mappedBy().isEmpty()) {
 fwForm.write(""
 + ""
 + "<gumga-many-to-many-" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + " entity-list=\"entity." + atributo.getName() + "\" label=\"" + Util.etiqueta(atributo) + "\"></gumga-many-to-many-" + Util.getTipoGenerico(atributo).getSimpleName().toLowerCase() + ">"
 + ""
 + "\n");
 }
 }

 public void geraEntradaBoolean(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "    <div class=\"form-group\" gumga-form-group=\"" + Util.etiqueta(atributo) + "\">\n"
 + "        <label><input type=\"checkbox\" name=\"" + atributo.getName() + "\" ng-model=\"" + controller + "entity." + atributo.getName() + "\" /> " + Util.etiqueta(atributo) + "</label>\n"
 + "        <gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "    </div>"
 + ""
 );
 }

 public void geraEntradaBigDecimal(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + "\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " gumga-number decimal-places=\"2\" />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaGumgaAddress(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + ""
 + " <%-- CEP --%>\n"
 + "    <gumga:accordion close-others=\"true\" >\n"
 + "        <gumga:accordion:group heading=\"" + Util.etiqueta(atributo) + "\" is-open=\"true\">\n"
 + "            <div class=\"form-group\" gumga-form-group=\"cep\"> \n"
 + "                <input name=\"cep\" size=\"9\" ng-model=\"" + controller + "entity." + atributo.getName() + ".cep\"  gumga-mask=\"99999-999\" required=\"true\"/>\n"
 + "                <button class=\"btn btn-xs btn-primary\" ng-click=\"ctrl." + atributo.getName() + "UpdateAddress()\">Procurar Endereço <span class=\"glyphicon glyphicon-search\"></span></button><br><br>\n"
 + "\n"
 + "                <select  ng-options=\"ps for ps in pais\" ng-model=\"" + controller + "entity." + atributo.getName() + ".pais\" required=\"true\"></select>\n"
 + "\n"
 + "                <input name=\"descricao\" class=\"form-group-sm\" ng-model=\"" + controller + "entity." + atributo.getName() + ".localidade\" required=\"false\" placeholder=\"Localidade\" />\n"
 + "                <select  ng-options=\"uf for uf in allUF track by uf\" ng-model=\"" + controller + "entity." + atributo.getName() + ".uf\" required=\"true\" >\n"
 + "                </select>\n"
 + "            </div>\n"
 + "\n"
 + "            <%-- TipoLogradouro/Logradouro/Número//Complemento//Bairro --%>\n"
 + "\n"
 + "            <div class=\"form-group\" gumga-form-group=\" numero\">\n"
 + "                <select required=\"true\" ng-options=\"log for log in allLogradouro\" ng-model=\"" + controller + "entity." + atributo.getName() + ".tipoLogradouro\"></select>\n"
 + "                <input name=\"descricao\" size=\"25\" ng-model=\"" + controller + "entity." + atributo.getName() + ".logradouro\"  placeholder=\"Nome do Logradouro\" required=\"false\" />\n"
 + "                <input type=\"text\" size=\"6\" ng-model=\"" + controller + "entity." + atributo.getName() + ".numero\" placeholder=\"Número\" autofocus=\"\" required=\"true\"> <br><br>\n"
 + "                <input name=\"descricao\" size=\"25\" ng-model=\"" + controller + "entity." + atributo.getName() + ".complemento\" placeholder=\"Complemento\"/>\n"
 + "                <input name=\"descricao\" ng-model=\"" + controller + "entity." + atributo.getName() + ".bairro\" required=\"false\" placeholder=\"Bairro\" />\n"
 + "            </div>\n"
 + "            <a ng-href=\"https://www.google.com.br/maps/place/{{" + controller + "entity." + atributo.getName() + ".tipoLogradouro + ',' + " + controller + "entity." + atributo.getName() + ".logradouro + ',' + " + controller + "entity." + atributo.getName() + ".numero + ',' + " + controller + "entity." + atributo.getName() + ".localidade}}\" target=\"_blank\" class=\"btn btn-primary btn-primary\">GOOGLE MAPS <span class=\"glyphicon glyphicon-globe\"></span></a>\n"
 + "        </gumga:accordion:group>\n"
 + "    </gumga:accordion>\n");
 }

 public void geraEntradaGumgaBoolean(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "    <div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "        <label><input type=\"checkbox\" name=\"" + atributo.getName() + "\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" /> " + Util.etiqueta(atributo) + "</label>\n"
 + "        <gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "    </div>\n");
 }

 public void geraEntradaGumgaCEP(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" required=\"" + requerido + "\" gumga-mask=\"99999-999\" " + (primeiro ? "autofocus" : "") + " />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaGumgaCNPJ(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" required=\"" + requerido + "\" gumga-mask=\"99.999.999/9999-99\" " + (primeiro ? "autofocus" : "") + " />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaGumgaCPF(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" required=\"" + requerido + "\"  gumga-mask=\"999.999.999-99\" " + (primeiro ? "autofocus" : "") + " />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaGumgaEmail(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input type=\"email\"  name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaGumgaGeoLocation(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "    <div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + " 	       <label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "            <input type=\"text\" name=\"descricao\" ng-model=\"" + controller + "entity." + atributo.getName() + ".latitude\" required=\"true\" min=\"-90\" max=\"90\" " + (primeiro ? "autofocus" : "") + "  gumga-number decimal-places=\"8\" />      \n"
 + "            <input type=\"text\" name=\"descricao\" ng-model=\"" + controller + "entity." + atributo.getName() + ".longitude\" required=\"true\" min=\"-180\" max=\"180\" gumga-number decimal-places=\"8\" />     \n"
 + "            <a ng-href=\"http://maps.google.com/maps?q={{entity." + atributo.getName() + ".latitude + ',' + entity." + atributo.getName() + ".longitude}}\" target=\"_blank\"> <p class=\"glyphicon glyphicon-globe\"></p> GOOGLE MAPS</a>\n"
 + "    </div>"
 + ""
 + ""
 + "");
 }

 public void geraEntradaGumgaIP4(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" required=\"" + requerido + "\" gumga-mask=\"999.999.999.999\" " + (primeiro ? "autofocus" : "") + " />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaGumgaIP6(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaGumgaImage(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + ""
 + "<form name=\"myForm\">\n"
 + "  	<fieldset>\n"
 + "	    " + atributo.getName() + ": <input ng-file-select=\"\" ng-model=\"" + controller + "picFile\" name=\"file\" accept=\"image/*\" ng-file-change=\"generateThumb(picFile[0], $files)\" required=\"\" type=\"file\">\n"
 + "	<button ng-disabled=\"!myForm.$valid\" ng-click=\"uploadPic(picFile)\">Submit</button>\n"
 + "  	</fieldset>\n"
 + "</form>"
 + "");
 }

 public void geraEntradaGumgaMoney(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + "  gumga-number decimal-places=\"2\"  />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "             <p class=\"help-block\">Valor: {{entity.money.value| currency }}</p>\n"
 + "	</div>\n");
 }

 public void geraEntradaGumgaMultiLine(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "             <label class=\"control-label\">" + Util.etiqueta(atributo) + "</label><br>\n"
 + "             <textarea ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" class=\"form-control\" placeholder=\"Digite " + Util.etiqueta(atributo) + ".\" rows=\"4\" cols=\"50\" ng-model=\"entity.multiLine.value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " ></textarea>\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaPhoneNumber(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaGumgaTime(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "    <div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "        <label class=\"control-label\">" + atributo.getName() + ":</label><br>\n"
 + "        <input type=\"number\" size=\"20\" ng-model=\"" + controller + "entity." + atributo.getName() + ".hour\" max=\"23\" min=\"0\" required=\"true\"/>\n"
 + "        <input type=\"number\" size=\"20\" ng-model=\"" + controller + "entity." + atributo.getName() + ".minute\" max=\"59\" min=\"0\" required=\"true\"/>\n"
 + "        <input type=\"number\" size=\"20\" ng-model=\"" + controller + "entity." + atributo.getName() + ".second\" max=\"59\" min=\"0\" required=\"true\"/>\n"
 + "        <p class=\"help-block\">" + atributo.getName() + ": {{ entity." + atributo.getName() + ".hour + ':' + entity." + atributo.getName() + ".minute + ':' + entity." + atributo.getName() + ".second }}</p>\n"
 + "    </div>"
 + ""
 + ""
 + "");
 }

 public void geraEntradaURL(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + ".value\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaGenerica(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "		<label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "		<input name=\"descricao\" class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + "\" required=\"" + requerido + "\"" + (primeiro ? "autofocus" : "") + " />\n"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 public void geraEntradaToOne(FileWriter fileWriter, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 String nomePrimeiroAtributo = Util.primeiroAtributo(atributo.getType()).getName();

 fileWriter.write("\n"
 + "    <div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "        <label class=\"control-label\">" + Util.etiqueta(atributo) + "</label>\n"
 + "\n"
 + "        <gumga:select ng-model=\"" + controller + "entity." + atributo.getName() + "\">\n"
 + "            <gumga:select:match placeholder=\"Selecione um " + Util.etiqueta(atributo) + "...\">{{$select.selected." + nomePrimeiroAtributo + "}}</gumga:select:match>\n"
 + "            <gumga:select:choices repeat=\"" + atributo.getName().toLowerCase() + " in lista" + atributo.getType().getSimpleName() + " track by $index\" refresh=\"ctrl.refreshLista" + atributo.getType().getSimpleName() + "($select.search)\" refresh-delay=\"0\">\n"
 + "                {{" + atributo.getName().toLowerCase() + "." + nomePrimeiroAtributo + "}}\n"
 + "            </gumga:select:choices>\n"
 + "        </gumga:select>\n"
 + "\n"
 + "\n"
 + "        <gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "    </div>\n"
 + "");
 }

 public void geraListJs() throws IOException {
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
 }

 public void geraFormJs() throws SecurityException, IOException {
 File arquivoForm = new File(pastaControllers + "/form.js");
 FileWriter fwForm = new FileWriter(arquivoForm);
 List<Field> atributosAddress = new ArrayList<>();
 for (Field at : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
 if (at.getType().equals(GumgaAddress.class)) {
 atributosAddress.add(at);
 }
 }

 fwForm.write(""
 + "define(function(require) {\n"
 + "\n"
 + "	return require('angular-class').create({\n\n");

 String injetar = "";
 for (Class tipo : dependenciasManyToOne) {
 injetar += ("'" + tipo.getSimpleName() + "Service',");
 }
 //        for (Class tipo : dependenciasManyToMany) {
 //            injetar += ("'" + tipo.getSimpleName() + "Service',");
 //        }
 for (Class tipo : dependenciasOneToMany) {
 injetar += ("'" + tipo.getSimpleName() + "Service',");
 }

 if (!injetar.isEmpty()) {
 fwForm.write("$inject: [" + injetar + "],");

 }

 fwForm.write("\n"
 + "		extends : require('app-commons/controllers/basic-form-controller'),\n"
 + "		prototype : {\n"
 + "\n"
 + "			initialize : function() {\n"
 + "				// Inicialização do controller\n");

 for (Class tipo : dependenciasManyToOne) {
 fwForm.write("this.$scope.lista" + tipo.getSimpleName() + " = [];\n");
 }

 //        for (Class tipo : dependenciasManyToMany) {
 //            fwForm.write(""
 //                    + "                this.$scope." + tipo.getSimpleName() + " = [];\n"
 //                    + "                this.refresh" + tipo.getSimpleName() + "();\n"
 //                    + "\n");
 //        }
 for (Field atributo : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
 if (atributo.isAnnotationPresent(OneToMany.class)) {
 fwForm.write(""
 + "                this.$scope.entity." + atributo.getName() + " = this.$scope.entity." + atributo.getName() + " || [];\n"
 + "");
 }
 }

 if (!atributosAddress.isEmpty()) {
 fwForm.write(""
 + "                this.$scope.entity = this.entity;\n"
 + "                this.$scope.allUF = ['AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS', 'MT', 'PA', 'PB', 'PE', 'PI', 'PR',\n"
 + "                    'RJ', 'RN', 'RR', 'RS', 'SC', 'SE', 'SP', 'TO'];\n"
 + "\n"
 + "                this.$scope.allLogradouro = ['Outros', 'Aeroporto', 'Alameda', 'Área', 'Avenida', 'Campo', 'Chácara', 'Colônia', 'Condomínio', 'Conjunto', 'Distrito',\n"
 + "                    'Esplanada', 'Estação', 'Estrada', 'Favela', 'Fazenda', 'Feira', 'Jardim', 'Ladeira', 'Largo', 'Lago', 'Lagoa', 'Loteamento', 'Núcleo', 'Parque', 'Passarela', 'Pátio', 'Praça',\n"
 + "                    'Quadra', 'Recanto', 'Residencial', 'Rodovia', 'Rua', 'Setor', 'Sítio', 'Travessa', 'Trevo', 'Trecho', 'Vale', 'Vereda', 'Via', 'Viaduto', 'Viela', 'Via'];\n"
 + "                this.$scope.pais = ['Brasil'];\n"
 + "\n");
 }
 fwForm.write("		},\n"
 + "	\n"
 + "			// Demais métodos do controller\n");

 //        for (Field atributo : Util.getTodosAtributos(classeEntidade)) {
 //            if (atributo.isAnnotationPresent(ManyToMany.class)) {
 //
 //                fwForm.write(""
 //                        + "            refresh" + Util.getTipoGenerico(atributo).getSimpleName() + ": function () {\n"
 //                        + "                var $scope = this.$scope;\n"
 //                        + "                this." + Util.getTipoGenerico(atributo).getSimpleName() + "Service.search($scope." + Util.primeiraMinuscula(Util.getTipoGenerico(atributo).getSimpleName()) + "Pesquisa, ['" + Util.primeiroAtributo(Util.getTipoGenerico(atributo)).getName() + "']).then(function (result) {\n"
 //                        + "                    $scope.lista" + Util.getTipoGenerico(atributo).getSimpleName() + Util.primeiraMaiuscula(atributo.getName()) + "= result.values;\n"
 //                        + "                });\n"
 //                        + "            },\n");
 //
 //                fwForm.write(""
 //                        + "            add" + Util.primeiraMaiuscula(atributo.getName()) + ": function (objeto) {\n"
 //                        + "                var index = this.indexOf" + Util.primeiraMaiuscula(atributo.getName()) + "(objeto);\n"
 //                        + "                this.$scope.entity." + atributo.getName() + " = this.$scope.entity." + atributo.getName() + " || [];\n"
 //                        + "                this.$scope.entity." + atributo.getName() + ".push(objeto);\n"
 //                        + "                this.$scope.entity.lista" + Util.getTipoGenerico(atributo).getSimpleName() + Util.primeiraMaiuscula(atributo.getName()) + ".splice(index,1)\n"
 //                        + "            },\n"
 //                        + "            remove" + Util.primeiraMaiuscula(atributo.getName()) + ": function (objeto) {\n"
 //                        + "                var index = this.indexOf" + Util.primeiraMaiuscula(atributo.getName()) + "(objeto);\n"
 //                        + "                this.$scope.entity." + atributo.getName() + ".splice(index, 1);\n"
 //                        + "            },\n"
 //                        + "            indexOf" + Util.primeiraMaiuscula(atributo.getName()) + ": function (objeto) {\n"
 //                        + "                this.$scope.entity." + atributo.getName() + " = this.$scope.entity." + atributo.getName() + " || [];\n"
 //                        + "                var lista = this.$scope.entity." + atributo.getName() + ";\n"
 //                        + "                for (var i = 0; i < lista.length; i++) {\n"
 //                        + "                    if (lista[i].id == objeto.id) {\n"
 //                        + "                        return i;\n"
 //                        + "                    }\n"
 //                        + "                }\n"
 //                        + "                return -1;\n"
 //                        + "            },\n"
 //                        + "            contains" + Util.primeiraMaiuscula(atributo.getName()) + ": function (" + atributo.getName() + ") {\n"
 //                        + "                return this.indexOf" + Util.primeiraMaiuscula(atributo.getName()) + "(" + atributo.getName() + ") >= 0;\n"
 //                        + "            },"
 //                        + "");
 //            }
 //        }
 if (!atributosAddress.isEmpty()) {
 for (Field at : atributosAddress) {
 fwForm.write(""
 + ""
 + "            " + at.getName() + "UpdateAddress: function () {\n"
 + "                var escopo = this.$scope;\n"
 + "                this.$scope.urlWithCep = 'http://cep.republicavirtual.com.br/web_cep.php?cep=' + this.entity." + at.getName() + ".cep + '&formato=jsonp';\n"
 + "                this.$http.get(this.$scope.urlWithCep)\n"
 + "                        .success(function (data) {\n"
 + "                            escopo.entity." + at.getName() + ".localidade = data.cidade;\n"
 + "                            escopo.entity." + at.getName() + ".bairro = data.bairro;\n"
 + "                            escopo.entity." + at.getName() + ".uf = data.uf;\n"
 + "                            escopo.entity." + at.getName() + ".tipoLogradouro = data.tipo_logradouro;\n"
 + "                            escopo.entity." + at.getName() + ".logradouro = data.logradouro;\n"
 + "                            escopo.entity." + at.getName() + ".pais = 'Brasil';\n"
 + "                        })\n"
 + "                        \n"
 + "            },\n"
 + "            \n");
 }
 }

 for (Class tipo : dependenciasManyToOne) {

 fwForm.write(""
 + "            refreshLista" + tipo.getSimpleName() + ": function (pesquisa) {\n"
 + "                var $scope = this.$scope;\n"
 + "\n"
 + "                if (pesquisa.length == 0) {\n"
 + "                    pesquisa = \"%\";\n"
 + "                }\n"
 + "\n"
 + "                this." + tipo.getSimpleName() + "Service.search(pesquisa, ['" + Util.primeiroAtributo(tipo).getName() + "']).then(function (result) {\n"
 + "                    $scope.lista" + tipo.getSimpleName() + " = result.values;\n"
 + "                });\n"
 + "            },\n");
 }
 fwForm.write(""
 + "        }\n"
 + "    });\n"
 + "});"
 + "");

 fwForm.close();
 }

 public void geraServiceJs() throws IOException {
 File arquivoService = new File(pastaScripts + "/service.js");
 FileWriter fwService = new FileWriter(arquivoService);

 fwService.write(""
 + "define([\n"
 + "		'gumga-class',\n"
 + "		'gumga/services/basic-crud-service',\n"
 + "         'app/locations'\n"
 + "	], function(GumgaClass, BasicCrudService,API) {\n"
 + "\n"
 + "	\n"
 + "	function " + nomeEntidade + "Service($http, $q) {\n"
 + "		" + nomeEntidade + "Service.super.constructor.call(this, $http, $q, API.location+\"" + nomeEntidade.toLowerCase() + "\");\n"
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
 }

 public void geraModuleJs() throws IOException {
 File arquivoModule = new File(pastaScripts + "/module.js");
 FileWriter fwModule = new FileWriter(arquivoModule);

 fwModule.write(""
 + "define(function(require) {\n"
 + "	\n"
 + "	require('gumga-components');\n"
 + "	require('app-commons/modules/crud-module').constant('baseTemplateURL', '" + nomeEntidade.toLowerCase() + "');\n");

 for (Class classe : dependenciasManyToMany) {
 fwModule.write("require('app/" + classeEntidade.getSimpleName().toLowerCase() + "/directive" + classe.getSimpleName() + "');\n");

 }

 fwModule.write("	\n"
 + "	return require('angular')\n"
 + "		.module('app." + nomeEntidade.toLowerCase() + "', [\"app.base.crud\", 'gumga.components'");

 for (Class classe : dependenciasManyToMany) {
 fwModule.write(", 'manyToMany" + classe.getSimpleName() + "'");
 }

 fwModule.write("])\n"
 + "		\n"
 + "		.service('EntityService', require('app/" + nomeEntidade.toLowerCase() + "/service'))\n\n");

 for (Class tipo : dependenciasManyToOne) {
 fwModule.write(".service(\"" + tipo.getSimpleName() + "Service\", require('app/" + tipo.getSimpleName().toLowerCase() + "/service'))\n");
 }
 for (Class tipo : dependenciasManyToMany) {
 fwModule.write(".service(\"" + tipo.getSimpleName() + "Service\", require('app/" + tipo.getSimpleName().toLowerCase() + "/service'))\n");
 }
 for (Class tipo : dependenciasOneToMany) {
 fwModule.write(".service(\"" + tipo.getSimpleName() + "Service\", require('app/" + tipo.getSimpleName().toLowerCase() + "/service'))\n");
 }

 //.controller("ItemModalController", require("app/venda/controllers/itens_modal"))
 for (Field f : Util.getTodosAtributosNaoEstaticos(classeEntidade)) {
 if (f.isAnnotationPresent(OneToMany.class)) {
 fwModule.write(".controller(\"" + Util.primeiraMaiuscula(f.getName()) + "ModalController\", require(\"app/" + classeEntidade.getSimpleName().toLowerCase() + "/controllers/" + f.getName() + "_modal\"))\n");
 }
 }

 fwModule.write(""
 + "		\n"
 + "		.controller(\"ListController\", require('app/" + nomeEntidade.toLowerCase() + "/controllers/list'))\n"
 + "		.controller(\"FormController\", require('app/" + nomeEntidade.toLowerCase() + "/controllers/form'));\n"
 + "	\n"
 + "});\n"
 + "");

 fwModule.close();
 }

 private void adicionaAoMenu() {
 try {
 Util.adicionaLinha(pastaGumgaJs + "directives.js", "//FIM MENU", "+ \"      <li><a href=\\\"/" + project.getParent().getName() + "/crud/" + nomeEntidade.toLowerCase() + "/base.html\\\" gumga-menu-id=\\\"" + nomeEntidade.toLowerCase() + "\\\">" + nomeEntidade + "</a></li>\\n\"");

 } catch (Exception ex) {
 getLog().error(ex);
 }

 }

 private String geraModalOneToMany(Field atributo) throws IOException {
 geraControladorModalOneToMany(atributo);
 String nomeArquivo = atributo.getName() + "_modal.jsp";
 File arquivoModal = new File(pastaHtml + "/" + nomeArquivo);
 try (FileWriter fwModal = new FileWriter(arquivoModal)) {
 fwModal.write(""
 + "<%@ page language=\"java\" contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\"%>\n"
 + "<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\" %>\n"
 + "<%@ taglib uri=\"http://gumga.com.br/jsp/tags\" prefix=\"g\" %>\n"
 + "\n"
 + "<div class=\"modal-body\">\n"
 + "    <form name=\"itemForm\" gumga-form-errors gumga-ng-model-errors>\n");

 geraCampos(Util.getTipoGenerico(atributo), fwModal, "ctrl.");

 fwModal.write(""
 + "    </form>\n"
 + "</div>\n"
 + "<div class=\"modal-footer\">\n"
 + "    <button class=\"btn btn-primary\" ng-click=\"$close(ctrl.entity)\">Selecionar</button>\n"
 + "    <button class=\"btn btn-default\" ng-click=\"$dismiss('close')\">Cancelar</button>\n"
 + "</div>"
 + "");
 }
 return nomeArquivo;

 }

 private void geraControladorModalOneToMany(Field atributo) throws IOException {
 String nomeArquivo = atributo.getName() + "_modal.js";
 File arquivoModal = new File(pastaControllers + "/" + nomeArquivo);
 FileWriter fwModal = new FileWriter(arquivoModal);

 fwModal.write(""
 + "define([\"gumga-class\"], function (e) {\n"
 + "    return e.create({$inject: [\"$scope\", \"$modalInstance\", \"entity\"], constructor: function (e, t, n) {\n"
 + "            this.$modalInstance = t, this.$scope = e, e.gumgaModalController = this, e.entity = n\n"
 + "        }, prototype: {confirm: function () {\n"
 + "                this.$modalInstance.close(this.$scope.entity)\n"
 + "            }, cancel: function () {\n"
 + "                this.$modalInstance.dismiss()\n"
 + "            }, validateModal: function () {\n"
 + "                return!0\n"
 + "            }}})\n"
 + "});"
 + "");

 fwModal.close();

 }

 private void geraEntradaGumgaFile(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) {

 }

 private void geraEntradaDate(FileWriter fwForm, Field atributo, boolean requerido, boolean primeiro, String controller) throws IOException {
 fwForm.write(""
 + "	<div class=\"form-group\" gumga-form-group=\"" + atributo.getName() + "\">\n"
 + "             <label class=\"control-label\">" + Util.etiqueta(atributo) + "</label><br>\n"
 + "             <input class=\"form-control\" ng-model=\"" + controller + "entity." + atributo.getName() + "\" gumga-datepicker-popup />"
 + "		<gumga:input:errors field=\"" + atributo.getName() + "\"></gumga:input:errors>\n"
 + "	</div>\n");
 }

 private void geraDiretivasManyToMany() throws IOException {

 for (Class classe : dependenciasManyToMany) {

 File arquivo = new File(pastaScripts + "/directive" + classe.getSimpleName() + ".js");
 FileWriter fw = new FileWriter(arquivo);

 fw.write(""
 + ""
 + "define(function (require) {\n"
 + "\n"
 + "    return require('angular').module('manyToMany" + classe.getSimpleName() + "', [])\n"
 + "            .service('" + classe.getSimpleName() + "Service', require('app/" + classe.getSimpleName().toLowerCase() + "/service'))\n"
 + "            .directive('gumgaManyToMany" + classe.getSimpleName() + "', ['" + classe.getSimpleName() + "Service', function (" + classe.getSimpleName() + "Service) {\n"
 + "                    return {\n"
 + "                        restrict: 'E',\n"
 + "                        scope:\n"
 + "                                {\n"
 + "                                    list: '=entityList',\n"
 + "                                    label: '@'\n"
 + "                                },\n"
 + "                        template:\n"
 + "                                \"<div class=\\\"form-group\\\">  \" +\n"
 + "                                \"   <div class=\\\"row\\\">\" +\n"
 + "                                \"       <div class=\\\"col-md-6\\\">\" +\n"
 + "                                \"           <label class=\\\"control-label\\\">" + classe.getSimpleName() + " {{label}}</label>\" +\n"
 + "                                '           <input type=\"text\" ng-model=\"filter\" ng-change=\"search()\" class=\"form-control\"/><br>' +\n"
 + "                                \"               <ul class=\\\"list-group\\\">\" +\n"
 + "                                \"                   <li class=\\\"list-group-item\\\" ng-repeat=\\\"f in firstList| orderBy: '" + Util.primeiroAtributo(classe).getName() + "'\\\" ng-show=\\\"!contains(f)\\\" >\" +\n"
 + "                                \"                      <button type=\\\"button\\\" class=\\\"btn btn-default btn-lg btn-block\\\" ng-click=\\\"add(f)\\\" >{{f." + Util.primeiroAtributo(classe).getName() + "}}</button>\" +\n"
 + "                                \"                   </li>\" +\n"
 + "                                \"               </ul>\" +\n"
 + "                                \"       </div>\" +\n"
 + "                                \"       <div class=\\\"col-md-6\\\">\" +\n"
 + "                                \"           <label class=\\\"control-label\\\">" + classe.getSimpleName() + " {{label}} </label>\" +\n"
 + "                                \"           <input type=\\\"text\\\" ng-model=\\\"filtro\\\" class=\\\"form-control\\\"/><br>\" +\n"
 + "                                \"               <ul class=\\\"list-group\\\">\" +\n"
 + "                                \"                   <li class=\\\"list-group-item\\\" ng-repeat=\\\"f in list | filter: filtro |orderBy: '" + Util.primeiroAtributo(classe).getName() + "'\\\">\" +\n"
 + "                                \"                       <button type=\\\"button\\\" class=\\\"btn btn-default btn-lg btn-block\\\" ng-click=\\\"remove(f)\\\">{{f." + Util.primeiroAtributo(classe).getName() + "}}</button>\" +\n"
 + "                                \"                   </li>\" +\n"
 + "                                \"               </ul>\" +\n"
 + "                                \"       </div>\" +\n"
 + "                                \"   </div>\" +\n"
 + "                                \"</div>\",\n"
 + "                        link: function (scope) {\n"
 + "                            scope.search = function () {\n"
 + "                                " + classe.getSimpleName() + "Service.search(scope.filter, ['" + Util.primeiroAtributo(classe).getName() + "']).then(function (result) {\n"
 + "                                    scope.firstList = result.values;\n"
 + "                                })\n"
 + "                            }\n"
 + "                            scope.add = function (obj) {\n"
 + "                                var index = this.indexOf(obj);\n"
 + "                                scope.list = scope.list || [];\n"
 + "                                scope.list.push(obj);\n"
 + "                            }\n"
 + "\n"
 + "                            scope.remove = function (obj) {\n"
 + "                                var index = this.indexOf(obj);\n"
 + "                                scope.list.splice(index, 1);\n"
 + "                            }\n"
 + "\n"
 + "                            scope.indexOf = function (obj) {\n"
 + "                                scope.list = scope.list || [];\n"
 + "                                var listAux = scope.list;\n"
 + "                                for (var i = 0; i < listAux.length; i++) {\n"
 + "                                    if (listAux[i].id === obj.id) {\n"
 + "                                        return i;\n"
 + "                                    }\n"
 + "                                }\n"
 + "                                return -1;\n"
 + "                            }\n"
 + "                            scope.contains = function (obj) {\n"
 + "                                return this.indexOf(obj) >= 0\n"
 + "                            }\n"
 + "                            \n"
 + "                            scope.search();\n"
 + "                        }\n"
 + "                    }\n"
 + "                }])\n"
 + "});"
 + ""
 + "");

 fw.close();
 }
 }
    
 */

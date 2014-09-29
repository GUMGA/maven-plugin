/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gumga.maven.plugins.gumgag;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
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
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        nomePacoteBase = nomeCompletoEntidade.substring(0, nomeCompletoEntidade.lastIndexOf(".domain"));
        nomeEntidade = nomeCompletoEntidade.substring(nomeCompletoEntidade.lastIndexOf('.') + 1);
        
        nomePacoteGateway = nomePacoteBase + ".gateway";
        nomePacoteDto = nomePacoteGateway + ".dto";
        nomePacoteTranslator = nomePacoteGateway + ".translator";
        
        pastaGateway = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteGateway.replaceAll("\\.", "/"));
        pastaDto = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteDto.replaceAll("\\.", "/"));
        pastaTranslator = project.getCompileSourceRoots().get(0) + "/".concat(nomePacoteTranslator.replaceAll("\\.", "/"));
        
        incializaClassLoader();
        
        try {
            Class classe = classLoader.loadClass(nomeCompletoEntidade);
            for (Field f : classe.getDeclaredFields()) {
                getLog().info(f.getName());
            }
        } catch (ClassNotFoundException ex) {
            getLog().error(ex);
        }
        
        geraGateway();
        geraDto();
        geraTranslator();
        
        getLog().info("Iniciando plugin Gerador de Gateway");
        getLog().info("Gerando para " + nomeEntidade);
        
    }
    
    private void geraGateway() {
        File f = new File(pastaGateway);
        f.mkdirs();
        File arquivoClasse = new File(pastaGateway + "/" + nomeEntidade + "Gateway.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
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
                    + "\n");
            
            fw.close();
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }
    
    private void geraTranslator() {
        File f = new File(pastaTranslator);
        f.mkdirs();
        File arquivoClasse = new File(pastaTranslator + "/" + nomeEntidade + "Translator.java");
        try {
            FileWriter fw = new FileWriter(arquivoClasse);
            fw.write(""
                    + "\n");
            
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
                getLog().info(" Claspath " + element);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }
            classLoader = new URLClassLoader(runtimeUrls,
                    Thread.currentThread().getContextClassLoader());
            
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }
    
}

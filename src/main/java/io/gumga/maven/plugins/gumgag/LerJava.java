/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gumga.maven.plugins.gumgag;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author munif
 */
@Mojo(name = "desgera", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class LerJava extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "entidade", defaultValue = "all")
    private String nomeCompletoEntidade;
    private Class classeEntidade;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            System.out.println("Inicio");
            classeEntidade = Util.getClassLoader(project).loadClass(nomeCompletoEntidade);
            System.out.println("Classe " + classeEntidade);

            System.out.println("Super classe " + classeEntidade.getSuperclass().getCanonicalName());

            String saida = "mvn br.com.gumga:gumgag:entidade -Dentidade=" + nomeCompletoEntidade + " -Datributos=\"";

            for (Field f : classeEntidade.getDeclaredFields()) {
                saida += f.getName() + ":" + f.getType().getSimpleName();
                if (f.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) f.getGenericType();
                    Type[] typeArguments = type.getActualTypeArguments();

                    saida += "<";
                    for (Type t : typeArguments) {
                        String tn=t.getTypeName();
                        tn=tn.substring(tn.lastIndexOf(".")+1);
                        saida += tn + ",";
                    }
                    saida = saida.substring(0, saida.length() - 1);
                    saida += ">";
                }

                Annotation[] annotations = f.getDeclaredAnnotations();
                if (annotations.length > 0) {
                    saida += ":";
                    for (Annotation an : annotations) {
                        String na=an.toString();
                        na=na.substring(na.lastIndexOf(".")+1,na.indexOf("("));

                        saida += "@"+na + " ";
                    }

                }
                saida += ",";
            }
            saida = saida.substring(0, saida.length() - 1);
            saida += "\"";

            if ((!classeEntidade.getSuperclass().equals(Object.class)) && (!classeEntidade.getSuperclass().getCanonicalName().equals("gumga.framework.domain.GumgaModel"))) {
                saida += " -Dsuper=\"" + classeEntidade.getSuperclass().getCanonicalName() + "\"";
            }

            System.out.println(saida);

        } catch (Exception ex) {
            Logger.getLogger(LerJava.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}

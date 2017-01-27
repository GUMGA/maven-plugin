package io.gumga.maven.plugins.gumgag;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.jdbc.support.JdbcUtils;

/**
 *
 * @author munif
 */
@Mojo(name = "dbunit", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GeraDBUnit extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "driver", defaultValue = "com.mysql.jdbc.Driver")
    private String driver;

    @Parameter(property = "url", defaultValue = "jdbc:mysql://localhost:3306/finance?zeroDateTimeBehavior=convertToNull", required = true)
    private String url;

    @Parameter(property = "user", defaultValue = "root", required = true)
    private String user;

    @Parameter(property = "password", defaultValue = "senha", required = true)
    private String password;

    @Parameter(property = "table", required = true)
    private String table;

    private Connection connection;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        createConnection();
        gospeXML(table);
        closeConnection(connection);

    }

    private void createConnection() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Conected in " + url);
        } catch (SQLException ex) {
            Logger.getLogger(GeraDBUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GeraDBUnit.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(GeraDBUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void gospeXML(String table) {
        try {
            Statement statement = connection.createStatement();
            ResultSet res = statement.executeQuery("SELECT * FROM " + table);
            ResultSetMetaData metaData = res.getMetaData();

            int columnCount = metaData.getColumnCount();

            System.out.println(""
                    + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<dataset>"
                    + "");

            while (res.next()) {
                System.out.println("<" + table + "\n");

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    System.out.println(Util.IDENTACAO08 + columnName + "=\"" + res.getString(i) + "\"");
                }

                System.out.println("/>\n");
            }

            System.out.println(""
                    + "</dataset>"
                    + "");

            statement.close();

        } catch (SQLException ex) {
            Logger.getLogger(GeraDBUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

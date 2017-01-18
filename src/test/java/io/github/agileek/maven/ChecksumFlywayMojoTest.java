package io.github.agileek.maven;


import com.helger.jcodemodel.JCodeModel;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.project.MavenProject;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ChecksumFlywayMojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ChecksumFlywayMojo tested;

    @Before
    public void setUp() throws Exception {
        tested = new ChecksumFlywayMojo();
    }

    @Test
    public void shouldExecute() throws Exception {

        tested.project = new MavenProject() {
            @Override
            public List<String> getCompileSourceRoots() {
                List<String> files = new ArrayList<String>();
                files.add("src/test/resources/java/");
                return files;
            }

        };
        tested.location = "db/migration";
        tested.generatedSourcesFolder = temporaryFolder.newFolder().getAbsolutePath();
        tested.execute();

        assertThat(new File(tested.generatedSourcesFolder, "io/github/agileek/flyway/JavaMigrationChecksums.java"))
                .hasSameContentAs(new File("src/test/resources/ExpectedJavaMigrationFile.java"));
    }

    @Test
    public void shouldGenerateFileChecksum() throws Exception {
        List<File> files = new ArrayList<File>();
        files.add(new File("src/test/resources/java/db/migration/Toto.java"));

        JCodeModel actual = tested.generateEnumWithFilesChecksum(files);
        File generatedJavaFolder = temporaryFolder.newFolder();
        actual.build(generatedJavaFolder, (PrintStream) null);

        assertThat(new File(generatedJavaFolder.getAbsolutePath(), "io/github/agileek/flyway/JavaMigrationChecksums.java"))
                .hasSameContentAs(new File("src/test/resources/ExpectedJavaMigrationFile.java"));
    }

    @Test
    public void shouldComputeFileChecksum() throws Exception {
        String actual = tested.computeFileChecksum(new File("src/test/resources/java/db/migration/Toto.java"));

        assertThat(actual).isEqualTo("60e90cc0aedc457d95ebfa601f366c10");
    }

    @Test
    public void shouldGetJavaFilesFromSourceRoots() throws Exception {
        List<String> compileSourceRoots = new ArrayList<String>();
        compileSourceRoots.add("src/test/resources/java");

        List<File> javaFiles = tested.getJavaFiles(compileSourceRoots, "/db/migration");

        assertThat(javaFiles).containsExactly(new File("src/test/resources/java/db/migration/Toto.java"));
    }

    @Test
    public void shouldGetJavaFilesFromSourceRoots_noFiles() throws Exception {
        List<String> compileSourceRoots = new ArrayList<String>();
        compileSourceRoots.add("src/test/resources/");

        List<File> javaFiles = tested.getJavaFiles(compileSourceRoots, "/any");

        assertThat(javaFiles).isEmpty();
    }
}
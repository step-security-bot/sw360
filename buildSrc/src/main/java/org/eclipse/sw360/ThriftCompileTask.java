/*
 * Copyright Helio Chissini de Castro 2023. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.stream.Stream;

public class ThriftCompileTask extends DefaultTask {
    @TaskAction
    public void compileThrift() {
        ThriftPluginExtension thriftExtension = getProject().getExtensions().getByType(ThriftPluginExtension.class);
        String sourceDirs = String.join(",", thriftExtension.getSourceDirs());
        String outputDir = thriftExtension.getOutputDir();

        // Create output directory if it doesn't exist
        new File(outputDir).mkdirs();

        if (thriftExtension.getSkipIfExists() && new File(outputDir).exists()) {
            getLogger().info("Thrift files already generated. Skipping compilation.");
        } else {
            for (String sourceDir : thriftExtension.getSourceDirs()) {
                try (Stream<Path> paths = Files.walk(Paths.get(sourceDir))) {
                    paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".thrift"))
                        .forEach(path -> {
                            getProject().exec(execSpec -> {
                                execSpec.commandLine("thrift", "-gen", "java", "-r", "-out", outputDir, "-I", sourceDir, path.toString());
                            });
                        });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

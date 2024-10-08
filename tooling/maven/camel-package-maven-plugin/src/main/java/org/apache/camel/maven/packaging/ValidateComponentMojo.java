/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.maven.packaging;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.camel.tooling.util.PackageHelper;
import org.apache.camel.tooling.util.Strings;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.build.BuildContext;

/**
 * Validate a Camel component analyzing if the meta-data files for
 * <ul>
 * <li>components</li>
 * <li>dataformats</li>
 * <li>languages</li>
 * </ul>
 * all contains the needed meta-data such as assigned labels, documentation for each option
 */
@Mojo(name = "validate-components", threadSafe = true)
public class ValidateComponentMojo extends AbstractGeneratorMojo {

    /**
     * Whether to validate if the components, data formats, and languages are properly documented and have all the
     * necessary details.
     */
    @Parameter(defaultValue = "true")
    protected Boolean validate;

    /**
     * The output directory for the generated component files
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    protected File outDir;

    @Inject
    public ValidateComponentMojo(MavenProjectHelper projectHelper, BuildContext buildContext) {
        super(projectHelper, buildContext);
    }

    /**
     * Execute goal.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException execution of the main class or one of the threads it
     *                                                        generated failed.
     * @throws org.apache.maven.plugin.MojoFailureException   something bad happened...
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (validate == null) {
            validate = true;
        }
        if (outDir == null) {
            outDir = new File(project.getBuild().getOutputDirectory());
        }
        if (!validate) {
            getLog().info("Validation disabled");
        } else {
            List<Path> jsonFiles;
            try (Stream<Path> stream = PackageHelper.findJsonFiles(outDir.toPath())) {
                jsonFiles = stream.toList();
            }
            boolean failed = false;

            for (Path file : jsonFiles) {
                final String name = PackageHelper.asName(file);
                final ErrorDetail detail = new ErrorDetail();

                if (getLog().isDebugEnabled()) {
                    getLog().debug("Validating file " + file);
                }

                try {
                    ValidateHelper.validate(file.toFile(), detail);
                } catch (Exception e) {
                    // ignore as it may not be a camel component json file
                }

                if (detail.hasErrors()) {
                    failed = true;
                    getLog().warn("The " + detail.getKind() + ": " + name + " has validation errors");
                    if (detail.isMissingDescription()) {
                        getLog().warn("Missing description on: " + detail.getKind());
                    }
                    if (detail.isMissingLabel()) {
                        getLog().warn("Missing label on: " + detail.getKind());
                    }
                    if (detail.isMissingSyntax()) {
                        getLog().warn("Missing syntax on endpoint");
                    }
                    if (detail.isMissingUriPath()) {
                        getLog().warn("Missing @UriPath on endpoint");
                    }
                    if (!detail.getMissingComponentDocumentation().isEmpty()) {
                        getLog().warn("Missing component documentation for the following options:"
                                      + Strings.indentCollection("\n\t", detail.getMissingComponentDocumentation()));
                    }
                    if (!detail.getMissingEndpointDocumentation().isEmpty()) {
                        getLog().warn("Missing endpoint documentation for the following options:"
                                      + Strings.indentCollection("\n\t", detail.getMissingEndpointDocumentation()));
                    }
                }
            }

            if (failed) {
                throw new MojoFailureException("Validating failed, see errors above!");
            } else {
                getLog().info("Validation complete");
            }
        }
    }

}

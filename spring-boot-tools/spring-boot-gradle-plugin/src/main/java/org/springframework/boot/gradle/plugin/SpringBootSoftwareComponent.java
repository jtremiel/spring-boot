/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.gradle.plugin;

import java.util.Collections;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.attributes.Usages;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.component.UsageContext;

/**
 * {@link org.gradle.api.component.SoftwareComponent} for a Spring Boot fat jar or war.
 *
 * @author Andy Wilkinson
 */
final class SpringBootSoftwareComponent implements SoftwareComponentInternal {

	private final PublishArtifact artifact;

	private final String name;

	private final Usage usage;

	SpringBootSoftwareComponent(Project project, PublishArtifact artifact, String name) {
		this.artifact = artifact;
		this.name = name;
		this.usage = createUsage(project);
	}

	private static Usage createUsage(Project project) {
		try {
			return Usages.usage("master");
		}
		catch (Throwable ex) {
			return createUsageUsingObjectFactory(project);
		}
	}

	private static Usage createUsageUsingObjectFactory(Project project) {
		try {
			Object objects = project.getClass().getMethod("getObjects").invoke(project);
			return (Usage) objects.getClass()
					.getMethod("named", Class.class, String.class)
					.invoke(objects, Usage.class, "master");
		}
		catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Set<UsageContext> getUsages() {
		return Collections.singleton(new BootUsageContext(this.usage, this.artifact));
	}

	private static final class BootUsageContext implements UsageContext {

		private final Usage usage;

		private final PublishArtifact artifact;

		private BootUsageContext(Usage usage, PublishArtifact artifact) {
			this.usage = usage;
			this.artifact = artifact;
		}

		@Override
		public Usage getUsage() {
			return this.usage;
		}

		@Override
		public Set<PublishArtifact> getArtifacts() {
			return Collections.singleton(this.artifact);
		}

		@Override
		public Set<ModuleDependency> getDependencies() {
			return Collections.emptySet();
		}

	}

}

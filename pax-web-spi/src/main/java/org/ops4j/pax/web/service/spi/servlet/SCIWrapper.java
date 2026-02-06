/*
 * Copyright 2020 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.web.service.spi.servlet;

import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.ops4j.pax.web.service.spi.model.elements.ContainerInitializerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ServletContainerInitializer} that calls actual SCI with different {@link ServletContext}
 */
public class SCIWrapper implements ServletContainerInitializer, Comparable<SCIWrapper> {

	public static final Logger LOG = LoggerFactory.getLogger(DynamicRegistrations.class);

	private final OsgiDynamicServletContext context;
	private final ContainerInitializerModel model;

	public SCIWrapper(OsgiDynamicServletContext context, ContainerInitializerModel model) {
		this.context = context;
		this.model = model;
	}

	/**
	 * No-arg version that uses the classes from the model itself.
	 * @throws ServletException
	 */
	public void onStartup() throws ServletException {
		STARTUP_ARMED.set(Boolean.TRUE);
		try {
			model.getContainerInitializer().onStartup(model.getClasses(), context);
		} finally {
			STARTUP_ARMED.set(Boolean.FALSE);
		}
		context.rememberAttributesFromSCIs();
	}

	private static final ThreadLocal<Boolean> STARTUP_ARMED = ThreadLocal.withInitial(() -> Boolean.FALSE);

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		// just call with different context - the "ctx" parameter is a delegate inside
		// this.context (org.ops4j.pax.web.service.spi.servlet.OsgiServletContext.containerServletContext)
		STARTUP_ARMED.set(Boolean.TRUE);
		try {
			model.getContainerInitializer().onStartup(c, context);
		} finally {
			STARTUP_ARMED.set(Boolean.FALSE);
		}
	}

	public static boolean isStartupArmed() {
		return STARTUP_ARMED.get().booleanValue();
	}

	public ContainerInitializerModel getModel() {
		return model;
	}

	@Override
	public int compareTo(SCIWrapper o) {
		return model.compareTo(o.model);
	}

}

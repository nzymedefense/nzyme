/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.rest;

import app.nzyme.core.rest.authentication.PreMFASecured;
import com.google.common.collect.ImmutableList;
import app.nzyme.plugin.rest.security.RESTSecured;
import app.nzyme.core.rest.authentication.PrometheusBasicAuthSecured;
import app.nzyme.core.rest.authentication.TapSecured;
import jakarta.ws.rs.PUT;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.testng.annotations.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.lang.reflect.Method;
import java.util.List;

import static org.testng.Assert.fail;

public class ResourcesRequireAuthenticationTest {

    private final List<String> WHITELIST = ImmutableList.of(
            "WebInterfaceAssetsResource.getIndex",
            "WebInterfaceAssetsResource.get",
            "PingResource.ping",
            "InitialUserResource.createInitialUser",
            "AuthenticationResource.createSession",
            "TrackerWebHIDAssetsResource.getPage",
            "TrackerWebHIDAssetsResource.getFavicon",
            "TrackerWebHIDAssetsResource.getCSS",
            "TrackerWebHIDAssetsResource.getFont",
            "TrackerWebHIDAssetsResource.getPNG",
            "TrackerWebHIDAssetsResource.getJS"
    );

    private final List<String> PREMFA_AUTHENTICATED = ImmutableList.of(
            "AuthenticationResource.getSessionInformation",
            "AuthenticationResource.initializeMfaSetup",
            "AuthenticationResource.completeMfaSetup",
            "AuthenticationResource.verifyMfa",
            "AuthenticationResource.mfaRecoveryCodeValidation"
    );

    private final List<String> TAP_SECRET_AUTHENTICATED = ImmutableList.of(
            "StatusResource.status",
            "TablesResource.report"
    );

    private final List<String> PROMETHEUS_BASIC_AUTHENTICATED = ImmutableList.of(
            "PrometheusResource.metrics"
    );

    @Test
    public void testAllResources() {
        Reflections r = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("app.nzyme.core.rest.resources"))
                .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
        );

        int checked = 0;
        for (Class<?> clazz : r.getTypesAnnotatedWith(Path.class)) {
            for (Method method : clazz.getMethods()) {
                String id = method.getDeclaringClass().getSimpleName() + "." + method.getName();

                if (!method.isAnnotationPresent(GET.class) && !method.isAnnotationPresent(POST.class)
                    && !method.isAnnotationPresent(PUT.class)) {
                    // Don't compare non-REST resources.
                    continue;
                }

                if (WHITELIST.contains(id)) {
                    continue;
                }

                if (TAP_SECRET_AUTHENTICATED.contains(id)) {
                    if (!method.isAnnotationPresent(TapSecured.class) && !clazz.isAnnotationPresent(TapSecured.class)) {
                        fail("REST resource " + id + " is not annotated with @TapSecured.");
                    }
                } else if (PREMFA_AUTHENTICATED.contains(id)) {
                    if (!method.isAnnotationPresent(PreMFASecured.class) && !clazz.isAnnotationPresent(PreMFASecured.class)) {
                        fail("REST resource " + id + " is not annotated with @PreMFASecured.");
                    }
                } else if (PROMETHEUS_BASIC_AUTHENTICATED.contains(id)) {
                    if (!method.isAnnotationPresent(PrometheusBasicAuthSecured.class) && !clazz.isAnnotationPresent(PrometheusBasicAuthSecured.class)) {
                        fail("REST resource " + id + " is not annotated with @PrometheusBasicAuthSecured.");
                    }
                } else {
                    if (!method.isAnnotationPresent(RESTSecured.class) && !clazz.isAnnotationPresent(RESTSecured.class)) {
                        fail("REST resource " + id + " is not annotated with @RESTSecured.");
                    }
                }

                checked++;
            }
        }

        if (checked == 0) {
            fail("Did not check any resources. Wrong package name?");
        }
    }

}

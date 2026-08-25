package com.aegispay;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Fails the build if a module reaches into another module's internals
 * (anything not exposed via the module's api package or explicitly
 * designated named interfaces).
 */
class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(AegisPayApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void documentsModuleStructure() throws Exception {
        new Documenter(modules).writeModulesDocumentation().writeIndividualModulesDocumentation();
    }
}

/**
 * Autonomous remediation. Consumes aegis.risk.v1 and mutates the Account
 * aggregate; emits aegis.remediation.v1 outcomes via the outbox.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"shared"})
package com.aegispay.remediation;

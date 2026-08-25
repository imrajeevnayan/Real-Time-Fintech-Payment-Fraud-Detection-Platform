/**
 * Event-sourced audit ledger. Read-only consumer of all versioned topics.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"shared"})
package com.aegispay.ledger;

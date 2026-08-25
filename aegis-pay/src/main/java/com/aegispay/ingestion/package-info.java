/**
 * Transaction intake. External API surface: {@code api} package only.
 * The domain Transaction aggregate and repository are internal.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"shared"})
package com.aegispay.ingestion;

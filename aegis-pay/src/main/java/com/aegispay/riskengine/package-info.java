/**
 * AI risk scoring. No other module may call the evaluation service directly;
 * they publish aegis.transaction.v1 and this module reacts.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"shared"})
package com.aegispay.riskengine;

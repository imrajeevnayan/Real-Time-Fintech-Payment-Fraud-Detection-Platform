package com.aegispay.ingestion.api;

import com.aegispay.ingestion.api.dto.SubmitTransactionRequest;
import com.aegispay.ingestion.application.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionIngestionController {

    private final IngestionService ingestion;

    @Operation(summary = "Submit a transaction for real-time fraud evaluation")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Accepted; risk evaluation in progress"),
            @ApiResponse(responseCode = "400", description = "Validation failed (RFC 7807)"),
            @ApiResponse(responseCode = "429", description = "Rate limited")
    })
    @PostMapping
    ResponseEntity<Map<String, UUID>> submit(@Valid @RequestBody SubmitTransactionRequest request,
                                             UriComponentsBuilder uri) {
        var transactionId = ingestion.submit(request);
        return ResponseEntity
                .accepted()
                .location(URI.create(uri.path("/api/v1/transactions/{id}").build(transactionId)))
                .body(Map.of("transactionId", transactionId));
    }
}

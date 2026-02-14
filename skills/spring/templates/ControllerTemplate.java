@RestController
@RequestMapping("/api/{resources}")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {
    
    private final ResourceService service;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(service.findAll()));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ResourceResponse>> create(
            @Valid @RequestBody ResourceRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        log.info("Resource creation initiated by: {}", user.getUsername());
        ResourceResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource created successfully", response));
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResourceResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request
    ) {
        ResourceResponse response = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Resource updated successfully", response));
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Resource deleted successfully", null));
    }
}

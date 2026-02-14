/**
 * Response payload for [feature], intended for [export / display / listing].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureResponse {

    private Long id;
    private String name;
    private String status;          // formatted string, not raw enum
    private String createdDate;     // formatted, not LocalDateTime

    private List<ItemResponse> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        private Long id;
        private String label;
        private Long amount;
    }
}

/**
 * Request payload for [action].
 * [Describe modes or conditional logic if any.]
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureRequest {

    @NotNull(message = "Field is required")
    private Long requiredId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    // Optional field — document what null means
    private String optionalField;

    @Valid
    private NestedRequest nested;

    private List<Long> relatedIds;
}

/**
 * Brief description of what this entity represents.
 * Include business rules, ownership model, or multi-tenancy notes if relevant.
 */
@Entity
@Table(name = "table_name", indexes = {
        @Index(name = "idx_table_owner_id", columnList = "owner_id"),
        @Index(name = "idx_table_status", columnList = "status"),
        @Index(name = "idx_table_owner_status", columnList = "owner_id, status")  // composite
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"children", "tags", "owner"})  // exclude all relations
public class MyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Required string field
    @Column(nullable = false)
    private String name;

    // Optional text field with length constraint
    @Column(length = 1000)
    private String description;

    // Enum field — always STRING
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MyStatusEnum status = MyStatusEnum.ACTIVE;

    // Required relation — always LAZY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Optional relation — null means [explain business meaning here].
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_id")
    private RelatedEntity related;

    // Owned one-to-many collection
    @OneToMany(mappedBy = "myEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChildEntity> children = new ArrayList<>();

    // Many-to-many — use Set, BatchSize, explicit JoinTable
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @JoinTable(
        name = "myentity_tags",
        joinColumns = @JoinColumn(name = "entity_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // Counter with safe default
    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    // Optimistic locking — for concurrent write protection
    @Builder.Default
    @Version
    private Integer version = 0;
}

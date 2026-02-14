import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * [Short description of what this enum represents.]
 *
 * [Optional: describe the FSM flow if applicable]
 * e.g., PENDING → ACTIVE → COMPLETED (terminal)
 *
 * Terminal states (no further transitions): [list them]
 */
@Getter
public enum MyStatus {

    /**
     * [Initial state description.]
     */
    PENDING("Chờ xử lý"),

    ACTIVE("Đang hoạt động"),

    /**
     * Terminal state — no further transitions allowed.
     */
    COMPLETED("Hoàn thành"),

    /**
     * Terminal state.
     */
    CANCELLED("Đã hủy");

    private final String displayName;

    // ─── FSM (remove if not needed) ───────────────────────────────────────

    private static final Map<MyStatus, Set<MyStatus>> ALLOWED_TRANSITIONS = Map.of(
            PENDING, Set.of(ACTIVE, CANCELLED),
            ACTIVE, Set.of(COMPLETED, CANCELLED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of());

    MyStatus(String displayName) {
        this.displayName = displayName;
    }

    // ─── State checks ─────────────────────────────────────────────────────

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    // ─── FSM transitions (remove if not needed) ───────────────────────────

    public MyStatus transitionTo(MyStatus next) {
        if (!ALLOWED_TRANSITIONS.get(this).contains(next)) {
            throw new InvalidInputException(
                    String.format("Cannot transition from %s to %s",
                            this.displayName, next.displayName));
        }
        return next;
    }

    public boolean canTransitionTo(MyStatus next) {
        return ALLOWED_TRANSITIONS.get(this).contains(next);
    }

    // ─── Lookup ───────────────────────────────────────────────────────────

    public static Optional<MyStatus> fromName(String name) {
        try {
            return Optional.of(MyStatus.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static MyStatus fromNameOrThrow(String name) {
        return fromName(name).orElseThrow(() -> new InvalidInputException("Invalid status: '" + name + "'"));
    }
}

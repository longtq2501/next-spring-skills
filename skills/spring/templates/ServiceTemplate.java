/** ─── INTERFACE (TutorService.java) ────────────────────────────────── **/

public interface MyService {
    Page<MyResponse> getAll(String search, Pageable pageable);

    MyResponse getById(Long id);

    MyResponse create(MyRequest request);

    MyResponse update(Long id, MyRequest request);

    void delete(Long id);
}

/** ─── IMPLEMENTATION (TutorServiceImpl.java) ───────────────────────── **/

@Service
@RequiredArgsConstructor
@Slf4j
public class MyServiceImpl implements MyService {

    private final MyRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<MyResponse> getAll(String search, Pageable pageable) {
        // ... conditional filtering
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MyResponse getById(Long id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Not found: " + id));
    }

    @Override
    @Transactional
    public MyResponse create(MyRequest request) {
        // 1. Validate
        if (repository.existsByName(request.getName())) {
            throw new AlreadyExistsException("Duplicate name: " + request.getName());
        }

        // 2. Build
        MyEntity entity = MyEntity.builder()
                .name(request.getName())
                .build();

        // 3. Save + Map
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public MyResponse update(Long id, MyRequest request) {
        MyEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found: " + id));

        entity.setName(request.getName());
        return mapToResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Not found: " + id);
        }
        repository.deleteById(id);
    }

    private MyResponse mapToResponse(MyEntity entity) {
        return MyResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}

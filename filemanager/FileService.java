@Service
public class FileService {
    private final AskRootProperties props;
    private final AgentChannel channel;

    public Path resolve(String requested) {
        Path candidate = Paths.get(requested).normalize();
        if (candidate.isAbsolute() || requested.contains("..")) throw new Forbidden("path_escape");
        Path base = Paths.get(props.getFiles().getAllowedRoots().split(",")[0]);
        Path full = base.resolve(candidate).normalize();
        if (!full.startsWith(base)) throw new Forbidden("outside_root");
        return full;
    }

    public void assertUpload(MultipartFile f) {
        if (f.getSize() > props.getFiles().getMaxUploadBytes()) throw new Conflict("too_large");
        String name = f.getOriginalFilename();
        if (name == null || !name.matches("^[A-Za-z0-9._-]{1,120}$")) throw new Conflict("bad_name");
    }
}

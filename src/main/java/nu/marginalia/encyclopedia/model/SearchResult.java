package nu.marginalia.encyclopedia.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SearchResult(
        @NotNull
        List<String> results,
        @NotNull
        String searchTerm,
        @Nullable
        String nextPrefix)
{
    public SearchResult(List<String> results, String searchTerm) {
        this(results, searchTerm, null);
    }

    public SearchResult(String searchTerm) {
        this(List.of(), searchTerm);
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }
}

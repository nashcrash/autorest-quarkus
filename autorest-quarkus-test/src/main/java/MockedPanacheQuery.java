import com.mongodb.ReadPreference;
import com.mongodb.client.model.Collation;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MockedPanacheQuery implements PanacheQuery<Object> {
    List<?> result;

    public MockedPanacheQuery(List<?> result) {
        this.result = result;
    }

    @Override
    public <T> PanacheQuery<T> project(Class<T> type) {
        return null;
    }

    @Override
    public <T> PanacheQuery<T> page(Page page) {
        return (PanacheQuery<T>) this;
    }

    @Override
    public <T> PanacheQuery<T> page(int pageIndex, int pageSize) {
        return (PanacheQuery<T>) this;
    }

    @Override
    public <T> PanacheQuery<T> nextPage() {
        return (PanacheQuery<T>) this;
    }

    @Override
    public <T> PanacheQuery<T> previousPage() {
        return (PanacheQuery<T>) this;
    }

    @Override
    public <T> PanacheQuery<T> firstPage() {
        return (PanacheQuery<T>) this;
    }

    @Override
    public <T> PanacheQuery<T> lastPage() {
        return (PanacheQuery<T>) this;
    }

    @Override
    public boolean hasNextPage() {
        return false;
    }

    @Override
    public boolean hasPreviousPage() {
        return false;
    }

    @Override
    public int pageCount() {
        return 0;
    }

    @Override
    public Page page() {
        return null;
    }

    @Override
    public <T> PanacheQuery<T> range(int startIndex, int lastIndex) {
        return null;
    }

    @Override
    public <T> PanacheQuery<T> withCollation(Collation collation) {
        return null;
    }

    @Override
    public <T> PanacheQuery<T> withReadPreference(ReadPreference readPreference) {
        return null;
    }

    @Override
    public <T> PanacheQuery<T> withBatchSize(int batchSize) {
        return null;
    }

    @Override
    public long count() {
        return this.result.size();
    }

    @Override
    public <T> List<T> list() {
        return (List<T>) this.result;
    }

    @Override
    public <T> Stream<T> stream() {
        return (Stream<T>) this.result.stream();
    }

    @Override
    public <T> T firstResult() {
        return null;
    }

    @Override
    public <T> Optional<T> firstResultOptional() {
        return Optional.empty();
    }

    @Override
    public <T> T singleResult() {
        return null;
    }

    @Override
    public <T> Optional<T> singleResultOptional() {
        return Optional.empty();
    }
}
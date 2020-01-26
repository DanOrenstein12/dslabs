package dslabs.kvstore;

import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;


@ToString
@EqualsAndHashCode
public class KVStore implements Application {

    public interface KVStoreCommand extends Command {
    }

    public interface SingleKeyCommand extends KVStoreCommand {
        String key();
    }

    @Data
    public static final class Get implements SingleKeyCommand {
        @NonNull private final String key;

        @Override
        public boolean readOnly() {
            return true;
        }
    }

    @Data
    public static final class Put implements SingleKeyCommand {
        @NonNull private final String key, value;
    }

    @Data
    public static final class Append implements SingleKeyCommand {
        @NonNull private final String key, value;
    }

    public interface KVStoreResult extends Result {
    }

    @Data
    public static final class GetResult implements KVStoreResult {
        @NonNull private final String value;
    }

    @Data
    public static final class KeyNotFound implements KVStoreResult {
    }

    @Data
    public static final class PutOk implements KVStoreResult {
    }

    @Data
    public static final class AppendResult implements KVStoreResult {
        @NonNull private final String value;
    }

    Map<String, String> kv = new Hashtable();

    @Override
    public KVStoreResult execute(Command command) {
        if (command instanceof Get) {
            Get g = (Get) command;
            if (kv.containsKey(g.key)) {
                String result = kv.get(g.key);
                return new GetResult(result);
            }
            return new KeyNotFound();

        }

        if (command instanceof Put) {
            Put p = (Put) command;
            kv.put(p.key, p.value);
            return new PutOk();
        }

        if (command instanceof Append) {
            Append a = (Append) command;
            if (kv.containsKey(a.key)) {
                String current = kv.get(a.key);
                kv.put(a.key,current + a.value);
            }
            else {
                kv.put(a.key,a.value);
            }
            return new AppendResult(kv.get(a.key));
        }

        throw new IllegalArgumentException();
    }
}


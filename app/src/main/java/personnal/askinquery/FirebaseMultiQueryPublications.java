package personnal.askinquery;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FirebaseMultiQueryPublications {

    private final HashSet<Query> refsQueries = new HashSet<>();
    private final HashMap<Query, DataSnapshot> snapsQueries = new HashMap<>();
    private final HashMap<Query, ValueEventListener> listenersQueries = new HashMap<>();

    FirebaseMultiQueryPublications(final ArrayList<Query> refs) {
        for (final Query ref : refs) {
            add(ref);
        }
    }
    public void add(final Query ref) {
        refsQueries.add(ref);
    }

    public Task<Map<Query, DataSnapshot>> start() {
        // Create a Task<DataSnapsot> to trigger in response to each database listener.
        //
        final ArrayList<Task<DataSnapshot>> tasks = new ArrayList<>(refsQueries.size());
    for (final Query ref : refsQueries) {
            final TaskCompletionSource<DataSnapshot> source = new TaskCompletionSource<>();
            final ValueEventListener listener = new MyValueEventListener(ref, source);
            ref.addListenerForSingleValueEvent(listener);
            listenersQueries.put(ref, listener);
            tasks.add(source.getTask());
        }

        // Return a single Task that triggers when all queries are complete.  It contains
        // a map of all original DatabaseReferences originally given here to their resulting
        // DataSnapshot.
        //
        return Tasks.whenAll(tasks).continueWith(new Continuation<Void, Map<Query, DataSnapshot>>() {
            @Override
            public Map<Query, DataSnapshot> then(@NonNull Task<Void> task) throws Exception {
                task.getResult();
                return new HashMap<>(snapsQueries);
            }
        });
    }
    void stop() {
        for (final Map.Entry<Query, ValueEventListener> entry : listenersQueries.entrySet()) {
            entry.getKey().removeEventListener(entry.getValue());
        }
        snapsQueries.clear();
        listenersQueries.clear();
    }

    private class MyValueEventListener implements ValueEventListener {
        private final Query ref;
        private final TaskCompletionSource<DataSnapshot> taskSource;

        MyValueEventListener(Query ref, TaskCompletionSource<DataSnapshot> taskSource) {
            this.ref = ref;
            this.taskSource = taskSource;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            snapsQueries.put(ref, dataSnapshot);
            taskSource.setResult(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            taskSource.setException(databaseError.toException());
        }
    }

}
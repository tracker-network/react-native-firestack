package io.fullstack.firestack;

import java.util.Map;
import java.util.List;
import android.net.Uri;
import android.util.Log;
import java.util.HashMap;
import android.content.Context;
import java.util.ListIterator;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import com.google.firebase.database.Query;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;

class DatabaseReference {
  private static final String TAG = "DatabaseReference";

  private String mPath;
//  private ReadableArray mModifiers;
  private HashMap<String, Boolean> mListeners = new HashMap<String, Boolean>();
  private Database mDatabase;
  private ChildEventListener mEventListener;
  private ValueEventListener mValueListener;
  private ValueEventListener mOnceValueListener;
  private ReactContext mReactContext;

  public DatabaseReference(final ReactContext context, final String path) {
    mReactContext = context;
    mPath = path;
  }

//  public void setModifiers(final ReadableArray modifiers) {
//    mModifiers = modifiers;
//  }

  public void addChildEventListener(final String name, final ReadableArray modifiersArray, final String modifiersString) {
    final DatabaseReference self = this;

    if (mEventListener == null) {
      mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
          self.handleDatabaseEvent("child_added", mPath, modifiersString, dataSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
          self.handleDatabaseEvent("child_changed", mPath, modifiersString, dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
          self.handleDatabaseEvent("child_removed", mPath, modifiersString, dataSnapshot);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
          self.handleDatabaseEvent("child_moved", mPath, modifiersString, dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError error) {
          self.handleDatabaseError(name, mPath, error);
        }
      };
      Query ref = this.getDatabaseQueryAtPathAndModifiers(modifiersArray);
      ref.addChildEventListener(mEventListener);
    }
  }

  public void addValueEventListener(final String name, final ReadableArray modifiersArray, final String modifiersString) {
    final DatabaseReference self = this;

    mValueListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        self.handleDatabaseEvent("value", mPath, modifiersString, dataSnapshot);
      }

      @Override
      public void onCancelled(DatabaseError error) {
        self.handleDatabaseError("value", mPath, error);
      }
    };

    Query ref = this.getDatabaseQueryAtPathAndModifiers(modifiersArray);
    ref.addValueEventListener(mValueListener);
    //this.setListeningTo(mPath, modifiersString, "value");
  }

  public void addOnceValueEventListener(final ReadableArray modifiersArray,
                                        final String modifiersString,
                                        final Callback callback) {
    final DatabaseReference self = this;

    mOnceValueListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        WritableMap data = Utils.dataSnapshotToMap("value", mPath, modifiersString, dataSnapshot);
        callback.invoke(null, data);
      }

      @Override
      public void onCancelled(DatabaseError error) {
        WritableMap err = Arguments.createMap();
        err.putInt("errorCode", error.getCode());
        err.putString("errorDetails", error.getDetails());
        err.putString("description", error.getMessage());
        callback.invoke(err);
      }
    };

    Query ref = this.getDatabaseQueryAtPathAndModifiers(modifiersArray);
    ref.addListenerForSingleValueEvent(mOnceValueListener);
  }

  //public Boolean isListeningTo(final String path, String modifiersString, final String evtName) {
  //  String key = this.pathListeningKey(path, modifiersString, evtName);
  //  return mListeners.containsKey(key);
  //}

  /**
   * Note: these path/eventType listeners only get removed when javascript calls .off() and cleanup is run on the entire path
   */
  //public void setListeningTo(final String path, String modifiersString, final String evtName) {
  //  String key = this.pathListeningKey(path, modifiersString, evtName);
  //  mListeners.put(key, true);
  //}

  //public void notListeningTo(final String path, String modifiersString, final String evtName) {
  //  String key = this.pathListeningKey(path, modifiersString, evtName);
  //  mListeners.remove(key);
  //}

  //private String pathListeningKey(final String path, String modifiersString, final String eventName) {
    //return "listener/" + path + "/" + modifiersString + "/" + eventName;
  //}

  public void cleanup() {
    Log.d(TAG, "cleaning up database reference " + this);
    this.removeChildEventListener();
    this.removeValueEventListener();
  }

  public void removeChildEventListener() {
    if (mEventListener != null) {
      com.google.firebase.database.DatabaseReference ref = this.getDatabaseRef();
      ref.removeEventListener(mEventListener);
      //this.notListeningTo(mPath, "child_added");
      //this.notListeningTo(mPath, "child_changed");
      //this.notListeningTo(mPath, "child_removed");
      //this.notListeningTo(mPath, "child_moved");
      mEventListener = null;
    }
  }

  public void removeValueEventListener() {
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseRef();
    if (mValueListener != null) {
      ref.removeEventListener(mValueListener);
      //this.notListeningTo(mPath, "value");
      mValueListener = null;
    }
    if (mOnceValueListener != null) {
      ref.removeEventListener(mOnceValueListener);
      mOnceValueListener = null;
    }
  }

  private void handleDatabaseEvent(final String name, final String path, final String modifiersString, final DataSnapshot dataSnapshot) {
    //if (!DatabaseReference.this.isListeningTo(path, modifiersString, name)) {
      //return;
    //}
    WritableMap data = Utils.dataSnapshotToMap(name, path, modifiersString, dataSnapshot);
    WritableMap evt = Arguments.createMap();
    evt.putString("eventName", name);
    evt.putString("path", path);
    evt.putString("modifiersString", modifiersString);
    evt.putMap("body", data);

    Utils.sendEvent(mReactContext, "database_event", evt);
  }

  private void handleDatabaseError(final String name, final String path, final DatabaseError error) {
    WritableMap err = Arguments.createMap();
    err.putInt("errorCode", error.getCode());
    err.putString("errorDetails", error.getDetails());
    err.putString("description", error.getMessage());

    WritableMap evt  = Arguments.createMap();
    evt.putString("eventName", name);
    evt.putString("path", path);
    evt.putMap("body", err);

    Utils.sendEvent(mReactContext, "database_error", evt);
  }

  public com.google.firebase.database.DatabaseReference getDatabaseRef() {
    return FirebaseDatabase.getInstance().getReference(mPath);
  }

  private Query getDatabaseQueryAtPathAndModifiers(final ReadableArray modifiers) {
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseRef();

    List<Object> strModifiers = Utils.recursivelyDeconstructReadableArray(modifiers);
    ListIterator<Object> it = strModifiers.listIterator();
    Query query = ref.orderByKey();

    while(it.hasNext()) {
      String str = (String) it.next();

      String[] strArr = str.split(":");
      String methStr = strArr[0];

      if (methStr.equalsIgnoreCase("orderByKey")) {
        query = ref.orderByKey();
      } else if (methStr.equalsIgnoreCase("orderByValue")) {
        query = ref.orderByValue();
      } else if (methStr.equalsIgnoreCase("orderByPriority")) {
        query = ref.orderByPriority();
      } else if (methStr.contains("orderByChild")) {
        String key = strArr[1];
        Log.d(TAG, "orderByChild: " + key);
        query = ref.orderByChild(key);
      } else if (methStr.contains("limitToLast")) {
        String key = strArr[1];
        int limit = Integer.parseInt(key);
        Log.d(TAG, "limitToLast: " + limit);
        query = query.limitToLast(limit);
      } else if (methStr.contains("limitToFirst")) {
        String key = strArr[1];
        int limit = Integer.parseInt(key);
        Log.d(TAG, "limitToFirst: " + limit);
        query = query.limitToFirst(limit);
      } else if (methStr.contains("equalTo")) {
        String value = strArr[1];
        String key = strArr.length >= 3 ? strArr[2] : null;
        if (key == null) {
          query = query.equalTo(value);
        } else {
          query = query.equalTo(value, key);
        }
      } else if (methStr.contains("endAt")) {
        String value = strArr[1];
        String key = strArr.length >= 3 ? strArr[2] : null;
        if (key == null) {
          query = query.endAt(value);
        } else {
          query = query.endAt(value, key);
        }
      } else if (methStr.contains("startAt")) {
        String value = strArr[1];
        String key = strArr.length >= 3 ? strArr[2] : null;
        if (key == null) {
          query = query.startAt(value);
        } else {
          query = query.startAt(value, key);
        }
      }
    }

    return query;
  }

}

class Database extends ReactContextBaseJavaModule {

  private static final String TAG = "FirestackDatabase";

  private Context context;
  private ReactContext mReactContext;
  private HashMap<String, DatabaseReference> mDBListeners = new HashMap<String, DatabaseReference>();

  public Database(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext;
    mReactContext = reactContext;
  }

  @Override
  public String getName() {
    return TAG;
  }

  // Persistence
  @ReactMethod
  public void enablePersistence(
    final Boolean enable,
    final Callback callback) {
      try {
        FirebaseDatabase.getInstance()
          .setPersistenceEnabled(enable);
      } catch (Throwable t) {
        Log.e(TAG, "FirebaseDatabase setPersistenceEnabled exception", t);
      }

      WritableMap res = Arguments.createMap();
      res.putString("status", "success");
      callback.invoke(null, res);
  }

  @ReactMethod
  public void keepSynced(
    final String path,
    final Boolean enable,
    final Callback callback) {
      com.google.firebase.database.DatabaseReference ref = this.getDatabaseReferenceAtPath(path);
      ref.keepSynced(enable);

      WritableMap res = Arguments.createMap();
      res.putString("status", "success");
      res.putString("path", path);
      callback.invoke(null, res);
  }

  // Database
  @ReactMethod
  public void set(
          final String path,
          final ReadableMap props,
          final Callback callback) {
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseReferenceAtPath(path);

    final Database self = this;
    Map<String, Object> m = Utils.recursivelyDeconstructReadableMap(props);

    com.google.firebase.database.DatabaseReference.CompletionListener listener = new com.google.firebase.database.DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError error, com.google.firebase.database.DatabaseReference ref) {
        handleCallback("set", callback, error, ref);
      }
    };

    ref.setValue(m, listener);
  }

  @ReactMethod
  public void update(final String path,
                     final ReadableMap props,
                     final Callback callback) {
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseReferenceAtPath(path);
    final Database self = this;
    Map<String, Object> m = Utils.recursivelyDeconstructReadableMap(props);

    com.google.firebase.database.DatabaseReference.CompletionListener listener = new com.google.firebase.database.DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError error, com.google.firebase.database.DatabaseReference ref) {
        handleCallback("update", callback, error, ref);
      }
    };

    ref.updateChildren(m, listener);
  }

  @ReactMethod
  public void remove(final String path,
                     final Callback callback) {
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseReferenceAtPath(path);
    final Database self = this;
    com.google.firebase.database.DatabaseReference.CompletionListener listener = new com.google.firebase.database.DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError error, com.google.firebase.database.DatabaseReference ref) {
        handleCallback("remove", callback, error, ref);
      }
    };

    ref.removeValue(listener);
  }

  @ReactMethod
  public void push(final String path,
                   final ReadableMap props,
                   final Callback callback) {

    Log.d(TAG, "Called push with " + path);
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseReferenceAtPath(path);
    com.google.firebase.database.DatabaseReference newRef = ref.push();

    final Uri url = Uri.parse(newRef.toString());
    final String newPath = url.getPath();

    ReadableMapKeySetIterator iterator = props.keySetIterator();
    if (iterator.hasNextKey()) {
      Log.d(TAG, "Passed value to push");
      // lame way to check if the `props` are empty
      final Database self = this;
      Map<String, Object> m = Utils.recursivelyDeconstructReadableMap(props);

      com.google.firebase.database.DatabaseReference.CompletionListener listener = new com.google.firebase.database.DatabaseReference.CompletionListener() {
        @Override
        public void onComplete(DatabaseError error, com.google.firebase.database.DatabaseReference ref) {
          if (error != null) {
            WritableMap err = Arguments.createMap();
            err.putInt("errorCode", error.getCode());
            err.putString("errorDetails", error.getDetails());
            err.putString("description", error.getMessage());
            callback.invoke(err);
          } else {
            WritableMap res = Arguments.createMap();
            res.putString("status", "success");
            res.putString("ref", newPath);
            callback.invoke(null, res);
          }
        }
      };

      newRef.setValue(m, listener);
    } else {
      Log.d(TAG, "No value passed to push: " + newPath);
      WritableMap res = Arguments.createMap();
      res.putString("result", "success");
      res.putString("ref", newPath);
      callback.invoke(null, res);
    }
  }

  @ReactMethod
  public void on(final String path,
                 final String modifiersString,
                 final ReadableArray modifiersArray,
                 final String name,
                 final Callback callback) {
    DatabaseReference ref = this.getDBHandle(path, modifiersString);

    WritableMap resp = Arguments.createMap();

    if (name.equals("value")) {
      ref.addValueEventListener(name, modifiersArray, modifiersString);
    } else {
      ref.addChildEventListener(name, modifiersArray, modifiersString);
    }

    this.saveDBHandle(path, modifiersString, ref);
    resp.putString("result", "success");
    Log.d(TAG, "Added listener " + name + " for " + ref + "with modifiers: "+ modifiersString);

    resp.putString("handle", path);
    callback.invoke(null, resp);
  }

  @ReactMethod
  public void onOnce(final String path,
                     final String modifiersString,
                     final ReadableArray modifiersArray,
                     final String name,
                     final Callback callback) {
    Log.d(TAG, "Setting one-time listener on event: " + name + " for path " + path);
    DatabaseReference ref = this.getDBHandle(path, modifiersString);
    ref.addOnceValueEventListener(modifiersArray, modifiersString, callback);
  }

  /**
   * At the time of this writing, off() only gets called when there are no more subscribers to a given path.
   * `mListeners` might therefore be out of sync (though javascript isnt listening for those eventTypes, so
   * it doesn't really matter- just polluting the RN bridge a little more than necessary.
   * off() should therefore clean *everything* up
   */
  @ReactMethod
  public void off(
          final String path,
          final String modifiersString,
          @Deprecated final String name,
          final Callback callback) {
    this.removeDBHandle(path, modifiersString);
    Log.d(TAG, "Removed listener " + path + "/" + modifiersString);
    WritableMap resp = Arguments.createMap();
    resp.putString("handle", path);
    resp.putString("result", "success");
    callback.invoke(null, resp);
  }

  // On Disconnect
  @ReactMethod
  public void onDisconnectSetObject(final String path, final ReadableMap props, final Callback callback) {
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseReferenceAtPath(path);
    Map<String, Object> m = Utils.recursivelyDeconstructReadableMap(props);

    OnDisconnect od = ref.onDisconnect();
    od.setValue(m, new com.google.firebase.database.DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, com.google.firebase.database.DatabaseReference databaseReference) {
        handleCallback("onDisconnectSetObject", callback, databaseError, databaseReference);
      }
    });
  }

  @ReactMethod
  public void onDisconnectSetString(final String path, final String value, final Callback callback) {
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseReferenceAtPath(path);

    OnDisconnect od = ref.onDisconnect();
    od.setValue(value, new com.google.firebase.database.DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, com.google.firebase.database.DatabaseReference databaseReference) {
        handleCallback("onDisconnectSetString", callback, databaseError, databaseReference);
      }
    });
  }

  @ReactMethod
  public void onDisconnectRemove(final String path, final Callback callback) {
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseReferenceAtPath(path);

    OnDisconnect od = ref.onDisconnect();
    od.removeValue(new com.google.firebase.database.DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, com.google.firebase.database.DatabaseReference databaseReference) {
        handleCallback("onDisconnectRemove", callback, databaseError, databaseReference);
      }
    });
  }
  @ReactMethod
  public void onDisconnectCancel(final String path, final Callback callback) {
    com.google.firebase.database.DatabaseReference ref = this.getDatabaseReferenceAtPath(path);

    OnDisconnect od = ref.onDisconnect();
    od.cancel(new com.google.firebase.database.DatabaseReference.CompletionListener() {
      @Override
      public void onComplete(DatabaseError databaseError, com.google.firebase.database.DatabaseReference databaseReference) {
        handleCallback("onDisconnectCancel", callback, databaseError, databaseReference);
      }
    });
  }

  // Private helpers
  // private void handleDatabaseEvent(final String name, final DataSnapshot dataSnapshot) {
  //   WritableMap data = this.dataSnapshotToMap(name, dataSnapshot);
  //   WritableMap evt  = Arguments.createMap();
  //   evt.putString("eventName", name);
  //   evt.putMap("body", data);
  //   Utils.sendEvent(mReactContext, "database_event", evt);
  // }

  // private void handleDatabaseError(final String name, final DatabaseError error) {
  //   WritableMap err = Arguments.createMap();
  //   err.putInt("errorCode", error.getCode());
  //   err.putString("errorDetails", error.getDetails());
  //   err.putString("description", error.getMessage());

  //   WritableMap evt  = Arguments.createMap();
  //   evt.putString("eventName", name);
  //   evt.putMap("body", err);
  //   Utils.sendEvent(mReactContext, "database_error", evt);
  // }

  private void handleCallback(
          final String methodName,
          final Callback callback,
          final DatabaseError databaseError,
          final com.google.firebase.database.DatabaseReference databaseReference) {
    if (databaseError != null) {
      WritableMap err = Arguments.createMap();
      err.putInt("errorCode", databaseError.getCode());
      err.putString("errorDetails", databaseError.getDetails());
      err.putString("description", databaseError.getMessage());
      callback.invoke(err);
    } else {
      WritableMap res = Arguments.createMap();
      res.putString("status", "success");
      res.putString("method", methodName);
      callback.invoke(null, res);
    }
  }

  private DatabaseReference getDBHandle(final String path, final String modifiersString) {
    String key = this.getDBListenerKey(path, modifiersString);
    if (!mDBListeners.containsKey(key)) {
      ReactContext ctx = getReactApplicationContext();
      mDBListeners.put(key, new DatabaseReference(ctx, path));
    }

    return mDBListeners.get(key);
  }

  private void saveDBHandle(final String path, String modifiersString, final DatabaseReference dbRef) {
    String key = this.getDBListenerKey(path, modifiersString);
    mDBListeners.put(key, dbRef);
  }

  private String getDBListenerKey(String path, String modifiersString) {
    return path + " | " + modifiersString;
  }

  private void removeDBHandle(final String path, String modifiersString) {
    String key = this.getDBListenerKey(path, modifiersString);
    if (mDBListeners.containsKey(key)) {
      DatabaseReference r = mDBListeners.get(key);
      r.cleanup();
      mDBListeners.remove(key);
    }
  }

  private String keyPath(final String path, final String eventName) {
    return path + "-" + eventName;
  }

  // TODO: move to DatabaseReference?
  private com.google.firebase.database.DatabaseReference getDatabaseReferenceAtPath(final String path) {
    com.google.firebase.database.DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(path);
    return mDatabase;
  }
}
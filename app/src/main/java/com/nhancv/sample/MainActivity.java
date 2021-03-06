package com.nhancv.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.nhancv.realmbowser.NRealmDiscovery;
import com.nhancv.realmbowser.NRealmServer;
import com.nhancv.sample.model.Person;
import com.nhancv.sample.model.User;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.activity_main_tv_msg);

        initRealm();
        String address = NRealmServer.getServerAddress(this);
        textView.setText(address);
        Log.e(TAG, "Server address: " + address);

        genSampleRealm();
        viewSample();
        NRealmServer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NRealmServer.stop();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(0)
                .build();
        Realm.setDefaultConfiguration(config);

        NRealmServer.init(new NRealmDiscovery(this, config));
        NRealmServer.getInstance().setEnableCorns(true);

    }

    private void genSampleRealm() {
        // Get a Realm instance for this thread
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setId(i);
            user.setAge(i);
            user.setName("User-" + i);

            RealmList<Person> personList = new RealmList<>();
            for (int j = 0; j < 10; j++) {
                Person person = new Person();
                person.setId(j);
                person.setName(String.format("Person-%s.%s", i, j));
                personList.add(person);
            }
            user.setPersonList(personList);
            realm.insertOrUpdate(user);
        }
        realm.commitTransaction();

    }

    private void viewSample() {
        RealmSchema schema = Realm.getDefaultInstance().getSchema();
        for (RealmObjectSchema realmObjectSchema : schema.getAll()) {
            Log.e(TAG, "RealmSchema: " + realmObjectSchema.getClassName());
            for (String s : realmObjectSchema.getFieldNames()) {
                Log.e(TAG, "onCreate:filed name: " + s);
            }
        }
        RealmResults<User> users = Realm.getDefaultInstance().where(User.class).findAll();
        for (User user : users) {
            Log.e(TAG, "onCreate: " + user);
        }
    }
}

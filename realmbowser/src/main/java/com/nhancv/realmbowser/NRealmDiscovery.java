package com.nhancv.realmbowser;

import android.content.Context;

import com.google.gson.Gson;
import com.nhancv.realmbowser.model.DataResponse;
import com.nhancv.realmbowser.model.Schema;
import com.nhancv.realmbowser.model.SchemaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.RealmModel;
import io.realm.RealmObjectSchema;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.RealmSchema;

/**
 * Created by nhancao on 4/12/17.
 */

public class NRealmDiscovery implements RealmDiscovery {
    private static final String TAG = NRealmDiscovery.class.getSimpleName();
    private Context context;

    public NRealmDiscovery(Context context, RealmConfiguration realmConfiguration) {
        this.context = context;
        Realm.init(context);
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public Context getContext() {
        return context;
    }

    @Override
    public String getSchema() {
        Realm realm = Realm.getDefaultInstance();

        List<Schema> schemaList = null;
        try {
            DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
            Set<Class<? extends RealmModel>> modelClasses = realm.getConfiguration().getRealmObjectClasses();

            schemaList = new ArrayList<>();
            for (Class<? extends RealmModel> modelClass : modelClasses) {
                RealmSchema rSchema = dynamicRealm.getSchema();
                RealmObjectSchema realmObjectSchema = rSchema.get(modelClass.getSimpleName());
                Set<String> fieldNames = realmObjectSchema.getFieldNames();
                List<Schema.Structure> structures = new ArrayList<>();
                for (String fieldName : fieldNames) {
                    RealmFieldType realmFieldType = realmObjectSchema.getFieldType(fieldName);
                    Schema.Structure structure = new Schema.Structure(fieldName, realmFieldType.name());
                    structures.add(structure);
                }
                schemaList.add(new Schema(modelClass.getSimpleName(), structures));
            }
            if (schemaList.size() > 0) {
                Collections.sort(schemaList, new Comparator<Schema>() {
                    @Override
                    public int compare(Schema o1, Schema o2) {
                        String str1 = o1.getName();
                        String str2 = o2.getName();
                        int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
                        return (res != 0) ? res : str1.compareTo(str2);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        realm.close();

        return toJson(schemaList);
    }

    @Override
    public String getSchema(String tableName) {

        try {
            try {
                Realm realm = Realm.getDefaultInstance();
                DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
                Set<Class<? extends RealmModel>> modelClasses = realm.getConfiguration().getRealmObjectClasses();
                Class<? extends RealmModel> modelModel = null;
                for (Class<? extends RealmModel> modelClass : modelClasses) {
                    if (modelClass.getSimpleName().equals(tableName)) {
                        modelModel = modelClass;
                        break;
                    }
                }

                if (modelModel != null) {
                    RealmSchema rSchema = dynamicRealm.getSchema();
                    RealmObjectSchema realmObjectSchema = rSchema.get(modelModel.getSimpleName());
                    Set<String> fieldNames = realmObjectSchema.getFieldNames();
                    List<Schema.Structure> structures = new ArrayList<>();
                    for (String fieldName : fieldNames) {
                        RealmFieldType realmFieldType = realmObjectSchema.getFieldType(fieldName);
                        Schema.Structure structure = new Schema.Structure(fieldName, realmFieldType.name());
                        structures.add(structure);
                    }
                    realm.close();
                    return toJson(new Schema(tableName, structures));
                } else {
                    throw new ClassNotFoundException("ClassNotFoundException");
                }
            } catch (ClassNotFoundException e) {
                return toJson(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toJson(null);
    }

    @Override
    public String query(String where) {

        try {
            try {
                Realm realm = Realm.getDefaultInstance();
                DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
                Set<Class<? extends RealmModel>> modelClasses = realm.getConfiguration().getRealmObjectClasses();
                Class<? extends RealmModel> modelModel = null;
                for (Class<? extends RealmModel> modelClass : modelClasses) {
                    if (modelClass.getSimpleName().equals(where)) {
                        modelModel = modelClass;
                        break;
                    }
                }

                if (modelModel != null) {
                    RealmSchema schema = dynamicRealm.getSchema();
                    RealmObjectSchema realmObjectSchema = schema.get(modelModel.getSimpleName());

                    List<String> columns = new ArrayList<>(realmObjectSchema.getFieldNames());

                    RealmFieldType[] realmFieldTypes = new RealmFieldType[columns.size()];
                    int index = 0;
                    for (String fieldName : columns) {
                        realmFieldTypes[index] = realmObjectSchema.getFieldType(fieldName);
                        index++;
                    }
                    RealmQuery<DynamicRealmObject> realmQuery = dynamicRealm.where(modelModel.getSimpleName());
                    RealmResults<DynamicRealmObject> realmResults = realmQuery.findAll();
                    int totalSize = realmResults.size();

                    List<List<Object>> rows = new ArrayList<>();
                    for (int i = 0; i < totalSize; i++) {
                        DynamicRealmObject dynamicRealmObject = realmResults.get(i);
                        List<Object> rowData = new ArrayList<>();
                        for (int j = 0; j < columns.size(); j++) {
                            String columnName = columns.get(j);
                            Object res;
                            switch (realmFieldTypes[j]) {
                                case INTEGER:
                                    res = dynamicRealmObject.getLong(columnName);
                                    break;
                                case BOOLEAN:
                                    res = dynamicRealmObject.getBoolean(columnName);
                                    break;
                                case FLOAT:
                                    res = dynamicRealmObject.getFloat(columnName);
                                    break;
                                case DOUBLE:
                                    res = dynamicRealmObject.getDouble(columnName);
                                    break;
                                case DATE:
                                    res = dynamicRealmObject.getDate(columnName).toString();
                                    break;
                                case STRING:
                                    res = dynamicRealmObject.getString(columnName);
                                    break;
                                case LIST:
                                    res = String.format("Size(%s)", dynamicRealmObject.getList(columnName).size());
                                    break;
                                default:
                                    res = "n/a";
                                    break;
                            }
                            rowData.add(res);
                        }
                        rows.add(rowData);
                    }
                    realm.close();

                    for (int i = 0; i < columns.size(); i++) {
                        String column = columns.get(i);
                        column = column + " (" + realmFieldTypes[i] + ")";
                        columns.set(i, column);
                    }

                    SchemaData schemaData = new SchemaData(columns, rows, totalSize);
                    return toJson(schemaData);

                } else {
                    throw new ClassNotFoundException("ClassNotFoundException");
                }
            } catch (ClassNotFoundException e) {
                return toJson(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toJson(null);
    }

    @Override
    public String query(String where, String field, NRealmController.QUERY action, String value) {

        try {
            try {
                Realm realm = Realm.getDefaultInstance();
                DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
                Set<Class<? extends RealmModel>> modelClasses = realm.getConfiguration().getRealmObjectClasses();
                Class<? extends RealmModel> modelModel = null;
                for (Class<? extends RealmModel> modelClass : modelClasses) {
                    if (modelClass.getSimpleName().equals(where)) {
                        modelModel = modelClass;
                        break;
                    }
                }

                if (modelModel != null) {
                    RealmSchema schema = dynamicRealm.getSchema();
                    RealmObjectSchema realmObjectSchema = schema.get(modelModel.getSimpleName());

                    List<String> columns = new ArrayList<>(realmObjectSchema.getFieldNames());

                    RealmFieldType realmFieldType = realmObjectSchema.getFieldType(field);
                    RealmFieldType[] realmFieldTypes = new RealmFieldType[columns.size()];
                    int index = 0;
                    for (String fieldName : columns) {
                        realmFieldTypes[index] = realmObjectSchema.getFieldType(fieldName);
                        index++;
                    }

                    RealmQuery<DynamicRealmObject> realmQuery = dynamicRealm.where(modelModel.getSimpleName());
                    switch (realmFieldType) {
                        case INTEGER:
                            switch (action) {
                                case EQUAL:
                                    realmQuery.equalTo(field, Long.parseLong(value));
                                    break;
                                case BEGIN:
                                    realmQuery.beginsWith(field, value);
                                    break;
                                default:
                                    realmQuery.contains(field, value);
                                    break;
                            }
                            break;
                        case BOOLEAN:
                            switch (action) {
                                case EQUAL:
                                    realmQuery.equalTo(field, Boolean.parseBoolean(value));
                                    break;
                                case BEGIN:
                                    realmQuery.beginsWith(field, value);
                                    break;
                                default:
                                    realmQuery.contains(field, value);
                                    break;
                            }
                            break;
                        case FLOAT:
                            switch (action) {
                                case EQUAL:
                                    realmQuery.equalTo(field, Float.parseFloat(value));
                                    break;
                                case BEGIN:
                                    realmQuery.beginsWith(field, value);
                                    break;
                                default:
                                    realmQuery.contains(field, value);
                                    break;
                            }
                            break;
                        case DOUBLE:
                            switch (action) {
                                case EQUAL:
                                    realmQuery.equalTo(field, Double.parseDouble(value));
                                    break;
                                case BEGIN:
                                    realmQuery.beginsWith(field, value);
                                    break;
                                default:
                                    realmQuery.contains(field, value);
                                    break;
                            }
                            break;
                        default:
                            switch (action) {
                                case EQUAL:
                                    realmQuery.equalTo(field, value);
                                    break;
                                case BEGIN:
                                    realmQuery.beginsWith(field, value);
                                    break;
                                default:
                                    realmQuery.contains(field, value);
                                    break;
                            }
                            break;
                    }
                    RealmResults<DynamicRealmObject> realmResults = realmQuery.findAll();
                    int totalSize = realmResults.size();

                    List<List<Object>> rows = new ArrayList<>();
                    for (int i = 0; i < totalSize; i++) {
                        DynamicRealmObject dynamicRealmObject = realmResults.get(i);
                        List<Object> rowData = new ArrayList<>();
                        for (int j = 0; j < columns.size(); j++) {
                            String columnName = columns.get(j);
                            Object res;
                            switch (realmFieldTypes[j]) {
                                case INTEGER:
                                    res = dynamicRealmObject.getLong(columnName);
                                    break;
                                case BOOLEAN:
                                    res = dynamicRealmObject.getBoolean(columnName);
                                    break;
                                case FLOAT:
                                    res = dynamicRealmObject.getFloat(columnName);
                                    break;
                                case DOUBLE:
                                    res = dynamicRealmObject.getDouble(columnName);
                                    break;
                                case DATE:
                                    res = dynamicRealmObject.getDate(columnName).toString();
                                    break;
                                case STRING:
                                    res = dynamicRealmObject.getString(columnName);
                                    break;
                                case LIST:
                                    res = String.format("Size(%s)", dynamicRealmObject.getList(columnName).size());
                                    break;
                                default:
                                    res = "n/a";
                                    break;
                            }
                            rowData.add(res);
                        }
                        rows.add(rowData);
                    }
                    realm.close();

                    for (int i = 0; i < columns.size(); i++) {
                        String column = columns.get(i);
                        column = column + " (" + realmFieldTypes[i] + ")";
                        columns.set(i, column);
                    }

                    SchemaData schemaData = new SchemaData(columns, rows, totalSize);
                    return toJson(schemaData);

                } else {
                    throw new ClassNotFoundException("ClassNotFoundException");
                }
            } catch (ClassNotFoundException e) {
                return toJson(e);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return toJson(null);
    }

    public <T> String toJson(T data) {
        return new Gson().toJson(new DataResponse<>(data));
    }


}

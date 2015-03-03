package au.com.shrinkray.bluetoothscanner.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by neal on 3/03/15.
 */
public class BidirectionalMap<KeyType, ValueType> {

    private Map<KeyType, ValueType> keyToValueMap = new ConcurrentHashMap<KeyType, ValueType>();
    private Map<ValueType, KeyType> valueToKeyMap = new ConcurrentHashMap<ValueType, KeyType>();

    synchronized public ValueType put(KeyType key, ValueType value){
        keyToValueMap.put(key, value);
        valueToKeyMap.put(value, key);
        return value;
    }

    synchronized public ValueType removeByKey(KeyType key){
        ValueType removedValue = keyToValueMap.remove(key);
        if ( removedValue != null ){
            valueToKeyMap.remove(removedValue);
        }
        return removedValue;
    }

    synchronized public KeyType removeByValue(ValueType value){
        KeyType removedKey = valueToKeyMap.remove(value);
        keyToValueMap.remove(removedKey);
        return removedKey;
    }

    public boolean containsKey(KeyType key){
        return keyToValueMap.containsKey(key);
    }

    public boolean containsValue(ValueType value){
        return valueToKeyMap.containsKey(value);
    }

    public KeyType getKey(ValueType value){
        return valueToKeyMap.get(value);
    }

    public ValueType get(KeyType key){
        return keyToValueMap.get(key);
    }

    public void clear() {
        keyToValueMap.clear();
        valueToKeyMap.clear();
    }

 }
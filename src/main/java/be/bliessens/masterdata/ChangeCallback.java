package be.bliessens.masterdata;

public interface ChangeCallback<T> {

    void entryRemoved(T entry);

    void entryAdded(T entry);

    void entryStateChanged(T entry);
}

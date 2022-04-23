package dev.lightdream.databasemanager.dto;

import dev.lightdream.databasemanager.DatabaseMain;
import dev.lightdream.databasemanager.annotations.database.DatabaseField;

import java.util.Objects;

@SuppressWarnings("unused")
public abstract class DatabaseEntry<T> {

    @DatabaseField(columnName = "id",
            autoGenerate = true,
            unique = true,
            primaryKey = true)
    private T id;
    private DatabaseMain main;

    public DatabaseEntry(DatabaseMain main) {
        this.main = main;
    }

    @SuppressWarnings("unused")
    public void save() {
        save(true);
    }

    public void save(boolean cache) {
        this.main.getDatabaseManager()
                .save(this, cache);
    }

    @SuppressWarnings("unused")
    public void delete() {
        main.getDatabaseManager()
                .delete(this);
    }

    public void setMain(DatabaseMain main) {
        this.main = main;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseEntry<?> that = (DatabaseEntry<?>) o;
        return Objects.equals(getID(), that.getID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public T getID() {
        return id;
    }
}

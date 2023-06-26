package Server.Database;

public interface Insertable<T> {
    boolean insert(T toAdd);
}

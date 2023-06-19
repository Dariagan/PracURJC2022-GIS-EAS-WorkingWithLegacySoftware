package raf.principal;

public interface ClassManager {

    void addOne(String className);
    void removeOne(String className);
    Class<?> findClass (String name);
    void removeClass(String name);

}

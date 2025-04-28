public class Holder<T> {
    T val;

    public Holder(T val) {
        set(val);
    }
    public T get() {
        return val;
    }

    public void set(T val) {
        this.val = val;
    }
}

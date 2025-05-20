import java.util.List;

public interface Callback<T>
{
    void onComplete(T result);
}
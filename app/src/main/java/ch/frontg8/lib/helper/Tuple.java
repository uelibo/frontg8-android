package ch.frontg8.lib.helper;

public class Tuple<T, U> {

    public final T _1;
    public final U _2;

    public Tuple(T val1, U val2) {
        this._1 = val1;
        this._2 = val2;
    }

    @Override
    public String toString(){
        return String.format("(%s, %s)", _1, _2);
    }
}

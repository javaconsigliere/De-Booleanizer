import org.junit.jupiter.api.Test;

public class NumberTest {
    @Test
    public void numberEquality()
    {
        for(int i = 0; i < 256; i++)
        {
            int a = i;
            int b = i;
            long lA = i;
            long lB = i;
            assert a==b;
            assert lA==lB;
            assert lA==a;
        }
    }
}

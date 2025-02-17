package jdbi.doc;

import java.util.Arrays;
import java.util.Collection;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.junit5.H2DatabaseExtension;
import org.jdbi.v3.core.statement.Batch;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchTest {

    @RegisterExtension
    public H2DatabaseExtension h2Extension = H2DatabaseExtension.instance().withPlugin(new SqlObjectPlugin());

    private Handle handle;

    @BeforeEach
    public void getHandle() {
        handle = h2Extension.getSharedHandle();
        handle.execute("CREATE TABLE fruit (id INT PRIMARY KEY, name VARCHAR)");
    }

    @Test
    public void testSimpleBatch() {
        // tag::simpleBatch[]
        Batch batch = handle.createBatch();

        batch.add("INSERT INTO fruit VALUES(0, 'apple')");
        batch.add("INSERT INTO fruit VALUES(1, 'banana')");

        int[] rowsModified = batch.execute();
        // end::simpleBatch[]
        assertThat(rowsModified).containsExactly(1, 1);
        assertThat(handle.createQuery("SELECT count(1) FROM fruit")
                .mapTo(int.class)
                .one()
                .intValue())
                .isEqualTo(2);
    }

    @Test
    // tag::sqlObjectBatch[]
    public void testSqlObjectBatch() {
        BasketOfFruit basket = handle.attach(BasketOfFruit.class);

        int[] rowsModified = basket.fillBasket(Arrays.asList(
                new Fruit(0, "apple"),
                new Fruit(1, "banana")));

        assertThat(rowsModified).containsExactly(1, 1);
        assertThat(basket.countFruit()).isEqualTo(2);
    }

    public interface BasketOfFruit {
        @SqlBatch("INSERT INTO fruit VALUES(:id, :name)")
        int[] fillBasket(@BindBean Collection<Fruit> fruits);

        @SqlQuery("SELECT count(1) FROM fruit")
        int countFruit();
    }
    // end::sqlObjectBatch[]

    public class Fruit {
        private final int id;
        private final String name;

        Fruit(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}

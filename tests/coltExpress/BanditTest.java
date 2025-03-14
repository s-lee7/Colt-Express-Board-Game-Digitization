package coltExpress;

import org.testng.annotations.Test;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.*;


public class BanditTest {

    @Test
    public void testDropRandomLoot() {
        Bandit bandit = new Bandit(1);
        Butin loot1 = new Butin("bourses");

        /** Add some loot to the bandit */
        bandit.addButin(loot1);

        /** Test dropping random loot */
        Butin droppedLoot = bandit.dropRandomLoot();
        assertNotNull(droppedLoot);
        assertFalse(bandit.getLoots().contains(droppedLoot));
    }

    @Test
    public void testAnotherBanditThan() {
        // Create a wagon
        TrainElts wagon = new TrainElts(false, 1);

        // Create bandits
        Bandit p = new Bandit(1);
        Bandit b1 = new Bandit(2);
        Bandit b2 = new Bandit(3);

        // Set bandits' wagons
        p.setWagon(wagon);
        b1.setWagon(wagon);
        b2.setWagon(wagon);

        Bandit result = p.getPosition().anotherBanditThan(p);

        // the result is not null
        assertNotNull(result);

        // the result is a different bandit (not p)
        assertNotEquals(p, result);

        // the result is indeed in the same wagon as p
        assertEquals(p.getPosition(), result.getPosition());
    }

}

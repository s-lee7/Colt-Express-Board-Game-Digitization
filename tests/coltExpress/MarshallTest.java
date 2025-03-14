package coltExpress;


import org.testng.annotations.Test;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.*;

public class MarshallTest {

    Train train = new Train();


    @Test
    public void testInitialPosition() {
        TrainElts initPosition = train.marshall.getPosition();
        assertNotNull( train.marshall.getPosition());
        assertTrue(train.marshall.isNotOnRoof());
    }


    @Test
    public void testMoveBanditToRoof() {
        Train.removeAllBanditsForTests();
        Bandit bandit1 = new Bandit(1);
        bandit1.setWagon(Train.wagons.get(0).get(0));
        train.marshall.moveBanditToRoof(bandit1);
        assertTrue(bandit1.getPosition().isRoof());
    }
    @Test
    public void testSetWagonMarshall() {
        Train.removeAllBanditsForTests();

        if (train.marshall.getPosition() == Train.wagons.get(0).get(0)){
        train.marshall.setWagonMarshall(Train.wagons.get(0).get(1));
        assertEquals(train.marshall.getPosition(), Train.wagons.get(0).get(1));
        assertNull(Train.wagons.get(0).get(0).getMarshall());}
    }

    @Test
    public void testMove() {

        train.removeAllBanditsForTests();
        assertTrue(train.marshall.isNotOnRoof());
        Bandit bandit1 = new Bandit(1);
        bandit1.setWagon(Train.wagons.get(0).get(1));
        Butin loot1 = new Butin("bourses");
        bandit1.addButin(loot1);

        assertNotEquals(train.marshall.getPosition(), bandit1.getPosition());
        Deplacer d = new Deplacer(bandit1, Direction.AVANT);
        d.execute();
        assertEquals(train.marshall.getPosition(), bandit1.getPosition());
        train.marshall.move();
        // if marshall meets bandit1, then bandit1 drops a random loot
        if (train.marshall.getPosition() == bandit1.getPosition())
            assertTrue(bandit1.getLoots().isEmpty());


    }

}

package coltExpress;

import org.testng.annotations.Test;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.*;

public class TrainEltsTest {

    Train train = new Train();

    @Test
    public void testNeighbor() {
        TrainElts wagon = new TrainElts(false, 1);

        // Test moving forward
        TrainElts nextWagon = wagon.neighbor(Direction.ARRIERE);
        //System.out.println(nextWagon.toString());
        assertEquals("nextWagon wrong", 2, nextWagon.getWagonNum());

        // Test moving backward
        TrainElts prevWagon = wagon.neighbor(Direction.AVANT);
        //System.out.println(prevWagon.toString());
        assertEquals("prevWagon wrong",0, prevWagon.getWagonNum()); // Assuming there's no negative wagon number

        wagon.neighbor(Direction.AVANT);
        // Test boundary cases
        assertNotNull("negative wagon error",prevWagon.neighbor(Direction.AVANT)); // Wagon number should not be negative

    }

    @Test
    public void testPlaceButin() {
        TrainElts wagon = new TrainElts(false, 1);
        int nbInit = wagon.getButins().size();
        Butin loot = new Butin("bourses");

        // Test placing loot in the wagon = initial nb of butins inside plus the new one
        wagon.placeButin(loot);
        assertEquals(1+nbInit, wagon.getButins().size());
        assertTrue(wagon.getButins().contains(loot));
    }

}
